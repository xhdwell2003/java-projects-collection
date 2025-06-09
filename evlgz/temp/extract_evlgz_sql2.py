import os
import re
import pandas as pd
from datetime import datetime
import xml.etree.ElementTree as ET
import argparse

def find_method_details_and_content(java_code, sql_line_num_1based):
    lines = java_code.splitlines()
    
    method_decl_line_pattern = re.compile(
        r'^\s*'
        r'((?:(?:public|protected|private|static|final|synchronized|abstract|default|native|strictfp)\s+)*'
        r'(?:[\w\<\>\[\],\s\.\?]+\s+)?)?'
        r'(?!(?:if|for|while|switch|catch|try|finally|else|return|throw|new|instanceof|assert|do|case|continue|break|default|goto|package|import|super|this|class|interface|enum|extends|implements|throws|transient|volatile|strictfp|native|synchronized|final|static|private|protected|public|abstract|const)\b)'
        r'([a-zA-Z_$][\w$]*)'
        r'\s*\('
    )

    method_candidates = []
    for i, line_content in enumerate(lines):
        match = method_decl_line_pattern.match(line_content)
        if match:
            method_candidates.append({
                "name": match.group(2),
                "declaration_line_idx": i, # 0-based index of the line where method signature starts
            })

    # Iterate through candidates, usually the one closest (and above) the SQL line is the target
    for candidate in sorted(method_candidates, key=lambda c: c["declaration_line_idx"], reverse=True):
        # Method declaration must be at or before the SQL line
        if candidate["declaration_line_idx"] + 1 > sql_line_num_1based:
            continue

        # --- Logic to find end of parameters and start of method body ---
        param_paren_balance = 0
        params_fully_closed_line_idx = -1
        
        # Start scanning for parentheses from the declaration line
        # Find the first '(' on the declaration line to start balancing
        first_paren_char_idx_on_decl_line = -1
        for char_idx, char_val in enumerate(lines[candidate["declaration_line_idx"]]):
            if char_val == '(':
                first_paren_char_idx_on_decl_line = char_idx
                break
        
        if first_paren_char_idx_on_decl_line == -1: continue # Should not happen if regex matched

        # Scan from the declaration line onwards to find where parameters truly end
        for line_idx_params in range(candidate["declaration_line_idx"], len(lines)):
            current_line_for_params = lines[line_idx_params]
            start_char_scan = 0
            if line_idx_params == candidate["declaration_line_idx"]:
                start_char_scan = first_paren_char_idx_on_decl_line # Start from the first '('

            for char_idx_param in range(start_char_scan, len(current_line_for_params)):
                char_in_line = current_line_for_params[char_idx_param]
                if char_in_line == '(': param_paren_balance += 1
                elif char_in_line == ')': param_paren_balance -= 1
            
            if param_paren_balance == 0: # Parameters are now balanced
                # Ensure the closing ')' is actually on this line if balance became 0
                if ')' in current_line_for_params[start_char_scan if line_idx_params == candidate["declaration_line_idx"] else 0:]:
                    params_fully_closed_line_idx = line_idx_params
                    break 
            
            # Safety break if params span too many lines (e.g., > 20 lines for params)
            if line_idx_params > candidate["declaration_line_idx"] + 20:
                params_fully_closed_line_idx = -1 # Mark as not found
                break
        
        if params_fully_closed_line_idx == -1: continue # Could not reliably find end of parameters

        # --- Logic to find the opening brace '{' of the method body ---
        open_brace_line_idx = -1
        # Start looking for '{' from the line where parameters ended
        for line_idx_brace in range(params_fully_closed_line_idx, len(lines)):
            current_line_for_brace = lines[line_idx_brace]
            # If on the same line as params end, search after the params part
            start_search_char_idx_for_brace = 0
            if line_idx_brace == params_fully_closed_line_idx:
                # Find the last ')' of the params on this line to search after it
                last_param_paren_idx = current_line_for_brace.rfind(')')
                if last_param_paren_idx != -1:
                    start_search_char_idx_for_brace = last_param_paren_idx + 1
            
            if '{' in current_line_for_brace[start_search_char_idx_for_brace:]:
                open_brace_line_idx = line_idx_brace
                break
            # If we encounter a ';' on the same line or immediately after params close, it might be an abstract/interface method
            if ';' in current_line_for_brace[start_search_char_idx_for_brace:] and line_idx_brace <= params_fully_closed_line_idx + 1 :
                 # Check if this ';' is not part of a for-loop on the same line as '{'
                if '{' not in current_line_for_brace[start_search_char_idx_for_brace:]: # No '{' on this line
                    open_brace_line_idx = -2 # Indicate no body (abstract/interface)
                    break
            
            # Safety break if '{' is not found soon after params
            if line_idx_brace > params_fully_closed_line_idx + 5: break
        
        if open_brace_line_idx == -2: continue # Abstract/interface method, skip
        if open_brace_line_idx == -1: continue # No opening brace found

        # --- Logic to find the matching closing brace '}' ---
        body_brace_balance = 0
        method_body_end_line_idx = -1
        
        # Start brace counting from the line with the opening brace
        for k_idx in range(open_brace_line_idx, len(lines)):
            line_for_balance = lines[k_idx]
            
            # Consider only part of the line after '{' if on the same line
            start_char_idx_for_balance = 0
            if k_idx == open_brace_line_idx:
                first_body_brace_char_idx = line_for_balance.find('{')
                if first_body_brace_char_idx != -1:
                    start_char_idx_for_balance = first_body_brace_char_idx
            
            for char_idx_balance in range(start_char_idx_for_balance, len(line_for_balance)):
                char_to_balance = line_for_balance[char_idx_balance]
                if char_to_balance == '{': body_brace_balance += 1
                elif char_to_balance == '}': body_brace_balance -= 1
            
            if body_brace_balance == 0: # If balance is zero, this line contains the final '}'
                # Ensure '}' is actually on this line if balance became 0
                if '}' in line_for_balance[start_char_idx_for_balance if k_idx == open_brace_line_idx else 0:]:
                    method_body_end_line_idx = k_idx
                    break
        
        if method_body_end_line_idx != -1:
            # Ensure SQL line is within the method body (between declaration and found end)
            # and specifically within the braces.
            if (candidate["declaration_line_idx"] + 1 <= sql_line_num_1based <= method_body_end_line_idx + 1 and
                open_brace_line_idx + 1 <= sql_line_num_1based): # SQL must be at or after the line with '{'
                
                content_lines = lines[candidate["declaration_line_idx"] : method_body_end_line_idx + 1]
                method_content = "\n".join(content_lines)
                return candidate["name"], candidate["declaration_line_idx"] + 1, method_content
    
    return "Unknown_Method", sql_line_num_1based, f"SQL Fragment at line {sql_line_num_1based}. Could not determine enclosing method and its full content."

