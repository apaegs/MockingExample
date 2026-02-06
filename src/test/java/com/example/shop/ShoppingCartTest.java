package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    void shouldAddItemToCart() {
        // ARRANGE
        Item item = new Item("T-Shirt", BigDecimal.valueOf(150), 1);

        // ACT
        cart.addItem(item);

        // ASSERT
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems()).contains(item);
    }

    @Test
    void shouldRemoveItemFromCart() {
        // ARRANGE
        Item item = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
        cart.addItem(item);

        // ACT
        cart.removeItem(item);

        // ASSERT
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void shouldCalculateTotalPrice() {
        // ARRANGE
        Item item1 = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
        Item item2 = new Item("Sweater", BigDecimal.valueOf(200), 2);
        cart.addItem(item1);
        cart.addItem(item2);

        // ACT
        BigDecimal total = cart.getTotalPrice();

        // ASSERT
        assertThat(total).isEqualTo(BigDecimal.valueOf(550));

    }

}