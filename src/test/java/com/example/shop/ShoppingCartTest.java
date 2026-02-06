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

}