def process_java_file(file_path, root_dir):
    results = []
    try:
        content = None
        try:
            with open(file_path, 'r', encoding='utf-8') as f: content = f.read()
        except UnicodeDecodeError:
            try:
                with open(file_path, 'r', encoding='gbk') as f: content = f.read()
            except Exception as e_gbk:
                print(f"Could not read Java file {file_path} with UTF-8 or GBK: {e_gbk}"); return results
        if content is None: return results
        lines = content.splitlines()
        sql_keyword_pattern = r'\.EVLGZ\b' 
        potential_sql_locations = []
        str_literal_pattern = re.compile(r'".*?"', re.DOTALL)
        append_pattern = re.compile(r'\.append\s*\(\s*".*?"\s*\)', re.DOTALL)
        for i, line_content in enumerate(lines):
            for match_str in str_literal_pattern.finditer(line_content):
                if re.search(sql_keyword_pattern, match_str.group(0)):
                    potential_sql_locations.append({"line_num": i + 1, "sql_snippet": match_str.group(0)})
            for match_append in append_pattern.finditer(line_content):
                appended_str_match = re.search(r'".*?"', match_append.group(0), re.DOTALL)
                if appended_str_match and re.search(sql_keyword_pattern, appended_str_match.group(0)):
                    potential_sql_locations.append({"line_num": i + 1, "sql_snippet": appended_str_match.group(0)})
        processed_methods = set()
        for loc in potential_sql_locations:
            sql_line_num = loc["line_num"]
            method_name, method_declaration_line, method_content = find_method_details_and_content(content, sql_line_num)
            method_identifier = (method_name, method_declaration_line)
            if method_declaration_line != -1 and method_name != "Unknown_Method" and method_identifier not in processed_methods:
                results.append({
                    "文件全路径": os.path.relpath(file_path, root_dir), "方法名": method_name,
                    "行号": method_declaration_line, "方法内容": method_content,
                }); processed_methods.add(method_identifier)
            elif (method_name == "Unknown_Method" or method_declaration_line == -1) and loc["sql_snippet"]:
                unknown_loc_id = (os.path.relpath(file_path, root_dir), sql_line_num)
                if unknown_loc_id not in processed_methods:
                    results.append({
                        "文件全路径": os.path.relpath(file_path, root_dir), "方法名": "SQL_Fragment_Container_Unknown",
                        "行号": sql_line_num,
                        "方法内容": f"SQL Snippet: {loc['sql_snippet']}\n(Enclosing method details could not be fully determined.)",
                    }); processed_methods.add(unknown_loc_id)
    except Exception as e: print(f"Error processing Java file {file_path}: {e}")
    return results

