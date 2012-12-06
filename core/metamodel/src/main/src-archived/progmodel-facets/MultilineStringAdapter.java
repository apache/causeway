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

import org.apache.isis.applib.value.MultilineString;
import org.apache.isis.noa.adapter.value.MultilineStringValue;
import org.apache.isis.nof.core.adapter.value.AbstractValueAdapter;
import org.apache.isis.nof.core.util.ToString;


public class MultilineStringAdapter extends AbstractValueAdapter implements MultilineStringValue {
    private MultilineString string;

    public MultilineStringAdapter() {
        this.string = null;
    }

    public MultilineStringAdapter(final MultilineString string) {
        this.string = string;
    }

    public byte[] asEncodedString() {
        return stringValue().getBytes();
    }

    public boolean canClear() {
        return true;
    }

    public void clear() {
        string = null;
    }

    public String getIconName() {
        return "text";
    }

    public Object getObject() {
        return string;
    }

    public Class getValueClass() {
        return MultilineString.class;
    }

    public boolean isEmpty() {
        return string == null;
    }

    public void parseTextEntry(final String text) {
        string = new MultilineString(text);
    }

    public void restoreFromEncodedString(final byte[] data) {
        if (data == null) {
            string = null;
        } else {
            String text = new String(data);
            string = new MultilineString(text);
        }
    }

    public void setMask(String mask) {}

    public void setValue(final String value) {
        string = new MultilineString(value);
    }

    public String stringValue() {
        return string == null ? "" : string.getString();
    }

    public String titleString() {
        return string == null ? "" : string.getString();
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("string", string == null ? "" : string.getString());
        return str.toString();
    }
}
