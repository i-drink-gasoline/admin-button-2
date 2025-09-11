// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import java.math.BigInteger;

public class BaseUTF16 {
    private char minChar = 0x80;
    private char maxChar = Character.MAX_VALUE;

    public BaseUTF16(char minChar, char maxChar) {
        this.minChar = minChar;
        this.maxChar = maxChar;
    }

    public String encode(byte[] src) {
        BigInteger big = new BigInteger(1, src);
        String s = "";
        int diff = maxChar - minChar + 1;
        while (big.compareTo(BigInteger.ZERO) > 0) {
            s = (char)(big.mod(BigInteger.valueOf(diff)).intValue() + minChar) + s;
            big = big.divide(BigInteger.valueOf(diff));
        }
        for (byte b : src) {
            if (b == 0) s = (char)(minChar) + s; else break;
        }
        return s;
    }

    public byte[] decode(String src) throws IllegalArgumentException {
        BigInteger big = BigInteger.ZERO;
        int diff = maxChar - minChar + 1;
        for (char c : src.toCharArray()) {
            if (c < minChar) throw new IllegalArgumentException("Invalid codepoint: " + (int)c);
            big = big.multiply(BigInteger.valueOf(diff)).add(BigInteger.valueOf(c - minChar));
        }
        int zeros = 0;
        for (char c : src.toCharArray()) {
            if (c == minChar) zeros++;
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
