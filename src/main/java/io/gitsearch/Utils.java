package io.gitsearch;

import java.util.Base64;

public class Utils {
    public static String toBase64(String url) {
        return Base64.getUrlEncoder().encodeToString(url.getBytes());
    }
}
