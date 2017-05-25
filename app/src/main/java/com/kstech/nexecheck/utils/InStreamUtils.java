package com.kstech.nexecheck.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lijie on 2016/4/25.
 */
public class InStreamUtils {
    public static String Stream2String(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        is.close();
        out.close();

        return out.toString();
    }
}