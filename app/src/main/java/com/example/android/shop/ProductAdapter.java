package com.example.android.shop;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.shop.data.ProductContract.ShopEntry;

public class ProductAdapter extends CursorAdapter {
    public ProductAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price= (TextView)view.findViewById(R.id.price);
        TextView quantity= (TextView)view.findViewById(R.id.quantity);

        int indexName = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_NAME);
        int indexPrice = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_PRICE);
        int indexQuantity = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_QUANTITY);

        String mName = cursor.getString(indexName);
        String mPrice = cursor.getString(indexPrice);
        String mQuantity = cursor.getString(indexQuantity);

        name.setText(mName);
        price.setText(mPrice);
        quantity.setText(mQuantity);
    }
}
