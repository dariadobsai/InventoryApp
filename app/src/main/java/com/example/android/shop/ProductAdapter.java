package com.example.android.shop;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.shop.data.ProductContract.ShopEntry;

// Created by Daria Kalashnikova 11.07.2017

public class ProductAdapter extends CursorAdapter {

    private Context mContext;
    public ProductAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.mContext = context;
    }

    private String mQuantity;
    private TextView quantity;
    private long id;

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        quantity = (TextView) view.findViewById(R.id.quantity);
        Button sale = (Button) view.findViewById(R.id.sale_button);

        id = cursor.getLong(cursor.getColumnIndex(ShopEntry._ID));
        int indexName = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_NAME);
        int indexPrice = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_PRICE);
        int indexQuantity = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_QUANTITY);

        final String mName = cursor.getString(indexName);
        String mPrice = cursor.getString(indexPrice);
        mQuantity = cursor.getString(indexQuantity);

        name.setText(mName);
        price.setText(mPrice);
        quantity.setText(mQuantity);

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();
            }
        });
    }

    private void decreaseQuantity() {
        int currentQuantity = Integer.parseInt(mQuantity);
        if (currentQuantity > 0) {
            currentQuantity -= 1;
            Uri mUri = ContentUris.withAppendedId(ShopEntry.CONTENT_URI, id);
            final ContentValues values = new ContentValues();
            values.put(ShopEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity);
            mContext.getContentResolver().update(mUri, values, null, null);
            quantity.setText(Integer.toString(currentQuantity));
        }
    }
}