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
import org.apache.isis.application.TitledObject;
import org.apache.isis.application.value.ValueParseException;


public abstract class BusinessValueHolder implements TitledObject {

    protected BusinessValueHolder(final BusinessObject parent) {
        this.parent = parent;
    }

    /** The parent which owns this value */
    private BusinessObject parent;

    public BusinessObject getParent() {
        return parent;
    }

    /**
     * Invokes <code>objectChanged()</code> on parent, provided that parent has been specified (is not
     * <code>nothing</code>).
     */
    protected void parentChanged() {
        if (this.getParent() == null) {
            return;
        }
        this.getParent().objectChanged();
    }

    protected void ensureAtLeastPartResolved() {
    }

    /** By default all values are changeable by the user */
    public boolean userChangeable() {
        return true;
    }

    public abstract boolean isEmpty();

    public abstract boolean isSameAs(final BusinessValueHolder object);

    public String titleString() {
        return title().toString();
    }

    public abstract Title title();

    /**
     * Returns a string representation of this object.
     * <p>
     * The specification of this string representation is not fixed, but, at the time of writing, consists of
     * <i>title [short ObjectAdapterClassName] </i>
     * </p>
     * 
     * @return string representation of object.
     */
    public String toString() {
        return titleString(); // + " [" + this.getClass().getName() + "]";
    }

    public Object getValue() {
        return this;
    }

    public abstract void parseUserEntry(final String text) throws ValueParseException;

    public abstract void restoreFromEncodedString(final String data);

    public abstract String asEncodedString();

    public abstract void copyObject(final BusinessValueHolder object);

    public abstract void clear();
}
