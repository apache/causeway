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


package org.apache.isis.progmodel.java5.value;

import org.apache.isis.applib.value.Quantity;
import org.apache.isis.noa.adapter.TextEntryParseException;
import org.apache.isis.noa.adapter.value.IntegerValue;
import org.apache.isis.nof.core.adapter.value.AbstractValueAdapter;
import org.apache.isis.nof.core.conf.Configuration;
import org.apache.isis.nof.core.context.IsisContext;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;


public class QuantityAdapter extends AbstractValueAdapter implements IntegerValue {
    private static NumberFormat DEFAULT_FORMAT = NumberFormat.getNumberInstance();
    private NumberFormat format = DEFAULT_FORMAT;
    private Quantity quantity;

    public QuantityAdapter() {
        this.quantity = null;
        String formatRequired = IsisContext.getConfiguration().getString(Configuration.ROOT + "value.format.quantity");
        if(formatRequired == null) {
            format = DEFAULT_FORMAT;
        } else {
            setMask(formatRequired);
        }
    }

    public QuantityAdapter(final Quantity quantity) {
        this();
        this.quantity = quantity;
    }

    public byte[] asEncodedString() {
        String asString = Integer.toString(quantity.intValue());
        return asString.getBytes();
    }

    public String getIconName() {
        return "quantity";
    }

    public Object getObject() {
        return quantity;
    }

    public Class getValueClass() {
        return String.class;
    }

    public Integer integerValue() {
        return quantity.intValue();
    }

    public void parseTextEntry(final String entry) {
        if (entry == null || entry.trim().equals("")) {
            quantity = null;
        } else {
            try {
                int intValue = format.parse(entry).intValue();
                quantity = new Quantity(intValue);
            } catch (ParseException e) {
                throw new TextEntryParseException("Invalid number", e);
            }
        }
    }

    public void restoreFromEncodedString(final byte[] data) {
        String text = new String(data);
        int value = Integer.valueOf(text).intValue();
        quantity = new Quantity(value);
    }

    public void setMask(String mask) {
        format = new DecimalFormat(mask);
    }

    public void setValue(Integer value) {
        quantity = new Quantity(value);
    }

    public String titleString() {
        return quantity == null ? "" : format.format(quantity.intValue());
    }

    public String toString() {
        return "QunatityAdapter: " + quantity;
    }
}
