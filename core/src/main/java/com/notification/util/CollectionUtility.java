package com.notification.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtility {

    public static <T> List<List<T>> shardList(List<T> list, int k) {
        List<List<T>> shardedList = new ArrayList<>();
        for(int i=0;i<list.size();i+=k) {
            int endIndex = Math.min(i+k, list.size());
            shardedList.add(new ArrayList<>(list.subList(i, endIndex)));
        }
        return shardedList;
    }
}