def _parse_xml_with_encoding_fallback(file_path):
    xml_lines_for_numbering = []
    tree = None
    def attempt_parse(encoding_to_try):
        nonlocal xml_lines_for_numbering
        with open(file_path, 'r', encoding=encoding_to_try) as f_lines:
            xml_lines_for_numbering = f_lines.readlines()
        parser = ET.XMLParser(encoding=encoding_to_try)
        parsed_tree = ET.parse(file_path, parser=parser)
        return parsed_tree
    try:
        tree = attempt_parse('utf-8')
    except (UnicodeDecodeError, ET.ParseError) as e_utf8:
        try:
            tree = attempt_parse('gbk')
        except (UnicodeDecodeError, ET.ParseError) as e_gbk:
            raise ValueError(f"Failed to parse XML {file_path} with UTF-8 (Error: {e_utf8}) and GBK (Error: {e_gbk}).")
        except Exception as e_gbk_other:
            raise ValueError(f"Failed to parse XML {file_path} with GBK (Other Error: {e_gbk_other}), after UTF-8 failed (Error: {e_utf8}).")
    except Exception as e_initial_other:
        raise ValueError(f"Unexpected error during initial UTF-8 parse of {file_path}: {e_initial_other}")
    return tree, xml_lines_for_numbering

def process_xml_file(file_path, root_dir):
    results = []
    try:
        tree, xml_content_lines = _parse_xml_with_encoding_fallback(file_path)
        if not tree: return results
        xml_root = tree.getroot()
        sql_keyword_pattern_xml = r'\.EVLGZ\b'
        for elem in xml_root.iter():
            if elem.tag.endswith(("select", "insert", "update", "delete", "sql")):
                inner_sql_text = ET.tostring(elem, encoding='unicode', method='text').strip()
                if re.search(sql_keyword_pattern_xml, inner_sql_text):
                    select_id = elem.get("id", "N/A")
                    elem_line_num = -1; element_xml_content = "Could not accurately extract XML element content."
                    tag_name_for_search = elem.tag.split('}')[-1]
                    search_str_start = f"<{tag_name_for_search}"
                    search_str_id_part = f'id="{select_id}"' if select_id != "N/A" else None
                    for i, line_content_from_read_lines in enumerate(xml_content_lines):
                        line_matches_start = search_str_start in line_content_from_read_lines
                        line_matches_id = search_str_id_part and search_str_id_part in line_content_from_read_lines
                        if select_id != "N/A" and line_matches_start and line_matches_id:
                            elem_line_num = i + 1; break
                        elif select_id == "N/A" and line_matches_start:
                            if inner_sql_text and inner_sql_text[:min(30, len(inner_sql_text))].strip() in line_content_from_read_lines : 
                                elem_line_num = i + 1; break
                    if elem_line_num != -1:
                        tag_name = elem.tag.split('}')[-1]
                        open_tag_count = 0; content_lines_for_element = []
                        for line_idx in range(elem_line_num - 1, len(xml_content_lines)):
                            current_xml_line = xml_content_lines[line_idx]
                            if line_idx == elem_line_num -1 or open_tag_count > 0:
                                content_lines_for_element.append(current_xml_line.rstrip('\n\r'))
                            open_tag_matches = re.findall(r"<" + re.escape(tag_name) + r"(?:\s|>)", current_xml_line)
                            open_tag_count += len(open_tag_matches)
                            close_tag_matches = re.findall(r"</" + re.escape(tag_name) + r">", current_xml_line)
                            open_tag_count -= len(close_tag_matches)
                            if line_idx == elem_line_num - 1 and "/>" in current_xml_line and len(open_tag_matches) > 0 :
                                open_tag_count = 0 
                            if open_tag_count == 0 and line_idx >= elem_line_num -1 : break
                        if content_lines_for_element:
                             element_xml_content = "\n".join(content_lines_for_element)
                    results.append({
                        "文件全路径": os.path.relpath(file_path, root_dir), "方法名": select_id,
                        "行号": elem_line_num if elem_line_num != -1 else "N/A",
                        "方法内容": element_xml_content,
                    })
    except ValueError as ve: print(f"{ve}")
    except Exception as e: print(f"Generic error processing XML file {file_path}: {e}")
    return results

