package com.anurag.shopper;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;


public class ActivityMain extends ActionBarActivity {
    //TODO - add advertisement
    //TODO - make sure that you use test ad id for testing
    //TODO - replace test ad id with final ad id

    private AutoCompleteTextView autoCompleteTextViewItemName;
    private ImageButton imageButtonAdd;
    private ListView listViewItems;
    private ArrayList<ShoppingItem> itemsInListArray;
    private CustomListAdapter itemsInListAdapter;
    private ShoppingListManager slm;
    private Context applicationContext;
    private ShoppingItem latestItem;
    private String removeSingleItemintentAction;
    private String editSingleItemintentAction;
    private String cartSingleItemintentAction;
    private IntentFilter removeSingleItemintentFilter;
    private IntentFilter editSingleItemintentFilter;
    private IntentFilter cartSingleItemintentFilter;
    private BroadcastReceiver customActionsReceiver;
    private AdView adViewBannerBottom;
    private int toastOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** ASSIGN VALUES TO OBJECTS AND VARS */
        autoCompleteTextViewItemName                = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewItemName);
        imageButtonAdd                              = (ImageButton)findViewById(R.id.imageButtonAdd);
        listViewItems                               = (ListView)findViewById(R.id.listViewItems);

        applicationContext                          = getApplicationContext();
        slm                                         = new ShoppingListManager(applicationContext);
        itemsInListArray                            = slm.getAllShoppingItemsFromDB();
        toastOffset                                 = getResources().getDimensionPixelSize(R.dimen.m_spacing);
        itemsInListAdapter                          = new CustomListAdapter(this, itemsInListArray);
        removeSingleItemintentAction                = "com.anurag.shopper.REMOVE_SINGLE_ITEM";
        editSingleItemintentAction                  = "com.anurag.shopper.EDIT_SINGLE_ITEM";
        cartSingleItemintentAction                  = "com.anurag.shopper.CART_SINGLE_ITEM";
        removeSingleItemintentFilter                = new IntentFilter(removeSingleItemintentAction);
        editSingleItemintentFilter                  = new IntentFilter(editSingleItemintentAction);
        cartSingleItemintentFilter                  = new IntentFilter(cartSingleItemintentAction);
        String[] providedListOfItems                = getResources().getStringArray(R.array.shopping_items);
        ArrayAdapter<String> providedItemsAdapter   = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, providedListOfItems);



        //set up broadcast receivers
        customActionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() != null) {
                    Bundle intentExtras = intent.getExtras();

                    int itemId = intentExtras.getInt("item_id");

                    //ACTION FOR REMOVE SINGLE ITEM
                    if (intent.getAction().equals(removeSingleItemintentAction)) {
                        removeSingleItem(itemId);
                    }
                    //ACTION FOR EDIT SINGLE ITEM
                    else if (intent.getAction().equals(editSingleItemintentAction)) {
                        editSingleItem(itemId);
                    }
                    //ACTION FOR CART SINGLE ITEM
                    else if (intent.getAction().equals(cartSingleItemintentAction)) {
                        cartSingleItem(itemId);
                    }
                }
            }
        };

        //enter key or center button on dpad will do the same as ok button
        autoCompleteTextViewItemName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            addItemToTheList();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });


        //assign adapter to listview
        listViewItems.setAdapter(itemsInListAdapter);

        /** if cart empty */
        addEmptyListMessage();

        //assign adapter with hints to input field
        autoCompleteTextViewItemName.setAdapter(providedItemsAdapter);

        /** SET ON CLICK LISTENERS */

        //add button click listener
        imageButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToTheList();
            }
        });

        //item in the list click listener
        autoCompleteTextViewItemName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addItemToTheList();
            }
        });

    }

    private void addItemToTheList() {
        //make sure that text was entered
        if (!TextUtils.isEmpty(autoCompleteTextViewItemName.getText())) {
            showToast(autoCompleteTextViewItemName.getText().toString() + " added to the list");

            latestItem = new ShoppingItem(autoCompleteTextViewItemName.getText().toString(), 0);

            //retrieve id after saving in DB so that item in array has id which can be used for deleting,editing, etc
            int latestItemId = slm.insertShoppingItemIntoDB(latestItem);
            latestItem.setId(latestItemId);


            if (isAnythingInCart()) {
                hardRefreshListOfItems();
            }
            else {
                itemsInListArray.add(latestItem);
                softRefreshListOfItems();
            }

            autoCompleteTextViewItemName.setText("");

        }
        else {
            showToast(getResources().getString(R.string.empty_input_error_message));
        }
    }

    private void removeSingleItem(final int itemId) {
        final ShoppingItem si = slm.getShoppingItemFromDB(itemId);

        final AlertDialog.Builder promptDialog = new AlertDialog.Builder(this);
        promptDialog
            .setMessage(getResources().getString(R.string.do_you_want_to_remove) + " \'" + si.getName() + "\' " + getResources().getString(R.string.from_the_list))
            .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeSingleItemWorker(itemId);
                    showToast("\'" + si.getName() + "\' " + getResources().getString(R.string.removed_from_the_list));
                }
            })
            .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create().show();
    }

    private void removeSingleItemWorker(int itemId) {
        //remove item from db
        slm.deleteShoppingItem(itemId);

        //make sure to remove it from array so that we can use notifyDataSetChanged of the array adapter
        itemsInListArray.remove(findItemIndex(itemId));

        softRefreshListOfItems();
    }

    private void editSingleItem(final int itemId) {
        final ShoppingItem si = slm.getShoppingItemFromDB(itemId);

        //check if field is empty, if not ask user if they want to replace current text with edited item
        //otherwise place text in the field
        if (!TextUtils.isEmpty(autoCompleteTextViewItemName.getText())) {
            //alert user that field not empty
            final AlertDialog.Builder promptDialog = new AlertDialog.Builder(this);
            promptDialog
                    //.setMessage(getResources().getString(R.string.item_name_not_empty_warning))
                    .setMessage("You already entered \'"+ autoCompleteTextViewItemName.getText() +"\' but didn't save it yet. Do you want to save it before editing \'"+ si.getName() +"\'")
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addItemToTheList();
                            editSingleItemWorker(itemId);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editSingleItemWorker(itemId);
                        }
                    }).create().show();
        }
        //input field was empty, we can proceed with editing
        else {
            editSingleItemWorker(itemId);
        }
    }

    private void editSingleItemWorker(int itemId) {
        ShoppingItem si = slm.getShoppingItemFromDB(itemId);
        autoCompleteTextViewItemName.setText(si.getName());

        //move cursor to the end of text
        autoCompleteTextViewItemName.setSelection(autoCompleteTextViewItemName.length());

        removeSingleItemWorker(itemId);
    }

    private void cartSingleItem(int itemId) {
        ShoppingItem si = slm.getShoppingItemFromDB(itemId);

        //LOGIC FOR MOVING TO CART
        if (si.getInCart() == 0) {
            si.setInCart(1);

            slm.updateShoppingItem(si);

            //rebuild array of items based on new sorting to put items in cart at the bottom
            itemsInListArray.clear();
            itemsInListArray    = slm.getAllShoppingItemsFromDB();
            itemsInListAdapter  = new CustomListAdapter(this, itemsInListArray);
            listViewItems.setAdapter(itemsInListAdapter);

            showToast(" \'" + si.getName() + "\' " + getResources().getString(R.string.moved_to_cart));
        }
        //LOGIC FOR MOVING OUT OF CART
        else {
            si.setInCart(0);

            slm.updateShoppingItem(si);

            //rebuild array of items based on new sorting to put items in cart at the bottom
            hardRefreshListOfItems();

            showToast(" \'" + si.getName() + "\' " + getResources().getString(R.string.removed_from_cart));
        }
    }

    private void removeAllItems() {
        //check if list not empty
        if (itemsInListArray.size() > 0) {
            final AlertDialog.Builder promptDialog = new AlertDialog.Builder(this);
            promptDialog
                    .setMessage(getResources().getString(R.string.sure_to_remove_all_items))
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //remove all items
                            itemsInListArray.clear();

                            //delete al items from db
                            slm.deleteAllShoppingItems();

                            //show empty list message
                            addEmptyListMessage();

                            //refresh list
                            softRefreshListOfItems();

                            showToast(getResources().getString(R.string.items_removed));
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
        }
        //list is empty, display toast
        else {
            showToast(getResources().getString(R.string.no_items_in_list));
        }


    }

    private void removeItemsInCart() {
        //check if there are items in cart if yes show prompt otherwise show toast
        if (isAnythingInCart()) {
            final AlertDialog.Builder promptDialog = new AlertDialog.Builder(this);
            promptDialog
                    .setMessage(getResources().getString(R.string.sure_to_remove_cart_items))
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete cart items from db
                            for (int i = 0; i < itemsInListArray.size(); i++) {
                                if (itemsInListArray.get(i).getInCart() == 1) {
                                    slm.deleteShoppingItem(itemsInListArray.get(i).getId());
                                }
                            }

                            hardRefreshListOfItems();

                            //show empty list message
                            addEmptyListMessage();

                            showToast(getResources().getString(R.string.items_removed));
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
        }
        //show toast that there are no items moved to cart yet
        else {
            showToast(getResources().getString(R.string.no_items_in_cart_yet));
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_remove_all_items) {
            removeAllItems();
            return true;
        }
        else if (id == R.id.menu_remove_cart_items) {
            removeItemsInCart();
            return true;
        }
        else if (id == R.id.menu_send_list) {
            sendList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendList() {

        //make sure that list is not empty
        if (itemsInListArray.size() > 0) {
            //build the shareable list string
            String shareableList = getResources().getString(R.string.please_buy);
            int numberOfShareableItems = 0;

            for (int i = 0; i < itemsInListArray.size(); i++) {
                if (itemsInListArray.get(i).getInCart() == 0) {
                    //increment number of shareable items
                    numberOfShareableItems++;
                    shareableList = shareableList.concat("\n- " + itemsInListArray.get(i).getName());
                }
            }

            //there is at least one item to share
            if (numberOfShareableItems > 0) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareableList);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.send_list_using)));
            }
            else {
                showToast(getResources().getString(R.string.list_empty_or_all_items_in_cart));
            }
        }
        //list is empty
        else {
            showToast(getResources().getString(R.string.list_empty_message));
        }
    }


    private void addEmptyListMessage() {
        //change listview's view if empty
        listViewItems.setEmptyView(findViewById(R.id.empty_list_view));
    }

    private void softRefreshListOfItems() {
        itemsInListAdapter.notifyDataSetChanged();
    }

    private void hardRefreshListOfItems() {
        itemsInListArray.clear();
        itemsInListArray    = slm.getAllShoppingItemsFromDB();
        itemsInListAdapter  = new CustomListAdapter(this, itemsInListArray);
        listViewItems.setAdapter(itemsInListAdapter);
    }

    private int findItemIndex(int itemId){
        int foundIndex = -1;

        for (int i = 0; i < itemsInListArray.size(); i++) {
            if (itemId == itemsInListArray.get(i).getId()) {
                foundIndex = i;
                break;
            }
        }
        return foundIndex;
    }

    private boolean isAnythingInCart() {
        boolean itemInCart = false;

        for (int i = 0; i < itemsInListArray.size(); i++) {
            if (itemsInListArray.get(i).getInCart() == 1) {
                itemInCart = true;
                break;
            }
        }

        return itemInCart;
    }


    private void showToast(String text) {
        Toast toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT);

        toast.show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        this.registerReceiver(customActionsReceiver, removeSingleItemintentFilter);
        this.registerReceiver(customActionsReceiver, editSingleItemintentFilter);
        this.registerReceiver(customActionsReceiver, cartSingleItemintentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(customActionsReceiver);
    }
}
