package com.example.shop;

import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(itemName, item.itemName) &&
                Objects.equals(price, item.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemName, price);
    }
}