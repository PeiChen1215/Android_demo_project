package com.example.android_development.util;

/**
 * DAO/数据层返回码与线程内错误信息容器。
 * <p>
 * 某些 DAO 方法使用返回值表达“成功/失败”，并通过本类保存最近一次错误码与错误信息。
 * 采用 ThreadLocal 是为了避免不同线程之间相互污染（例如 UI 线程与后台线程并行访问）。
 * </p>
 */
public class DaoResult {
    /** 成功 */
    public static final int OK = 0;
    /** 权限不足 */
    public static final int ERR_PERMISSION = 1;
    /** 数据不存在 */
    public static final int ERR_NOT_FOUND = 2;
    /** 参数或状态非法 */
    public static final int ERR_INVALID = 3;
    /** 冲突（例如重复/并发导致的冲突） */
    public static final int ERR_CONFLICT = 4;
    /** 未知错误 */
    public static final int ERR_UNKNOWN = 99;

    private static final ThreadLocal<Integer> lastCode = new ThreadLocal<>();
    private static final ThreadLocal<String> lastMessage = new ThreadLocal<>();

    /**
     * 设置当前线程最近一次错误。
     *
     * @param code 错误码（建议使用本类常量）
     * @param msg  错误信息（可为空）
     */
    public static void setError(int code, String msg) {
        lastCode.set(code);
        lastMessage.set(msg == null ? "" : msg);
    }

    /**
     * 获取当前线程最近一次错误码。
     *
     * @return 错误码；如果未设置则为 {@link #OK}
     */
    public static int getCode() { Integer v = lastCode.get(); return v == null ? OK : v; }

    /**
     * 获取当前线程最近一次错误信息。
     *
     * @return 错误信息；如果未设置则返回空字符串
     */
    public static String getMessage() { String m = lastMessage.get(); return m == null ? "" : m; }

    /**
     * 清除当前线程的错误信息。
     */
    public static void clear() { lastCode.remove(); lastMessage.remove(); }
}
