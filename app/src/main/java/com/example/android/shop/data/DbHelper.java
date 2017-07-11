package com.example.android.shop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.shop.data.ProductContract.ShopEntry;

// Created by Daria Kalashnikova 11.07.2017

public class DbHelper extends SQLiteOpenHelper {

    // Name of the database file
    private static final String DATABASE_NAME = "products.db";

    // Database version
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This is called when the database is created for the first time.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ShopEntry.TABLE_NAME + " ("
                + ShopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ShopEntry.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL, "
                + ShopEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ShopEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + ShopEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ShopEntry.COLUMN_PRODUCT_EMAIL + " TEXT NOT NULL );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    // This is called when the database needs to be upgraded.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
