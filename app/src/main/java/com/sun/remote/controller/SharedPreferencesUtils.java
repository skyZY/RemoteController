package com.sun.remote.controller;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/*
 * =====================================================================================
 * Summary:
 *
 * File: SharedPreferencesUtils.java
 * Author: Yanpeng.Sun
 * Create: 2019/6/15 12:57
 * =====================================================================================
 */
public class SharedPreferencesUtils{

    public static final String name = "Screen";
    private static SharedPreferencesUtils instance;

    public final static String KEY_WIDTH = "width";
    public final static String KEY_HEIGHT = "height";
    public final static String KEY_IP = "ip";

    public static SharedPreferencesUtils getInstance() {
        if (instance == null) {
            synchronized (SharedPreferencesUtils.class) {
                if (instance == null) {
                    instance = new SharedPreferencesUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 保存数据，修改数据
     *
     * @param key
     * @param value
     * @param <V>
     */
    public <V> void setValue(@NonNull String key, V value) {
        SharedPreferences sp = RemoteApplication.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }
        editor.commit();
    }

    /**
     * 读取数据
     *
     * @param key
     * @param defaultValue
     * @param <V>
     * @return
     */
    public <V> V getValue(@NonNull String key, V defaultValue) {
        SharedPreferences sp = RemoteApplication.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE);
        Object value = defaultValue;
        if (defaultValue instanceof String) {
            value = sp.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            value = sp.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Long) {
            value = sp.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            value = sp.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Float) {
            value = sp.getFloat(key, (Float) defaultValue);
        }
        return (V) value;
    }

    /**
     * 清空数据
     */
    public void clearData() {
        SharedPreferences.Editor editor = RemoteApplication.getInstance().getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

}
