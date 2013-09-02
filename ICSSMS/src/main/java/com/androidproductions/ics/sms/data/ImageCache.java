package com.androidproductions.ics.sms.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import com.androidproductions.ics.sms.R;

public class ImageCache {
    private static ImageCache mImageCache;
    private static Context mContext;
    private final LruCache<Long, Bitmap> cache;

    private ImageCache(final int cacheSize)
    {
        cache = new LruCache<Long, Bitmap>(cacheSize);
    }

    public static Bitmap getItem(final Long id)
    {
        return mImageCache.cache.get(id);
    }

    public static Bitmap getDefault()
    {
        final Bitmap item = mImageCache.cache.get(0L);
        if (item == null)
            putItem(0L, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_contact_picture));
        return mImageCache.cache.get(0L);
    }

    public static void putItem(final Long id, final Bitmap bmp)
    {
        mImageCache.cache.put(id, bmp);
    }

    public static void initInstance(final int cacheSize, final Context context)
    {
        mImageCache = new ImageCache(cacheSize);
        mContext = context;
        putItem(0L, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_contact_picture));
    }
}