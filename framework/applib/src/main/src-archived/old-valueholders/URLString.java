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


package org.apache.isis.application.valueholder;

import org.apache.isis.application.BusinessObject;
import org.apache.isis.application.Title;
import org.apache.isis.application.value.ValueParseException;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * value object to represent an URL.
 * <p>
 * NOTE: this class currently does not support about listeners
 * </p>
 */
public class URLString extends BusinessValueHolder {
    private String urlString;

    public URLString() {
        this(null, "");
    }

    public URLString(final String urlString) {
        this(null, urlString);
    }

    public URLString(final URLString urlString) {
        this(null, urlString);
    }

    public URLString(final BusinessObject parent) {
        this(parent, "");
    }

    public URLString(final BusinessObject parent, final String urlString) {
        super(parent);
        this.urlString = urlString;
    }

    public URLString(final BusinessObject parent, final URLString urlString) {
        super(parent);
        this.urlString = new String(urlString.toString());
    }

    public void clear() {
        setValuesInternal(null, true);
    }

    /**
     * Copies the specified object's contained data to this instance. param object the object to copy the data
     * from
     */
    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof URLString)) {
            throw new IllegalArgumentException("Can only copy the value of  a URLString object");
        }
        setValue((URLString) object);
    }

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof URLString)) {
            return false;
        }
        URLString object = (URLString) obj;
        if (object.isEmpty() && isEmpty()) {
            return true;
        }
        return object.urlString.equals(urlString);
    }

    public String getObjectHelpText() {
        return "A URLString object.";
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return urlString == null;
    }

    /**
     * Compares the url string to see if the contain the same text if the specified object is a
     * <code>URLString</code> object else returns false.
     * 
     * @see BusinessValueHolder#isSameAs(BusinessValueHolder)
     */
    public boolean isSameAs(final BusinessValueHolder object) {
        ensureAtLeastPartResolved();
        if (object instanceof URLString) {
        	URLString other = (URLString) object;
        	if (urlString == null) {
        		return other.urlString == null;
        	}
            return urlString.equals(other.urlString);
        } else {
            return false;
        }

    }

    public void parseUserEntry(final String urlString) throws ValueParseException {
        try {
            new URL(urlString);
            setValue(urlString);
        } catch (MalformedURLException e) {
            throw new ValueParseException("Invalid URL", e);
        }
    }

    /**
     * Reset this url string so it contains an empty string, i.e. "".
     */
    public void reset() {
        setValue("");
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            setValuesInternal(null, false);
        } else {
            setValuesInternal(data, false);
        }
    }

    public String asEncodedString() {
        return isEmpty() ? "NULL" : urlString;
    }

    public void setValue(final String urlString) {
        setValuesInternal(urlString, true);
    }

    public void setValue(final URLString urlString) {
        setValuesInternal(urlString.urlString, true);
    }

    private void setValuesInternal(final String value, final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.urlString = value;
        if (notify) {
            parentChanged();
        }
    }

    public String stringValue() {
        ensureAtLeastPartResolved();
        return urlString;
    }

    public Title title() {
        ensureAtLeastPartResolved();
        return new Title(urlString);
    }
}
