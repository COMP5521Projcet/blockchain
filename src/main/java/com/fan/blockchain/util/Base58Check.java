package com.fan.blockchain.util;

import javax.xml.crypto.Data;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

public final class Base58Check {
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger ALPHABET_SIZE = BigInteger.valueOf(ALPHABET.length());
    public static String byteToBase58(byte[] data){
        return rawByteToBase58(data);
    }

    /**
     * 转化为Base58字符串
     * @param data
     * @return
     */
    public static String rawByteToBase58(byte[] data) {
        StringBuilder sb = new StringBuilder();
        BigInteger num = new BigInteger(1, data);
        while (num.signum() != 0){
            BigInteger[] quotRem = num.divideAndRemainder(ALPHABET_SIZE);
            sb.append(ALPHABET.charAt(quotRem[1].intValue()));
            num = quotRem[0];
        }
        // add '1' characters for leading 0-value byte
        for (int i = 0;i < data.length && data[i] == 0;i++){
            sb.append(ALPHABET.charAt(0));
        }
        return sb.reverse().toString();
    }

    /**
     * 添加校验码并返回带有校验码的原生数据
     * @param data
     * @return
     */
    static byte[] addCheckHash(byte[] data) {
        try {
            byte[] hash = Arrays.copyOf(BtcAddressUtils.doubleHash(data),4);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            buf.write(data);
            buf.write(hash);
            return  buf.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 将Base58Check 字符串转化为byte数组，并校验其校验码
     * 返回的byte数组带有版本号，但不带有校验码
     * @param s
     * @return
     */
    public static byte[] base58ToBytes(String s) {
        byte[] concat = base58ToRawBytes(s);
        byte[] data = Arrays.copyOf(concat, concat.length - 4);
        byte[] hash = Arrays.copyOfRange(concat, concat.length - 4, concat.length);
        byte[] rehash = Arrays.copyOf(BtcAddressUtils.doubleHash(data), 4);
        if (!Arrays.equals(rehash,hash)){
            throw new IllegalArgumentException("Checksum mismatch");
        }
        return data;
    }

    /**
     * 将Base58Check 字符串反转为 byte数组
     * @param s
     * @return
     */
    private static byte[] base58ToRawBytes(String s) {
        // parse base58 string
        BigInteger num = BigInteger.ZERO;
        for (int i = 0;i < s.length();i++){
            num = num.multiply(ALPHABET_SIZE);
            int digit = ALPHABET.indexOf(s.charAt(i));
            if (digit == -1){
                throw new IllegalArgumentException("Invalid character for Base58Check");
            }
            num = num.add(BigInteger.valueOf(digit));
        }
        byte[] b = num.toByteArray();
        if (b[0] == 0){
            b = Arrays.copyOfRange(b,1,b.length);
        }
        try {
            // convert leading '1' characters to leading 0-value bytes
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (int i = 0;i < s.length() && s.charAt(i) == ALPHABET.charAt(0);i++){
                buf.write(0);
            }
            buf.write(b);
            return buf.toByteArray();
        } catch (IOException e){
            throw new AssertionError(e);
        }
    }
}
