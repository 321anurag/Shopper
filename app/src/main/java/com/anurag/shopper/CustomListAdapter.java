package com.anurag.shopper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<ShoppingItem> {

    private final Context context;
    private final ArrayList<ShoppingItem> shoppingListItems;
    private ShoppingListManager slm;


    public CustomListAdapter(Activity context, ArrayList<ShoppingItem> shoppingListItems) {
        super(context, R.layout.list_view_item, shoppingListItems);

        this.context            = context;
        this.shoppingListItems  = shoppingListItems;

        slm = new ShoppingListManager(context);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_view_item, parent, false);

        TextView shoppingItemNameTextView       = (TextView)rowView.findViewById(R.id.textViewItemName);
        ImageButton shoppingItemRemoveButton    = (ImageButton)rowView.findViewById(R.id.imageButtonRemove);
        ImageButton shoppingItemEditButton      = (ImageButton)rowView.findViewById(R.id.imageButtonEdit);
        ImageButton shoppingItemCartButton      = (ImageButton)rowView.findViewById(R.id.imageButtonInCart);

        shoppingItemNameTextView.setText(shoppingListItems.get(position).getName());

        //special actions for items that are already in cart
        if (shoppingListItems.get(position).getInCart() == 1) {
            shoppingItemNameTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
            rowView.setBackgroundColor(context.getResources().getColor(R.color.material_grey_300));
            shoppingItemRemoveButton.setVisibility(View.INVISIBLE);
            shoppingItemEditButton.setVisibility(View.INVISIBLE);
            shoppingItemCartButton.setImageResource(R.drawable.cart_button_remove);
        }

        //set on click listeners for buttons
        shoppingItemRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent removeSingleItemIntent = new Intent();
                removeSingleItemIntent.setAction("com.anurag.shopper.REMOVE_SINGLE_ITEM");
                removeSingleItemIntent.putExtra("item_id", shoppingListItems.get(position).getId());
                context.sendBroadcast(removeSingleItemIntent);

            }
        });

        shoppingItemEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editSingleItemIntent = new Intent();
                editSingleItemIntent.setAction("com.anurag.shopper.EDIT_SINGLE_ITEM");
                editSingleItemIntent.putExtra("item_id", shoppingListItems.get(position).getId());
                context.sendBroadcast(editSingleItemIntent);
            }
        });

        shoppingItemCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartSingleItemIntent = new Intent();
                cartSingleItemIntent.setAction("com.anurag.shopper.CART_SINGLE_ITEM");
                cartSingleItemIntent.putExtra("item_id", shoppingListItems.get(position).getId());
                context.sendBroadcast(cartSingleItemIntent);
            }
        });

        return rowView;
    }
}
