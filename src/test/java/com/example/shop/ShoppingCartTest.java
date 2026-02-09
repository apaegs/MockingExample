package com.example.shop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Nested
    class BasicOperations {

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
    }

    @Nested
    class Calculations {

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
        void shouldReturnZeroForEmptyCart() {
            // ARRANGE

            // ACT
            BigDecimal total = cart.getTotalPrice();

            // ASSERT
            assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
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
        void shouldAllowExactly100PercentDiscount() {
            cart.addItem(new Item("T-Shirt", BigDecimal.valueOf(100), 1));
            cart.applyDiscount(BigDecimal.valueOf(100));
            assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Validation and Error Handling")
    class Validation {

        @ParameterizedTest
        @CsvSource({
                "-1, Kvantitet måste vara positiv",
                "0,  Kvantitet måste vara positiv"
        })
        void shouldThrowExceptionForInvalidQuantities(int quantity, String expectedMessage) {
            // ARRANGE
            Item item = new Item("T-Shirt", BigDecimal.valueOf(150), quantity);

            // ACT & ASSERT
            assertThatThrownBy(() -> cart.addItem(item))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedMessage);

            // test updateQuantity
            Item validItem = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
            cart.addItem(validItem);
            assertThatThrownBy(() -> cart.updateQuantity(validItem, quantity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedMessage);
        }

        @ParameterizedTest
        @CsvSource({
                "-10, Rabatt kan inte vara negativ",
                "0,   Rabatt kan inte vara negativ",
                "101, Rabatt kan inte vara över 100%"
        })
        void shouldThrowExceptionForInvalidDiscounts(int discount, String expectedMessage) {
            // ARRANGE
            BigDecimal discountValue = BigDecimal.valueOf(discount);

            // ACT & ASSERT
            assertThatThrownBy(() -> cart.applyDiscount(discountValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(expectedMessage);
        }
    }
}