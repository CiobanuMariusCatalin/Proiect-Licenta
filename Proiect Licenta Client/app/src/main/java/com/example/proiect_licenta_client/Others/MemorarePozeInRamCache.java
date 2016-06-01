package com.example.proiect_licenta_client.Others;



import android.graphics.Bitmap;

import android.util.LruCache;



//PE MOMENT NU O FOLOSESC IN PROGRAM AM INCERCAT CEVA CU EA DAR AM RAMAS LA MEMORIE CACHE PE STORAGE
public class MemorarePozeInRamCache {
    public static LruCache<String, Bitmap> mMemoryCache = null;



    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB


/*
    public static void initiere() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }*/





}

