package com.example.android_development.util;

public class DaoResult {
    public static final int OK = 0;
    public static final int ERR_PERMISSION = 1;
    public static final int ERR_NOT_FOUND = 2;
    public static final int ERR_INVALID = 3;
    public static final int ERR_CONFLICT = 4;
    public static final int ERR_UNKNOWN = 99;

    private static final ThreadLocal<Integer> lastCode = new ThreadLocal<>();
    private static final ThreadLocal<String> lastMessage = new ThreadLocal<>();

    public static void setError(int code, String msg) {
        lastCode.set(code);
        lastMessage.set(msg == null ? "" : msg);
    }

    public static int getCode() { Integer v = lastCode.get(); return v == null ? OK : v; }
    public static String getMessage() { String m = lastMessage.get(); return m == null ? "" : m; }
    public static void clear() { lastCode.remove(); lastMessage.remove(); }
}
