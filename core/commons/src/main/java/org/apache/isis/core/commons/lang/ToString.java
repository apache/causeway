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

package org.apache.isis.core.commons.lang;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class ToString {

    public static String createName(final Object forObject) {
        final StringBuffer buffer = new StringBuffer();
        createName(forObject, buffer);
        return buffer.toString();
    }

    private static void createName(final Object forObject, final StringBuffer string) {
        string.append(name(forObject));
        string.append("@");
        string.append(Integer.toHexString(forObject.hashCode()));
    }

    public static String name(final Object forObject) {
        final String name = forObject.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static String timestamp(final Date date) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-hhmmssSSS");
        return date == null ? "" : simpleDateFormat.format(date);
    }

    public static ToString createWithIdentifier(final Object object) {
        return new ToString(object);
    }

    public static ToString createAnonymous(final Object object) {
        final ToString string = new ToString();
        string.append(name(object));
        string.append("[");
        return string;
    }

    private boolean addComma = false;
    private final StringBuffer buf;
    private boolean useLineBreaks;

    private ToString() {
        buf = new StringBuffer();
    }

    public ToString(final Object forObject) {
        buf = new StringBuffer();
        createName(forObject, buf);
        buf.append("[");
    }

    public ToString(final Object forObject, final int id) {
        buf = new StringBuffer();
        buf.append(name(forObject));
        buf.append("#");
        buf.append(id);
        buf.append("[");
    }

    public ToString(final Object forObject, final String text) {
        this(forObject);
        buf.append(text);
        addComma = text.length() > 0;
    }

    public ToString append(final String text) {
        buf.append(text);
        return this;
    }

    public ToString append(final String name, final boolean flag) {
        append(name, flag ? "true" : "false");
        return this;
    }

    public ToString append(final String name, final byte number) {
        append(name, Byte.toString(number));
        return this;
    }

    public ToString append(final String name, final double number) {
        append(name, Double.toString(number));
        return this;
    }

    public ToString append(final String name, final float number) {
        append(name, Float.toString(number));
        return this;
    }

    public ToString append(final String name, final int number) {
        append(name, Integer.toString(number));
        return this;
    }

    public ToString append(final String name, final long number) {
        append(name, Long.toString(number));
        return this;
    }

    public ToString append(final String name, final Object object) {
        append(name, object == null ? "null" : object.toString());
        return this;
    }

    public ToString append(final String name, final short number) {
        append(name, Short.toString(number));
        return this;
    }

    public ToString append(final String name, final String string) {
        if (addComma) {
            this.buf.append(',');
            if (useLineBreaks) {
                this.buf.append("\n\t");
            }
        } else {
            addComma = true;
        }
        this.buf.append(name);
        this.buf.append('=');
        this.buf.append(string);

        return this;
    }

    public ToString appendAsHex(final String name, final long number) {
        append(name, "#" + Long.toHexString(number));
        return this;
    }

    public void appendAsTimestamp(final String name, final Date date) {
        final String dateString = timestamp(date);
        append(name, dateString);
    }

    public void appendTruncated(final String name, final String string, final int maxLength) {
        if (string.length() > maxLength) {
            append(name, string.substring(0, maxLength));
            append("...");
        } else {
            append(name, string);
        }
    }

    public void setAddComma() {
        this.addComma = true;
    }

    public void setUseLineBreaks(final boolean useLineBreaks) {
        this.useLineBreaks = useLineBreaks;
    }

    @Override
    public String toString() {
        buf.append(']');
        return buf.toString();
    }
}
