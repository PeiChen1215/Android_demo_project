package com.example.android_development.database;

import com.example.android_development.util.Constants;

public class DbContract {

    // 用户表创建SQL
    public static final String SQL_CREATE_TABLE_USERS =
            "CREATE TABLE " + Constants.TABLE_USERS + " (" +
                    Constants.COLUMN_USER_ID + " TEXT PRIMARY KEY," +
                    Constants.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
                    Constants.COLUMN_PASSWORD + " TEXT NOT NULL," +
                    Constants.COLUMN_ROLE + " TEXT NOT NULL," +
                    Constants.COLUMN_FULL_NAME + " TEXT," +
                    Constants.COLUMN_PHONE + " TEXT," +
                    Constants.COLUMN_EMAIL + " TEXT," +
                    Constants.COLUMN_CREATED_AT + " INTEGER" +
                    ")";

    // 用户表删除SQL
    public static final String SQL_DROP_TABLE_USERS =
            "DROP TABLE IF EXISTS " + Constants.TABLE_USERS;
}