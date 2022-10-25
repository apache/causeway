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
package org.apache.causeway.applib.util;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.internaltestsupport.contract.ValueTypeContractTestAbstract;

class ObjectContractsTest_equals_and_hashCode extends ValueTypeContractTestAbstract<InvoiceItem3> {

    private Invoice3 inv123;
    private Invoice3 inv456;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        inv123 = new Invoice3();
        inv123.setNumber("123");
        inv456 = new Invoice3();
        inv456.setNumber("456");
    }

    @Override
    protected List<InvoiceItem3> getObjectsWithSameValue() {
        return _Lists.of(
                InvoiceItem3.newInvoiceItem(inv123, "A", Integer.valueOf(1)),
                InvoiceItem3.newInvoiceItem(inv123, "A", Integer.valueOf(1))
                );
    }

    @Override
    protected List<InvoiceItem3> getObjectsWithDifferentValue() {
        return _Lists.of(
                InvoiceItem3.newInvoiceItem(null, "A", Integer.valueOf(2)),
                InvoiceItem3.newInvoiceItem(inv456, "A", Integer.valueOf(2)),
                InvoiceItem3.newInvoiceItem(inv123, null, Integer.valueOf(1)),
                InvoiceItem3.newInvoiceItem(inv123, "A", Integer.valueOf(2)),
                InvoiceItem3.newInvoiceItem(inv123, "B", Integer.valueOf(1)),
                InvoiceItem3.newInvoiceItem(inv123, "A", null)
                );
    }
}

@SuppressWarnings("deprecation")
class Invoice3 {
    private static final String KEY_PROPERTIES = "number";

    private String number;
    public String getNumber() {
        return number;
    }
    public void setNumber(final String number) {
        this.number = number;
    }
    @Override
    public int hashCode() {
        return ObjectContracts.hashCode(this, KEY_PROPERTIES);
    }
    @Override
    public boolean equals(final Object obj) {
        return ObjectContracts.equals(this, obj, KEY_PROPERTIES);
    }

}

@SuppressWarnings("deprecation")
class InvoiceItem3 {

    static InvoiceItem3 newInvoiceItem(final Invoice3 invoice, final String productCode, final Integer quantity) {
        final InvoiceItem3 invoiceItem = new InvoiceItem3();
        invoiceItem.setInvoice(invoice);
        invoiceItem.setProductCode(productCode);
        invoiceItem.setQuantity(quantity);
        return invoiceItem;
    }

    private Invoice3 invoice;
    public Invoice3 getInvoice() {
        return invoice;
    }
    public void setInvoice(final Invoice3 invoice) {
        this.invoice = invoice;
    }

    private String productCode;
    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(final String productCode) {
        this.productCode = productCode;
    }

    private Integer quantity;
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }


    private static final String KEY_PROPERTIES = "invoice desc, productCode, quantity";
    @Override
    public int hashCode() {
        return ObjectContracts.hashCode(this, KEY_PROPERTIES);
    }
    @Override
    public boolean equals(final Object obj) {
        return ObjectContracts.equals(this, obj, KEY_PROPERTIES);
    }
}
