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
package org.apache.isis.applib.services.tablecol;

import java.util.List;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;

// tag::refguide[]
public interface TableColumnOrderService {

    List<String> orderParented(
            final Object parent,
            final String collectionId,
            final Class<?> collectionType,
            final List<String> propertyIds);

    List<String> orderStandalone(
            final Class<?> collectionType,
            final List<String> propertyIds);

// end::refguide[]
    /**
     * Used as a fallback.
     */
    @Service
    @Named("isisApplib.TableColumnOrderService.Default")
    @Order(OrderPrecedence.LATE)
    @Primary
    @Qualifier("Default")
    public static class Default implements TableColumnOrderService {

        @Override
        public List<String> orderParented(
                final Object parent,
                final String collectionId,
                final Class<?> collectionType,
                final List<String> propertyIds) {
            return propertyIds;
        }

        @Override
        public List<String> orderStandalone(
                final Class<?> collectionType,
                final List<String> propertyIds) {
            return propertyIds;
        }
    }
// tag::refguide[]

}
// end::refguide[]
