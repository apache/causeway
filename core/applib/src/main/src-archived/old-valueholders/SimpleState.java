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

import org.apache.isis.application.ApplicationException;
import org.apache.isis.application.BusinessObject;
import org.apache.isis.application.Title;
import org.apache.isis.application.control.State;
import org.apache.isis.application.value.ValueParseException;


public class SimpleState extends BusinessValueHolder implements State {
    private static final long serialVersionUID = 1L;
    private String name;
    private int id;
    private State[] states;

    public SimpleState(final int id, final String name) {
        this(null, id, name);
    }

    public SimpleState(final State[] states) {
        this(null, states);
    }

    public SimpleState(final BusinessObject parent, final int id, final String name) {
        super(parent);
        if (id < 0) {
            throw new IllegalArgumentException("Id must be 0 or greater");
        }

        this.id = id;
        this.name = name;
    }

    public SimpleState(final BusinessObject parent, final State[] states) {
        super(parent);
        this.states = states;
    }

    public String getName() {
        ensureAtLeastPartResolved();
        return name;
    }

    public Object getValue() {
        ensureAtLeastPartResolved();
        return this;
    }

    public boolean userChangeable() {
        return false;
    }

    public void clear() {
        id = -1;
        name = null;
        parentChanged();
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        setState(text, true);
    }

    public void reset() {
        id = -1;
        name = null;
        parentChanged();
    }

    public void restoreFromEncodedString(final String data) {
        id = Integer.valueOf(data).intValue();
        name = "unmatched state";
        for (int i = 0; i < states.length; i++) {
            if (id == ((SimpleState) states[i]).id) {
                name = ((SimpleState) states[i]).name;
                break;
            }
        }
    }

    public void setValue(final String stateName) {
        if ((stateName == null)) {
            this.clear();
        } else {
            setState(stateName, true);
        }
    }

    private void setState(final String stateName, final boolean notify) {

        if (this.states == null) {
            return;
        }
        for (int i = 0; i < states.length; i++) {
            SimpleState state = (SimpleState) states[i];
            if (state.name.equals(stateName)) {
                setValuesInternal(state.id, state.name, notify);
                return;
            }
        }
    }

    private void setValuesInternal(final int id, final String name, final boolean notify) {

        if (notify) {
            ensureAtLeastPartResolved();
        }
        this.id = id; // -1 means null.
        this.name = name;
        if (notify) {
            parentChanged();
        }
    }

    public String asEncodedString() {
        ensureAtLeastPartResolved();
        return String.valueOf(id);
    }

    public void copyObject(final BusinessValueHolder object) {
        throw new ApplicationException();
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return id == -1;
    }

    public boolean equals(final Object object) {
        ensureAtLeastPartResolved();
        if (object instanceof SimpleState) {
            int cid = ((SimpleState) object).id;
            if (cid == id) {
                return true;
            }
        }
        return false;
    }

    public boolean isSameAs(final BusinessValueHolder object) {
        ensureAtLeastPartResolved();
        if (object instanceof SimpleState) {
            int cid = ((SimpleState) object).id;
            if (cid == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * copies the state across from the specified state
     */
    public void changeTo(final State state) {
        SimpleState ss = (SimpleState) state;
        setValuesInternal(ss.id, ss.name, true);
    }

    public String titleString() {
        ensureAtLeastPartResolved();
        return name;
    }

    public Title title() {
        return new Title(titleString());
    }
}
