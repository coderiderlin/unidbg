package re.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {
    public static void i(String tag,String msg)
    {
        log("INFO",tag,msg);
    }
    public static void e(String tag,String msg)
    {
        log("ERROR",tag,msg);
    }

    public static void log(String level,String tag,String msg)
    {
        System.err.printf("%s [%s] %s%n",level,tag,msg);
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }
}
