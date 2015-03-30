package com.anurag.shopper;

/**
 * Created by kamil on 3/8/15.
 */
public class ShoppingItem {

    private int id;
    private String name;
    private int inCart;

    public ShoppingItem(String name, int inCart) {
        this.name = name;
        this.inCart = inCart;
    }

    public ShoppingItem(int id, String name, int inCart) {
        this.id = id;
        this.name = name;
        this.inCart = inCart;
    }

    //GETTERS
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getInCart() {
        return inCart;
    }

    //SETTERS
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInCart(int inCart) {
        this.inCart = inCart;
    }

    @Override
    public String toString() {
        return "ShoppingItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", inCart=" + inCart +
                '}';
    }
}
