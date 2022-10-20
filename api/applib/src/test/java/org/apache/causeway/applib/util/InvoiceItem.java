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

@SuppressWarnings("deprecation")
class InvoiceItem implements Comparable<InvoiceItem> {

    static InvoiceItem newInvoiceItem(Invoice invoice, String productCode, Integer quantity, Boolean rush) {
        final InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setInvoice(invoice);
        invoiceItem.setProductCode(productCode);
        invoiceItem.setQuantity(quantity);
        invoiceItem.setRush(rush);
        return invoiceItem;
    }

    private Invoice invoice;
    public Invoice getInvoice() {
        return invoice;
    }
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    private String productCode;
    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    private Integer quantity;
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    private Boolean rush;
    public Boolean isRush() {
        return rush;
    }
    public void setRush(Boolean rush) {
        this.rush = rush;
    }

    private static final String KEY_PROPERTIES = "invoice desc, productCode asc, quantity asc nullsLast, rush desc nullsLast";

    @Override
    public String toString() {
        return ObjectContracts.toString(this, KEY_PROPERTIES);
    }
    @Override
    public int compareTo(InvoiceItem o) {
        return ObjectContracts.compare(this, o, KEY_PROPERTIES);
    }
}