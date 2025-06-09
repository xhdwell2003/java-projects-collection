// 获取最大和最小的确认日期
LocalDate minGzqrrq = listAll.stream()
        .filter(record -> record.getGzqrrq() != null)
        .min(Comparator.comparing(CheckDetailMonRecord::getGzqrrq))
        .map(CheckDetailMonRecord::getGzqrrq)
        .orElse(null);

LocalDate maxGzqrrq = listAll.stream()
        .filter(record -> record.getGzqrrq() != null)
        .max(Comparator.comparing(CheckDetailMonRecord::getGzqrrq))
        .map(CheckDetailMonRecord::getGzqrrq)
        .orElse(null);

// 打印结果
System.out.println("最小确认日期: " + minGzqrrq);
System.out.println("最大确认日期: " + maxGzqrrq);