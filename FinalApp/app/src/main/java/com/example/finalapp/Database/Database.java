package com.example.finalapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.finalapp.Model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "EatItDB.db";
    private static final int DB_VER = 1;
    private static final String KEY_ID = "ID";
    private static final String KEY_PRODUCTID = "ProductId";
    private static final String KEY_PRODUCTNAME = "ProductName";
    private static final String KEY_QUANTITY = "Quantity";
    private static final String KEY_PRICE = "Price";
    private static final String KEY_DISCOUNT = "Discount";
    private static final String KEY_FOODID = "FoodId";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + "OrderDetail" + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PRODUCTID + " TEXT,"+KEY_PRODUCTNAME+" TEXT,"+KEY_QUANTITY+" TEXT,"+KEY_PRICE+" TEXT,"+KEY_DISCOUNT+" TEXT"+")";
    private static final String SQL_CREATE_ENTRIES2 = "CREATE TABLE " + "Favorites" + " (" + KEY_FOODID + " TEXT UNIQUE)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DB_NAME;
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }
    public List<Order> getCarts(){
        List<Order> orderList = new ArrayList<>();
        String query = "SELECT * FROM OrderDetail" ;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();
        while (cursor.isAfterLast()==false){
            Order order = new Order(cursor.getString(cursor.getColumnIndex("ProductId")),cursor.getString(cursor.getColumnIndex("ProductName")),cursor.getString(cursor.getColumnIndex("Quantity")),cursor.getString(cursor.getColumnIndex("Price")),cursor.getString(cursor.getColumnIndex("Discount")));
            orderList.add(order);
            cursor.moveToNext();
        }
        return orderList;
    }

    public void addToCart(Order order){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCTID,order.getProductId());
        values.put(KEY_PRODUCTNAME,order.getProductName());
        values.put(KEY_QUANTITY,order.getQuantity());
        values.put(KEY_PRICE,order.getPrice());
        values.put(KEY_DISCOUNT,order.getDiscount());
        db.insert("OrderDetail",null,values);
        db.close();
    }

    public void cleanCart(){
        SQLiteDatabase db= getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail");
        db.execSQL(query);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void addToFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(FoodId) VALUES('%S');", foodId);
        db.execSQL(query);
    }

    public void removeFromFavorites(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId='%s';", foodId);
        db.execSQL(query);
    }

    public boolean isFavorite(String foodId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId='%s';", foodId);
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

}
