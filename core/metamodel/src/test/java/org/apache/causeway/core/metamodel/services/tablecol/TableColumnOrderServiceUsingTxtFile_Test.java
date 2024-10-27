/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.metamodel.services.tablecol;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableColumnOrderServiceUsingTxtFile_Test {

    private TableColumnOrderServiceUsingTxtFile service;

    @BeforeEach
    void setUp() {
        service = new TableColumnOrderServiceUsingTxtFile();
    }

    @Nested
    class orderParented {

        /**
         * should read from Customer#orders.columnOrder.txt
         */
        @Test
        void happy_case() {
            // when
            var ordered = service.orderParented(new Customer(), "orders", Order.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderDate", "orderStatus");
        }

        /**
         * should read from Order.columnOrder.txt
         */
        @Test
        void fallback_to_fallback_file() {
            // when
            var ordered = service.orderParented(new Customer(), "moreOrders", Order.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderDate", "orderStatus");
        }

        /**
         * should read from Customer#_.Order4.columnOrder.txt
         */
        @Test
        void fallback_to_wildcard_type() {
            // when
            var ordered = service.orderParented(new Customer(), "otherOrders", Order4.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderDate", "orderStatus");
        }

        /**
         * should read from Customer#_.Order5.columnOrder.fallback.txt
         */
        @Test
        void fallback_to_wildcard_fallback_type() {
            // when
            var ordered = service.orderParented(new Customer(), "otherOrders", Order5.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderDate", "orderStatus");
        }

        /**
         * should read from Order.columnOrder.txt
         */
        @Test
        void fallback_to_element_type() {
            // when
            var ordered = service.orderParented(new Customer(), "previousOrders", Order.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));   // "orderDate" is not in the file being read

            // then
            assertThat(ordered).containsExactly("orderNum", "orderStatus");
        }

        @Test
        void missing_file() {
            // when
            var ordered = service.orderParented(new Customer(), "nonExistent", Order2.class,
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
            var ordered = service.orderStandalone(Order.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderStatus");
        }

        @Test
        void fallback_file() {
            // when
            var ordered = service.orderStandalone(Order3.class,
                    Arrays.asList("orderNum", "orderStatus", "orderDate", "orderAmount"));

            // then
            assertThat(ordered).containsExactly("orderNum", "orderStatus");
        }

        @Test
        void missing_file() {
            // when
            var ordered = service.orderStandalone(Order2.class,
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
class Order3 {
}
class Order4 {
}
class Order5 {
}
class Customer {
}
