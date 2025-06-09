import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringExtractor {

    public static String[] extractParts(String input) {
        if (input == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("(.*?)(HIGHER|LOWER)(.*)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String[] result = new String[2];
            result[0] = matcher.group(1).trim(); // Part before HIGHER/LOWER
            result[1] = matcher.group(3).trim(); // Part after HIGHER/LOWER
            return result;
        }
        return null; // Or throw an exception if the pattern is not found
    }

    public static void main(String[] args) {
        String s1 = "PHzWr HIGHER vnSpn";
        String s2 = "PHzWr LOWER vnSpn";
        String s3 = "NoKeywordHere";

        String[] parts1 = extractParts(s1);
        if (parts1 != null) {
            System.out.println("For s1: Before = '" + parts1[0] + "', After = '" + parts1[1] + "'");
        } else {
            System.out.println("For s1: Keyword not found.");
        }

        String[] parts2 = extractParts(s2);
        if (parts2 != null) {
            System.out.println("For s2: Before = '" + parts2[0] + "', After = '" + parts2[1] + "'");
        } else {
            System.out.println("For s2: Keyword not found.");
        }
        
        String[] parts3 = extractParts(s3);
        if (parts3 != null) {
            System.out.println("For s3: Before = '" + parts3[0] + "', After = '" + parts3[1] + "'");
        } else {
            System.out.println("For s3: Keyword not found.");
        }
    }
}
