package com.example.shop;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Test
    void itemsWithSamePropertiesShouldBeEqual() {
        Item item1 = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
        Item item2 = new Item("T-Shirt", BigDecimal.valueOf(150), 1);
        Item different = new Item("Jeans", BigDecimal.valueOf(150), 1);

        assertThat(item1)
                .isNotNull()
                .isEqualTo(item2)
                .isNotEqualTo(different)
                .hasSameHashCodeAs(item2);
    }

}