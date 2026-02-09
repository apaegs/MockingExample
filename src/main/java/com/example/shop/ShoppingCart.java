package com.example.shop;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private Map<Item, Integer> items = new HashMap<>();
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    public void addItem(Item item) {
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Kvantitet måste vara positiv");
        }
        items.merge(item, item.getQuantity(), Integer::sum);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void updateQuantity(Item item, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Kvantitet måste vara positiv");
        }
        if (!items.containsKey(item)) {
            throw new IllegalArgumentException("Produkten finns inte i varukorgen");
        }
        items.replace(item, newQuantity);
    }

    public List<Item> getItems() {
        return items.entrySet().stream()
                .map(entry -> new Item(entry.getKey().getItemName(),
                        entry.getKey().getPrice(),
                        entry.getValue()))
                .toList();
    }

    public void applyDiscount(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rabatt kan inte vara negativ");
        }
        if (percentage.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Rabatt måste vara större än 0");
        }
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Rabatt kan inte vara över 100%");
        }
        this.discountPercentage = percentage;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal subtotal = items.entrySet().stream()
                .map(entry -> entry.getKey().getPrice()
                        .multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = subtotal.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        }

        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }
}