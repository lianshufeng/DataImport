package com.github.data.core.util;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class CRC32Util {


    public static String update(String data) {
        byte[] phoneBin = data.getBytes(StandardCharsets.UTF_8);
        CRC32 crc32 = new CRC32();
        crc32.update(phoneBin, 0, phoneBin.length);
        return Long.toHexString(crc32.getValue());
    }


    /**
     * 2次crc32
     * @param data
     * @return
     */
    public static String update2(String data) {
        return update(update(data));
    }


}
