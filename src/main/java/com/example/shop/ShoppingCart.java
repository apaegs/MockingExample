package com.example.shop;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<Item> items = new ArrayList<>();

    public void addItem(Item name) {
        items.add(name);
    }

    public List<Item> getItems() {
        return items;
    }
}

