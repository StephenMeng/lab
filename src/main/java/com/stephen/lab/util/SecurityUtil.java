package com.stephen.lab.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {
    public static String md5(String input) {
        LogRecod.print(input);
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //加密密码
        messageDigest.update(input.getBytes());
        // 加密结果
        byte[] md = messageDigest.digest();
        return byteArrayToHexString(md);
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private static String byteToHexString(byte b) {
        int number = b & 0xff;
        String str = Integer.toHexString(number);
        return str;
    }
}
