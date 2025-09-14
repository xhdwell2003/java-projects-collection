import java.util.concurrent.ThreadLocalRandom;

public class UniqueIdGenerator {

    /**
     * 生成一个16位的long类型ID.
     * 构成: 13位时间戳 + 3位随机数.
     *
     * @return 16位的ID
     */
    public static long generateId() {
        // 1. 获取13位的毫秒时间戳
        long timestamp = System.currentTimeMillis();

        // 2. 生成一个0-999的随机数 (保证是3位数)
        // ThreadLocalRandom在多线程环境下性能更好
        int randomSuffix = ThreadLocalRandom.current().nextInt(1000);

        // 3. 组合ID: 时间戳 * 1000 + 随机数
        // 乘以1000是为了给后面的3位随机数腾出位置
        long uniqueId = timestamp * 1000 + randomSuffix;

        return uniqueId;
    }

    public static void main(String[] args) {
        long id = generateId();
        System.out.println("生成的16位ID: " + id);
        System.out.println("ID长度: " + String.valueOf(id).length());

        // 连续生成5个ID查看效果
        System.out.println("\n--- 连续生成5个ID ---");
        for (int i = 0; i < 5; i++) {
            System.out.println(generateId());
        }
    }
}
