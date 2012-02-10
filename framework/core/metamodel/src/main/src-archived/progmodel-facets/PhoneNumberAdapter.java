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

import org.apache.isis.applib.value.PhoneNumber;
import org.apache.isis.noa.adapter.value.StringValue;
import org.apache.isis.nof.core.adapter.value.AbstractValueAdapter;


public class PhoneNumberAdapter extends AbstractValueAdapter implements StringValue {
    private PhoneNumber number;

    public PhoneNumberAdapter() {
        this.number = null;
    }

    public PhoneNumberAdapter(final PhoneNumber number) {
        this.number = number;
    }

    public byte[] asEncodedString() {
        return number.toString().getBytes();
    }

    public String getIconName() {
        return "phone-number";
    }

    public Object getObject() {
        return number;
    }

    public Class getValueClass() {
        return PhoneNumber.class;
    }

    public void parseTextEntry(final String text) {
        if (text == null || text.trim().equals("")) {
            number = null;
        } else {
            StringBuffer s = new StringBuffer(text.length());
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if ("01234567890 .-()".indexOf(c) >= 0) {
                    s.append(c);
                }
                // TODO allow 'ext' to be part of string
            }
            number = new PhoneNumber(s.toString().trim());
        }
    }

    public void restoreFromEncodedString(final byte[] data) {
        String text = new String(data);
        number = new PhoneNumber(text);
    }

    public void setMask(String mask) {}

    public void setValue(String value) {
        number = new PhoneNumber(value);
    }

    public String stringValue() {
        return number.toString();
    }

    public String titleString() {
        return number == null ? "" : number.toString();
    }

    public String toString() {
        return "QunatityAdapter: " + number;
    }
}
