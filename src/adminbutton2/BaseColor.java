// SPDX-License-Identifier: GPL-3.0
package adminbutton2;

import java.math.BigInteger;

public class BaseColor {
    public static final String base = "0123456789ABCDEFabcdef";
    public static final int baseLength = base.length();

    public static String encode(byte[] src) {
        BigInteger big = new BigInteger(1, src);
        StringBuilder sb = new StringBuilder();
        while (big.compareTo(BigInteger.ZERO) > 0) {
            sb.append(base.charAt(big.mod(BigInteger.valueOf(baseLength)).intValue()));
            big = big.divide(BigInteger.valueOf(baseLength));
        }
        for (byte b : src) {
            if (b == 0) sb.append(base.charAt(0)); else break;
        }
        return sb.reverse().toString();
    }

    public static byte[] decode(String src) throws IllegalArgumentException {
        BigInteger big = BigInteger.ZERO;
        for (char c : src.toCharArray()) {
            int number = base.indexOf(c);
            if (number == -1) throw new IllegalArgumentException("Invalid BaseColor character: " + c);
            big = big.multiply(BigInteger.valueOf(baseLength)).add(BigInteger.valueOf(number));
        }
        int zeros = 0;
        for (char c : src.toCharArray()) {
            if (c == base.charAt(0)) zeros++;
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
