package com.stephen.lab.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
    public static <T> List<T> oneItemToList(T item) {
        List<T> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    public static String ListToString(List<String> entityType, String s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String item : entityType) {
            stringBuilder.append(item);
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }
}