def main(scan_directory_path):
    all_results = []
    scan_directory = os.path.abspath(scan_directory_path)
    if not os.path.isdir(scan_directory):
        print(f"Error: Root directory '{scan_directory}' not found or not a directory."); return
    print(f"Starting SQL extraction in directory: {scan_directory}")
    for subdir, dirs, files in os.walk(scan_directory):
        dirs[:] = [d for d in dirs if d not in ['__pycache__', 'temp']]
        for file in files:
            file_path = os.path.join(subdir, file)
            if file.endswith(".java"): all_results.extend(process_java_file(file_path, scan_directory))
            elif file.endswith(".xml"): all_results.extend(process_xml_file(file_path, scan_directory))

    if not all_results: print("No relevant SQL information found."); return
    try:
        df = pd.DataFrame(all_results)
        if not df.empty:
            df_output = df.drop_duplicates(subset=["文件全路径", "方法名", "行号"])
            output_columns = ["文件全路径", "方法名", "行号", "方法内容"]
            df_output = df_output[output_columns]
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            output_filename = os.path.join(scan_directory, f"evlgz_sql_extraction_{timestamp}.xlsx")
            df_output.to_excel(output_filename, index=False)
            print(f"Successfully extracted SQL information to {output_filename}")
        else: print("No data to write to Excel.")
    except ModuleNotFoundError as mnfe:
        if 'openpyxl' in str(mnfe).lower():
            print(f"Error writing to Excel: {mnfe}. 'openpyxl' module is required. Install via: pip install openpyxl")
        else: print(f"Error writing to Excel: {mnfe}. A required module is missing.")
    except Exception as e: print(f"Error writing to Excel file: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Extract EVLGZ related SQL from Java and XML files.")
    parser.add_argument(
        "-r", "--root", dest="root_directory",
        help="Root directory to scan. Defaults to current working directory.", default=None
    )
    args = parser.parse_args()
    target_scan_dir = args.root_directory if args.root_directory else os.getcwd()
    if args.root_directory is None:
        print(f"--root not specified. Defaulting to current working directory: {target_scan_dir}")
    main(target_scan_dir)