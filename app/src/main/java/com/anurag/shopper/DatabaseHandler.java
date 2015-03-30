package com.anurag.shopper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class DatabaseHandler extends SQLiteOpenHelper {
	
	private static DatabaseHandler sInstance = null;
	
	//database version
	private static final int DATABASE_VERSION = 1;
	
	//database name
	private static final String DATABASE_NAME = "shoppingListDB";
	
	//reminders table name
	private static final String TABLE_SHOPPING_ITEMS = "shoppingItems";
	
	//reminders table column names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_IN_CART = "inCart";

	// Use the application context, which will ensure that you 
    // don't accidentally leak an Activity's context.
    // See this article for more information: http://bit.ly/6LRzfx
	public static DatabaseHandler getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new DatabaseHandler(context.getApplicationContext());
		}
		return sInstance;
	}

	//constructor
	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	//create database and table
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_SHOPPING_ITEMS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
				+ KEY_IN_CART + " INTEGER" + ")";
		db.execSQL(CREATE_REMINDERS_TABLE);
	}
	
	//upgrade database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		if (oldVersion == 1) {
			upgradeFrom1to2(db);
			oldVersion = 2;
		}
		
		if (oldVersion == 2) {
			upgradeFrom2to3(db);
			oldVersion = 3;
		}


	}
	
	private void upgradeFrom1to2(SQLiteDatabase db){
		//EXAMPLE FROM REMINDER APP
		//String ALTER_REMINDERS_TABLE_ADD_COLUMN_RECURRENCE 			= "ALTER TABLE " + TABLE_SHOPPING_ITEMS + " ADD COLUMN " + KEY_RECURRENCE_POSITION + " INTEGER DEFAULT 0";
		//String ALTER_REMINDERS_TABLE_ADD_COLUMN_ORIGINAL_TIMESTAMP 	= "ALTER TABLE " + TABLE_SHOPPING_ITEMS + " ADD COLUMN " + KEY_ORIGINAL_TIMESTAMP + " INTEGER DEFAULT 0";
		//db.execSQL(ALTER_REMINDERS_TABLE_ADD_COLUMN_RECURRENCE);
		//db.execSQL(ALTER_REMINDERS_TABLE_ADD_COLUMN_ORIGINAL_TIMESTAMP);
	}
	
	private void upgradeFrom2to3(SQLiteDatabase db){
		//for future use
	}


	
	//add new reminder
	public int addShoppingItem(ShoppingItem shoppingItem) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, shoppingItem.getName());
		values.put(KEY_IN_CART, shoppingItem.getInCart());

		//create long that will be returned
		int insertedRowId;
		
		//insert row
		insertedRowId = (int) db.insert(TABLE_SHOPPING_ITEMS, null, values);
		db.close();
		
		return insertedRowId;		
	}
	
	//get single reminder
	public ShoppingItem getShoppingItem(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_SHOPPING_ITEMS, new String[] { KEY_ID, KEY_NAME,
				KEY_IN_CART }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);

		if(cursor != null && cursor.moveToFirst()) {
			/*0=id
             *1=name
             *2=is in cart
             */

                ShoppingItem shoppingItem = new ShoppingItem(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), Integer.parseInt(cursor.getString(2)));

            cursor.close();
            db.close();

            return shoppingItem;
		}
        //prevent force close on java null pointer exception
        return new ShoppingItem(0, "", 0);
	} 
	
	//get all shopping items
	public ArrayList<ShoppingItem> getAllShoppingItems() {
		ArrayList<ShoppingItem> shoppingItemsList = new ArrayList<>();
		
		//select all query, SORT IT FIRST BY STATUS (enabled first) then by DATE
		String selectQuery = "SELECT * FROM " + TABLE_SHOPPING_ITEMS + " ORDER BY " + KEY_IN_CART + " ASC, " + KEY_ID + " ASC";
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		//looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				
				ShoppingItem shoppingItem = new ShoppingItem(Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), Integer.parseInt(cursor.getString(2)));
				
				//insert reminder into the list
                shoppingItemsList.add(shoppingItem);
			}
			while (cursor.moveToNext());
		}
		
		cursor.close();
		db.close();
		
		return shoppingItemsList;
	}
	
	//get reminders count
	public int getShoppingItemsCount() {
		String countQuery = "SELECT * FROM " + TABLE_SHOPPING_ITEMS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		
		int count = cursor.getCount();
		
		cursor.close();
		db.close();
		
		return count;
	}
	
	//update single shopping item
	public int updateShoppingItem(ShoppingItem shoppingItem) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, shoppingItem.getName());
		values.put(KEY_IN_CART, shoppingItem.getInCart());
		
		
		//update row
		int affectedRows = db.update(TABLE_SHOPPING_ITEMS, values, KEY_ID + "=?",
				new String[] { String.valueOf(shoppingItem.getId()) });
		
		db.close();
		
		return affectedRows;
	}
	
	//delete single shopping item by passing item object
	public void deleteShoppingItem(ShoppingItem shoppingItem) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SHOPPING_ITEMS, KEY_ID + "=?",
				new String[] { String.valueOf(shoppingItem.getId()) });
		db.close();
	}
	
	//delete single shopping item by passing id
	public void deleteShoppingItem(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SHOPPING_ITEMS, KEY_ID + "=?",
				new String[] { String.valueOf(id) });
		db.close();
	}

    //delete single shopping item by passing id
    public void deleteAllShoppingItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SHOPPING_ITEMS, null, null);
        db.close();
    }

}
