package com.anurag.shopper;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class ShoppingListManager {
	private Context context;
	private DatabaseHandler dbh;

	/** CONSTRUCTOR */
	public ShoppingListManager(Context context) {
		this.context = context;
		this.dbh = DatabaseHandler.getInstance(context);
	}
	

	/** PRIVATE FUNCTIONS */

	
	/** PUBLIC FUNCTIONS */
    public int insertShoppingItemIntoDB(ShoppingItem si){
        return dbh.addShoppingItem(si);
    }

	public ShoppingItem getShoppingItemFromDB(int id) {
		return dbh.getShoppingItem(id);

	}
	
	public ArrayList<ShoppingItem> getAllShoppingItemsFromDB() {
		return dbh.getAllShoppingItems();
	}

	public void deleteShoppingItem(int id) {
        //remove shoppingItem from DB
        dbh.deleteShoppingItem(id);
	}

    public void deleteAllShoppingItems() {
        dbh.deleteAllShoppingItems();
    }
	


	public void showNumberOfShoppingItems() {
		int numRem = dbh.getShoppingItemsCount();
		
		String msg;
		
		if (numRem == 1) {
			msg = "There is " + numRem + " shoppingItem in the database";
		}
		else {
			msg = "There are " + numRem + " shoppingItems in the database";
		}
		
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public void updateShoppingItem(ShoppingItem si) {
		dbh.updateShoppingItem(si);
	}

}
