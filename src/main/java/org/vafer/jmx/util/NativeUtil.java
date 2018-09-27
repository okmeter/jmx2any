package org.vafer.jmx.util;

import java.io.FileInputStream;
import java.io.IOException;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class NativeUtil {
    private static final CStdLib STD_LIB = Native.loadLibrary("c", CStdLib.class);

    public interface CStdLib extends Library {
        int syscall(int number, Object... args);
    }

    public static int setns(String nsPath) throws IOException {
        FileInputStream file = new FileInputStream(nsPath);
        int fd = sun.misc.SharedSecrets.getJavaIOFileDescriptorAccess().get(file.getFD());

        // 308 == setns, 0x4 -- network namespace
        int res = STD_LIB.syscall(308, fd, 0x40000000);

        file.close();

        return res;
    }

}
