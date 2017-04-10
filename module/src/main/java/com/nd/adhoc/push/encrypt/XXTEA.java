package com.nd.adhoc.push.encrypt;

import java.nio.charset.Charset;

/*
 * XXTEA 加密算法
 */
public class XXTEA implements SymmetricCipher{

    private static int DELTA = 0x9e3779b9;
    private static int MIN_LENGTH = 32;
    private static char SPECIAL_CHAR = '\0';
    private String key;

    @Override
    public void init(String key) {
        this.key = key;
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return XXTeaEncrypt(data, this.key);
    }

    @Override
    public byte[] decrypt(byte[] data, int position, int length) {
        return XXTeaDecrypt(data, position, length, this.key);
    }


    private byte[] XXTeaEncrypt(byte[] data, String key) {
        return ToByteArray(XXTEAEncrypt(
                ToIntArray(data, true),
                ToIntArray(PadRight(key, MIN_LENGTH).getBytes(
                        Charset.forName("UTF8")), false)), false);
    }

    private byte[] XXTeaDecrypt(byte[] data, int position, int length, String key) {

        byte[] code = ToByteArray(XXTEADecrypt(
                ToIntArray(data, position, length),
                ToIntArray(PadRight(key, MIN_LENGTH).getBytes(
                        Charset.forName("UTF8")), false)), true);
        return code;
    }

    private static int[] XXTEAEncrypt(int[] data, int[] key) {
        int n = data.length;
        if (n < 1) {
            return data;
        }

        int z = data[data.length - 1], y = data[0], sum = 0, e, p, q;
        q = 6 + 52 / n;
        while (q-- > 0) {
            sum += DELTA;
            e = (sum >>> 2) & 3;
            for (p = 0; p < n - 1; p++) {
                y = data[(p + 1)];
                z = data[p] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4)
                        ^ (sum ^ y) + (key[(p & 3 ^ e)] ^ z);
            }
            y = data[0];
            z = data[n - 1] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4)
                    ^ (sum ^ y) + (key[(p & 3 ^ e)] ^ z);
        }

        return data;
    }

    private static int[] XXTEADecrypt(int[] data, int[] key) {
        int n = data.length;
        if (n < 1) {
            return data;
        }

        int z = data[data.length - 1], y = data[0], sum = 0, e, p, q;
        q = 6 + 52 / n;
        sum = q * DELTA;
        while (sum != 0) {
            e = (sum >>> 2) & 3;
            for (p = n - 1; p > 0; p--) {
                z = data[(p - 1)];
                y = data[p] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4)
                        ^ (sum ^ y) + (key[(p & 3 ^ e)] ^ z);
            }
            z = data[n - 1];
            y = data[0] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y)
                    + (key[(p & 3 ^ e)] ^ z);
            sum -= DELTA;
        }

        return data;
    }


    private static int[] ToIntArray(byte[] data, boolean withlength) {
        int n =  ( (data.length % 8 == 0 ? 0 : 1) + data.length / 8 ) * 2;
        int[] result;
        if (withlength){
            result = new int[n+2];
        } else {
            result = new int[n];
        }

        for (int i = 0; i < n - 2; i+=2) {
            result[i] = bytes2int(data, i * 4);
            result[i+1] = bytes2int(data, (i + 1)* 4 );
        }

        byte[] buffer = new byte[8];
        for (int i = 0, j = (n - 2) * 4; j < data.length; i++, j++) {
            buffer[i] = data[j];
        }
        result[n - 2] = bytes2int(buffer, 0);
        result[n - 1] = bytes2int(buffer, 4);
        if (withlength){
            result[n] = data.length;
        }
        return result;
    }

    private static int[] ToIntArray(byte[] data, int position, int length) {
        int n =  length / 4;
        int[] result;
        result = new int[n];

        for (int i = 0; i < n; i++) {
            result[i] = bytes2int(data, position + i * 4);
        }
        return result;
    }

    private static byte[] ToByteArray(int[] data, boolean withlength) {
        if (!withlength){
            int dataSize = data.length*4;
            byte[] ret = new byte[dataSize];
            int index = 0;
            for (int i = 0; i < data.length && index<dataSize; i++) {
                byte[] bs = int2bytes(data[i]);
                for (int j = 0; j < 4 && index<dataSize; j++) {
                    ret[index++] = bs[j];
                }
            }
            return ret;
        }
        int dataSize = data[data.length - 2];
        if (dataSize>81920 || dataSize<0){
            return null;
        }
        byte[] ret = new byte[dataSize];
        int index = 0;
        for (int i = 0; i < data.length - 2 && index<dataSize; i++) {
            byte[] bs = int2bytes(data[i]);
            for (int j = 0; j < 4 && index<dataSize; j++) {
                ret[index++] = bs[j];
            }
        }
        return ret;
    }

    public static byte[] int2bytes(int num) {
//        ByteBuffer buffer = ByteBuffer.allocate(4).order(
//                ByteOrder.LITTLE_ENDIAN);
//        buffer.putInt(num);
//        return buffer.array();

        byte []ret = new byte[4];
        ret[0] = (byte) (num & 0xFF);
        ret[1] = (byte) ((num >>> 8) & 0xFF);
        ret[2] = (byte) ((num >>> 16) & 0xFF);
        ret[3] = (byte) ((num >>> 24) & 0xFF);
        return ret;
    }

    public static int bytes2int(byte[] data, int offset) {
//        ByteBuffer buffer = ByteBuffer.allocate(4).order(
//                ByteOrder.LITTLE_ENDIAN);
//        buffer.put(data, offset, 4);
//        int ret = buffer.getInt(0);
        int tmp0 = (data[offset + 0] & 0xFF);
        int tmp1 = (data[offset + 1] & 0xFF) << 8;
        int tmp2 = (data[offset + 2] & 0xFF) << 16;
        int tmp3 = (data[offset + 3] & 0xFF) << 24;
        int tmp = (tmp0 | tmp1 | tmp2 | tmp3);

//        if (ret!=tmp){
//            tmp = ret;
//        }

        return tmp;
    }

    private static String PadRight(String source, int length) {
        while (source.length() < length) {
            source += SPECIAL_CHAR;
        }
        return source;
    }

}