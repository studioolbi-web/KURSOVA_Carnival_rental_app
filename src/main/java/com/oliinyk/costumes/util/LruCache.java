package com.oliinyk.costumes.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Простий LRU (Least Recently Used) кеш на базі LinkedHashMap.
 *
 * @param <K> тип ключа
 * @param <V> тип значення
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LruCache(int capacity) {
        // initial capacity, load factor, accessOrder=true
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
