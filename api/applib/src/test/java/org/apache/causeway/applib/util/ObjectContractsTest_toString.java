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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.util.ObjectContracts.ObjectContract;
import org.apache.causeway.applib.util.ObjectContracts.ToStringEvaluator;

public class ObjectContractsTest_toString {

    private Invoice inv;
    private Invoice2 inv2;

    @BeforeEach
    public void setUp() throws Exception {
        inv = new Invoice();
        inv2 = new Invoice2();

        inv.setNumber("123");
        inv2.setNumber("123");
    }

    @Test
    public void vanilla() {
        assertThat(InvoiceItem.newInvoiceItem(null, null, null, null).toString(), is("InvoiceItem{invoice=null, productCode=null, quantity=null, rush=null}"));
        assertThat(InvoiceItem.newInvoiceItem(inv, null, null, null).toString(), is("InvoiceItem{invoice=Invoice{number=123}, productCode=null, quantity=null, rush=null}"));
        assertThat(InvoiceItem.newInvoiceItem(inv, "A", null, null).toString(), is("InvoiceItem{invoice=Invoice{number=123}, productCode=A, quantity=null, rush=null}"));
        assertThat(InvoiceItem.newInvoiceItem(inv, "A", Integer.valueOf(1), null).toString(), is("InvoiceItem{invoice=Invoice{number=123}, productCode=A, quantity=1, rush=null}"));
        assertThat(InvoiceItem.newInvoiceItem(inv, "A", Integer.valueOf(1), true).toString(), is("InvoiceItem{invoice=Invoice{number=123}, productCode=A, quantity=1, rush=true}"));
    }

    @Test
    public void customized() {
        assertThat(InvoiceItem2.newInvoiceItem(null, null, null, null).toString(), is("InvoiceItem2{invoice=null, productCode=null, quantity=null, rush=null}"));
        assertThat(InvoiceItem2.newInvoiceItem(inv2, null, null, null).toString(), is("InvoiceItem2{invoice=123, productCode=null, quantity=null, rush=null}"));
        assertThat(InvoiceItem2.newInvoiceItem(inv2, "A", null, null).toString(), is("InvoiceItem2{invoice=123, productCode=A, quantity=null, rush=null}"));
        assertThat(InvoiceItem2.newInvoiceItem(inv2, "A", Integer.valueOf(1), null).toString(), is("InvoiceItem2{invoice=123, productCode=A, quantity=1, rush=null}"));
        assertThat(InvoiceItem2.newInvoiceItem(inv2, "A", Integer.valueOf(1), false).toString(), is("InvoiceItem2{invoice=123, productCode=A, quantity=1, rush=false}"));
    }

}

@SuppressWarnings("deprecation")
class Invoice2 implements Comparable<Invoice2>, Numbered {
    private static final String KEY_PROPERTIES = "number";

    private String number;
    @Override
    public String getNumber() {
        return number;
    }
    public void setNumber(final String number) {
        this.number = number;
    }
    @Override
    public String toString() {
        return ObjectContracts.toString(this, KEY_PROPERTIES);
    }
    @Override
    public int compareTo(final Invoice2 o) {
        return ObjectContracts.compare(this, o, KEY_PROPERTIES);
    }
}

@SuppressWarnings("deprecation")
class InvoiceItem2 implements Comparable<InvoiceItem2> {

    static InvoiceItem2 newInvoiceItem(final Invoice2 invoice, final String productCode, final Integer quantity, final Boolean rush) {
        final InvoiceItem2 invoiceItem = new InvoiceItem2();
        invoiceItem.setInvoice(invoice);
        invoiceItem.setProductCode(productCode);
        invoiceItem.setQuantity(quantity);
        invoiceItem.setRush(rush);
        return invoiceItem;
    }

    private Invoice2 invoice;
    public Invoice2 getInvoice() {
        return invoice;
    }
    public void setInvoice(final Invoice2 invoice) {
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

    private Boolean rush;
    public Boolean isRush() {
        return rush;
    }
    public void setRush(final Boolean rush) {
        this.rush = rush;
    }

    private static final String KEY_PROPERTIES = "invoice desc, productCode, quantity, rush desc";

    private static final ObjectContract<InvoiceItem2> contract =
            ObjectContracts.parse(InvoiceItem2.class, KEY_PROPERTIES)
            .withValueToStringFunction(ToStringEvaluator.combineToFunction(new NumberedEvaluator()));

    @Override
    public String toString() {
        return contract.toString(this);
        //legacy of ...
        //return new ObjectContracts().with(new NumberedEvaluator()).toStringOf(this, KEY_PROPERTIES);
    }
    @Override
    public int compareTo(final InvoiceItem2 o) {
        return ObjectContracts.compare(this, o, KEY_PROPERTIES);
    }
}