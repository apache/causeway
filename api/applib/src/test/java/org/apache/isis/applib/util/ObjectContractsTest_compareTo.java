/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.util;

import java.util.List;

import org.junit.Before;

import org.apache.isis.core.unittestsupport.comparable.ComparableContractTest_compareTo;

public class ObjectContractsTest_compareTo extends ComparableContractTest_compareTo<InvoiceItem> {

    private Invoice inv123;
    private Invoice inv456;

    @Before
    public void setUp() throws Exception {
        inv123 = new Invoice();
        inv123.setNumber("123");

        inv456 = new Invoice();
        inv456.setNumber("456");
    }

    /**
     * as per {@link InvoiceItem#compareTo(InvoiceItem)}
     */
    @Override
    protected List<List<org.apache.isis.applib.util.InvoiceItem>> orderedTuples() {
        return listOf(
                listOf(
                        // invoice desc, ...
                        InvoiceItem.newInvoiceItem(null, null, null, null)
                        ,InvoiceItem.newInvoiceItem(inv456, null, null, null)
                        ,InvoiceItem.newInvoiceItem(inv456, null, null, null)
                        ,InvoiceItem.newInvoiceItem(inv123, null, null, null)
                        )
                ,listOf(
                        // ..., productCode asc, ...
                        InvoiceItem.newInvoiceItem(inv123, null, null, null)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", null, null)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", null, null)
                        ,InvoiceItem.newInvoiceItem(inv123, "B", null, null)
                        )
                ,listOf(
                        // ..., quantity asc nullsLast,...
                        InvoiceItem.newInvoiceItem(inv123, "A", Integer.valueOf(1), null)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", Integer.valueOf(2), null)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", Integer.valueOf(2), null)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", null, null)
                        )
                ,listOf(
                        // ..., rush desc nullsLast
                        InvoiceItem.newInvoiceItem(inv123, "A", Integer.valueOf(1), true)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", Integer.valueOf(1), false)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", Integer.valueOf(1), false)
                        ,InvoiceItem.newInvoiceItem(inv123, "A", Integer.valueOf(1), null)
                        )
                );
    }
}
