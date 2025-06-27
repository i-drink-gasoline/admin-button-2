// SPDX-License-Identifier: GPL-3.0
package adminbutton2;

import arc.math.Mathf;
import mindustry.gen.Iconc;

import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import adminbutton2.BaseColor;

public class Secret {
    public static String icons = Iconc.all + "♿";
    public static String generateSecretMessage(char icon, String message) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(message.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        byte[] buffer = new byte[message.length() * 3 / 2 + 16];
        int length = deflater.deflate(buffer);
        byte[] compressed = new byte[length];
        System.arraycopy(buffer, 0, compressed, 0, length);
        deflater.end();
        StringBuilder sb = new StringBuilder("[#");
        for (char c : BaseColor.encode(compressed).toCharArray()) {
            if (sb.length() % 10 == 8) sb.append("]" + icon + "[#");
            sb.append(c);
        }
        sb.append("]" + icon);
        return sb.toString();
    }

    public static String readSecretMessage(String message) {
        StringBuilder sb = new StringBuilder();
        boolean expectOpen = true;
        boolean expectNumber = false;
        boolean expectBase = false;
        boolean expectClose = false;
        boolean expectIcon = false;
        int bases = 0;
        String best = "";
        for (char c : message.toCharArray()) {
            if (expectOpen && c == '[') {
                expectOpen = false;
                expectNumber = true;
            } else if (expectNumber && c == '#') {
                expectNumber = false;
                expectBase = true;
            } else if (expectBase && BaseColor.base.indexOf(c) != -1) {
                bases++;
                sb.append(c);
                expectClose = true;
                if (bases == 6) {
                    expectBase = false;
                    bases = 0;
                }
            } else if (expectClose && c == ']') {
                expectClose = false;
                expectBase = false;
                expectIcon = true;
            } else if (expectIcon && icons.indexOf(c) != -1) {
                expectIcon = false;
                expectOpen = true;
            } else {
                if ((expectOpen || expectNumber) && (sb.length() > best.length())) best = sb.toString();
                sb.delete(0, sb.length());
                expectOpen = true;
                expectNumber = false;
                expectBase = false;
                expectClose = false;
                expectIcon = false;
                bases = 0;
            }
        }
        if ((expectOpen || expectNumber) && (sb.length() > best.length())) best = sb.toString();
        if (best.isEmpty()) return best;
        byte[] data;
        try {
            data = BaseColor.decode(best);
        } catch (IllegalArgumentException e) {
            return "";
        }
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        byte[] result = new byte[128];
        try {
            int length = inflater.inflate(result);
            inflater.end();
            return new String(result, 0, length, StandardCharsets.UTF_8);
        } catch (java.util.zip.DataFormatException e) {
            return "";
        }
    }

    public static final String colchars = "01234567890abcdef";
    public static String generateRandomMessage(char icon, int maxLength) {
        if (maxLength < 10) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = Mathf.random(1, maxLength / 10); i > 0; i--) {
            sb.append("[#");
            for (int j = 0; j < 6; j++) {
                sb.append(colchars.charAt(Mathf.random(0, 15)));
            }
            sb.append("]" + icon);
        }
        return sb.toString();
    }
}
