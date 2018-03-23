package com.stephen.lab.util.Progress;

import java.util.Hashtable;

/**
 * Created by stephen on 2018/3/19.
 */
public class Progress {
    private static Hashtable<Object, Object> table = new Hashtable<>();
    private static Hashtable<Object, Object> info = new Hashtable<>();

    public static void put(Object key, Object value) {
        table.put(key, value);
    }

    public static Object get(Object key) {
        return table.get(key);
    }

    public static void putInfo(Object key, Object value) {
        info.put(key, value);
    }

    public static Object getInfo(Object key) {
        return info.get(key);
    }

    public static Object remove(Object key) {
        return table.remove(key);
    }

    public static Object removeInfo(Object key) {
        return info.remove(key);
    }

}
