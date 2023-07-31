package re.qqsdk;

import com.github.unidbg.AssemblyCodeDumper;
import com.github.unidbg.Emulator;
import com.github.unidbg.listener.TraceCodeListener;

public class MyCodeDumper extends AssemblyCodeDumper {
    public MyCodeDumper(Emulator<?> emulator, long begin, long end, TraceCodeListener listener) {
        super(emulator, begin, end, listener);
    }
}
