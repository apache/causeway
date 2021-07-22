package org.apache.isis.core.metamodel.services.tablecol;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;

class TableColumnOrderServiceUsingTxtFile_Test {

    private TableColumnOrderServiceUsingTxtFile service;

    @BeforeEach
    void setUp() {
        service = new TableColumnOrderServiceUsingTxtFile();
    }

    @Nested
    class orderParented {

        @Test
        void happy_case() {
            // when
            val ordered = service.orderParented(new Customer(), "orders", Order.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderDate", "orderStatus");
        }

        @Test
        void missing_file() {
            // when
            val ordered = service.orderParented(new Customer(), "nonExistent", Order.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).isNull();
        }
    }

    @Nested
    class orderStandalone {

        @Test
        void happy_case() {
            // when
            val ordered = service.orderStandalone(Order.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderDate", "orderStatus");
        }

        @Test
        void missing_file() {
            // when
            val ordered = service.orderStandalone(Order2.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).isNull();
        }
    }

}
class Order {
}
class Order2 {
}
class Customer {
}
