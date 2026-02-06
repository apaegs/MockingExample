package com.example.shop;

import java.math.BigDecimal;

public class Item {

    private String itemName;
    private BigDecimal price;
    private int quantity;
    public Item(String itemName, BigDecimal price, int quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}