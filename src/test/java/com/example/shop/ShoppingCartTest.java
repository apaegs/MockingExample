package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


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
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(550));
    }

    @Test
    void shouldApplyDiscountToTotalPrice() {
        // ARRANGE
        Item item1 = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
        Item item2 = new Item("Sweater", BigDecimal.valueOf(200), 1);
        cart.addItem(item1);
        cart.addItem(item2);

        // ACT
        cart.applyDiscount(BigDecimal.valueOf(10)); // 10% discount
        BigDecimal total = cart.getTotalPrice();

        // ASSERT
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(315));
    }

    @Test
    void shouldUpdateItemQuantity() {
        // ARRANGE
        Item item = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
        cart.addItem(item);

        // ACT
        cart.updateQuantity(item, 3);

        // ASSERT
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(450));
    }

    // Edge Cases

    @Test
    void shouldReturnZeroForEmptyCart() {
        // ARRANGE

        // ACT
        BigDecimal total = cart.getTotalPrice();

        // ASSERT
        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldHandleRemovingNonExistentItem() {
        // ARRANGE
        Item existingItem = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
        Item nonExistentItem = new Item("Jeans", BigDecimal.valueOf(200), 1);
        cart.addItem(existingItem);

        // ACT
        cart.removeItem(nonExistentItem);

        // ASSERT
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems()).contains(existingItem);
    }

    @Test
    void shouldMergeQuantitiesWhenAddingSameItemTwice() {
        // ARRANGE
        Item item1 = new Item("T-Shirt", BigDecimal.valueOf(150), 2);
        Item item2 = new Item("T-Shirt", BigDecimal.valueOf(150), 3);

        // ACT
        cart.addItem(item1);
        cart.addItem(item2);

        // ASSERT
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldThrowExceptionWhenAddingItemWithNegativeQuantity() {
        // ARRANGE
        Item item = new Item("T-Shirt", BigDecimal.valueOf(150), -1);

        // ACT & ASSERT
        assertThatThrownBy(() -> cart.addItem(item))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kvantitet måste vara positiv");

    }

    @Test
    void shouldThrowExceptionWhenAddingItemWithZeroQuantity() {
        // ARRANGE
        Item item = new Item("T-Shirt", BigDecimal.valueOf(150), 0);

        // ACT & ASSERT
        assertThatThrownBy(() -> cart.addItem(item))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kvantitet måste vara positiv");
    }

}