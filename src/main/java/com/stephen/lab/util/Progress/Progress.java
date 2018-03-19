package com.stephen.lab.util.Progress;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by stephen on 2018/3/19.
 */
public class Progress {
    private static Hashtable<Object, Object> table = new Hashtable<>();

    public static void put(Object key, Object value) {
        table.put(key, value);
    }

    public static Object get(Object key) {
        return table.get(key);
    }

    public static Object remove(Object key) {
        return table.remove(key);
    }
}
