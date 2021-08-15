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
package org.apache.isis.core.metamodel.services.tablecol;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;

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
