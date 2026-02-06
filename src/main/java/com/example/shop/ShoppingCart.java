package com.example.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private List<Item> items = new ArrayList<>();
    private BigDecimal discountPercentage;

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal subtotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = subtotal.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return subtotal.subtract(discount);
        }

        return subtotal;
    }

    public void applyDiscount(BigDecimal precentage) {
        this.discountPercentage = precentage;
    }
}

