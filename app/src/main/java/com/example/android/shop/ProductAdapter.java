package com.example.android.shop;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.shop.data.ProductContract.ShopEntry;


// Created by Daria Kalashnikova 11.07.2017

class ProductAdapter extends CursorAdapter {

    int mQuantity;
    Context mContext;
    private TextView quantity;
    private Button sale;

    ProductAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        quantity = (TextView) view.findViewById(R.id.quantity);
        sale = (Button) view.findViewById(R.id.sale_button);

        // The columns of product info that we will see in the list
        int indexName = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_NAME);
        int indexPrice = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_PRICE);
        final int indexQuantity = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_QUANTITY);

        String mName = cursor.getString(indexName);
        int mPrice = cursor.getInt(indexPrice);
        mQuantity = cursor.getInt(indexQuantity);

        name.setText(mName);
        price.setText(context.getString(R.string.priceIs) + " " + String.valueOf(mPrice));
        quantity.setText(String.valueOf(mQuantity));

        if (mQuantity == 0) {
            sale.setBackgroundColor(ContextCompat.getColor(context, R.color.sale_button_sold_color));
            sale.setEnabled(false);
        }
        else{
            sale.setBackgroundColor(ContextCompat.getColor(context, R.color.sale_button_color));
            sale.setEnabled(true);
        }

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = cursor.getLong(cursor.getColumnIndex(ShopEntry._ID));
                if (mQuantity > 0) {
                decreaseQuantity(id);
                }
            }
        });
    }

    private void decreaseQuantity(Long id) {
        mQuantity --;
        Uri mUri = ContentUris.withAppendedId(ShopEntry.CONTENT_URI, id);
        final ContentValues values = new ContentValues();
        values.put(ShopEntry.COLUMN_PRODUCT_QUANTITY, mQuantity);
        mContext.getContentResolver().update(mUri, values, null, null);
    }
}
