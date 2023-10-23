package re.chatgpt;

import com.github.unidbg.Emulator;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import re.util.LogUtil;

public class FileHandler implements IOResolver<AndroidFileIO> {
    private static final String TAG = FileHandler.class.getSimpleName();

    @Override
    public FileResult<AndroidFileIO> resolve(Emulator<AndroidFileIO> emulator, String pathname, int oflags) {
        LogUtil.e(TAG,String.format("need fileHandler : [%d] %s",oflags,pathname));
        return null;
    }
}
