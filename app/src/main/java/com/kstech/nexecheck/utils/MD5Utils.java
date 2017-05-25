package com.kstech.nexecheck.utils;

/**
 * Created by lijie on 2016/12/23.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    /**
     * md5加密
     */
    public static String md5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无对应加密算法");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static String encodeFile(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            FileInputStream in = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }

            byte[] bytes = digest.digest();

            StringBuffer sb = new StringBuffer();
            for (byte b : bytes) {
                int i = b & 0xff;
                String hexString = Integer.toHexString(i);
                // System.out.println(hexString);
                if (hexString.length() == 1) {
                    hexString = "0" + hexString;
                }

                sb.append(hexString);
            }

            String md5 = sb.toString();

            return md5;
        } catch (Exception e) {
            // 没有此算法异常
            e.printStackTrace();
        }

        return null;
    }

    /**
     * String s = getMac();
     * s = MD5Utils.md5(s);
     * String rs = readMD5file();
     * Toast.makeText(getApplicationContext(),rs+"\n"+s,Toast.LENGTH_SHORT).show();
     * Log.e("MD5","s    "+s);
     * Log.e("MD5","rs   "+rs);
     * if(!s.equals(rs)){
     *      UninstallAPP();
     *      finish();
     * }
     * if(s.equals(rs)){
     *     generateMD5(s);
     *     Toast.makeText(getApplicationContext(),s+"\n机型匹配成功",Toast.LENGTH_SHORT).show();
     * }else {
     *    Toast.makeText(getApplicationContext(),s+"\n机型匹配no成功",Toast.LENGTH_SHORT).show();
     * }
     *
     * @param activity
     */
    public static void UninstallAPP(Activity activity) {
        Uri packageuri = Uri.parse("package:" + activity.getPackageName());
        Intent intent = new Intent(Intent.ACTION_DELETE, packageuri);
        activity.startActivity(intent);
    }

    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static void generateMD5(String s) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("/storage/sdcard1/Models/check.txt", true);
            PrintStream printStream = new PrintStream(fileOutputStream);
            printStream.println(s);
            printStream.flush();
            printStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readMD5file() {
        File file = new File("/storage/sdcard1/Models/check.txt");
        BufferedReader reader = null;
        String tempString = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            // 一次读入一行，直到读入null为文件结束
            tempString = reader.readLine();
            return tempString;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "no d a ";
    }
}
