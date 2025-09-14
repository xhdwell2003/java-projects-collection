import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class StringFinder {

    /**
     * 在列表中查找包含所有其他字符串的字符串。
     * 如果找不到，则返回列表中最长的字符串。
     *
     * @param list 输入的字符串列表
     * @return 包含所有其他字符串的字符串，或者列表中最长的字符串
     */
    public static String findLongestStringContainingOthers(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        // 使用Stream API查找包含所有其他字符串的记录
        Optional<String> container = list.stream()
                .filter(potentialContainer -> list.stream()
                        .filter(other -> !potentialContainer.equals(other))
                        .allMatch(potentialContainer::contains))
                .findFirst();

        // 如果找到了，直接返回
        if (container.isPresent()) {
            return container.get();
        }

        // 兜底流程：如果没有找到，则返回列表中最长的字符串
        return list.stream()
                .max(Comparator.comparingInt(String::length))
                .orElse(null); // 在list为空的情况下返回null
    }

    public static void main(String[] args) {
        ArrayList<String> keyList = new ArrayList<>();
        keyList.add("filterKxzCL");
        keyList.add("filtermrSXn");
        keyList.add(" (  ( filteriOHEE and filtermrSXn )  and  ( filterKxzCL or filterrENLL )  ) ");
        keyList.add("filterrENLL");
        keyList.add(" ( filteriOHEE and filtermrSXn ) ");
        keyList.add(" ( filterKxzCL or filterrENLL ) ");
        keyList.add("filteriOHEE");

        String result = findLongestStringContainingOthers(keyList);
        System.out.println("包含所有其他字符串的记录是: " + result);

        // 测试兜底流程
        ArrayList<String> keyList2 = new ArrayList<>();
        keyList2.add("abc");
        keyList2.add("defg");
        keyList2.add("hi");
        String result2 = findLongestStringContainingOthers(keyList2);
        System.out.println("最长的字符串是: " + result2);
    }
}
