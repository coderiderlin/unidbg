package re.util;

public class Utils {
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                // 如果转换后的十六进制数只有一位，前面补0
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    public static void sleep(long t){
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
        }
    }
}
