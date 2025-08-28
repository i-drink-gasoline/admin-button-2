// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import java.math.BigInteger;

public class BaseUTF16 {
    public static String encode(byte[] src) {
        BigInteger big = new BigInteger(1, src);
        String s = "";
        while (big.compareTo(BigInteger.ZERO) > 0) {
            s = (char)(big.mod(BigInteger.valueOf(Character.MAX_VALUE - ' ')).intValue() + ' ') + s;
            big = big.divide(BigInteger.valueOf(Character.MAX_VALUE - ' '));
        }
        for (byte b : src) {
            if (b == 0) s = (char)(' ' + 1) + s; else break;
        }
        return s;
    }

    public static byte[] decode(String src) throws IllegalArgumentException {
        BigInteger big = BigInteger.ZERO;
        for (char c : src.toCharArray()) {
            if (c <= ' ') throw new IllegalArgumentException("Invalid codepoint: " + (int)c);
            big = big.multiply(BigInteger.valueOf(Character.MAX_VALUE - ' ')).add(BigInteger.valueOf(c - ' '));
        }
        int zeros = 0;
        for (char c : src.toCharArray()) {
            if (c == ' ' + 1) zeros++;
            else break;
        }
        byte[] decoded = big.toByteArray();
        int trim;
        if (decoded[0] == 0) trim = 1; else trim = 0;
        byte[] dst = new byte[zeros + decoded.length - trim];
        System.arraycopy(decoded, trim, dst, zeros, decoded.length - trim);
        return dst;
    }
}
