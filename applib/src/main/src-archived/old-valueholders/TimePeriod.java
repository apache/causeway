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


public class TimePeriod extends BusinessValueHolder {
    private final Time end = new Time();
    private final Time start = new Time();

    public TimePeriod() {
        this((BusinessObject) null);
    }

    public TimePeriod(final TimePeriod existing) {
        this(null, existing);
    }

    public TimePeriod(final BusinessObject parent) {
        super(parent);
        clear();
    }

    public TimePeriod(final BusinessObject parent, final TimePeriod existing) {
        super(parent);
        setValue(existing);
    }

    public void clear() {
        clearInternal(true);
    }

    private void clearInternal(final boolean notify) {
        if (notify) {
            ensureAtLeastPartResolved();
        }
        start.clear();
        end.clear();
        if (notify) {
            parentChanged();
        }
    }

    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof TimePeriod)) {
            throw new IllegalArgumentException("Can only copy the value of a TimePeriod object");
        }

        TimePeriod tp = (TimePeriod) object;

        if (tp.isEmpty()) {
            clear();
        } else {
            setValue(tp);
        }
    }

    public boolean endsAfter(final TimePeriod arg) {
        ensureAtLeastPartResolved();
        if (end.isGreaterThan(arg.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean entirelyContains(final TimePeriod arg) {
        ensureAtLeastPartResolved();
        return arg.getStart().isBetween(start, end) && arg.getEnd().isBetween(start, end);
    }

    public Time getEnd() {
        ensureAtLeastPartResolved();
        return end;
    }

    public Time getStart() {
        ensureAtLeastPartResolved();
        return start;
    }

    public boolean isEmpty() {
        ensureAtLeastPartResolved();
        return (start.isEmpty()) && (end.isEmpty());
    }

    public boolean isEqualTo(final TimePeriod arg) {
        ensureAtLeastPartResolved();
        if (start.isEqualTo(arg.getStart()) && end.isEqualTo(arg.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSameAs(final BusinessValueHolder object) {
        ensureAtLeastPartResolved();
        if (object instanceof TimePeriod) {
            TimePeriod tp = (TimePeriod) object;

            return (start.isEqualTo(tp.getStart())) && (end.isEqualTo(tp.getEnd()));
        } else {
            return false;
        }
    }

    public TimePeriod leadDifference(final TimePeriod arg) {
        TimePeriod lead = new TimePeriod();

        if (this.startsBefore(arg)) {
            lead.getStart().setValue(start);
            lead.getEnd().setValue(arg.getStart());
        } else {
            lead.getStart().setValue(arg.getStart());
            lead.getEnd().setValue(start);
        }

        return lead;
    }

    public TimePeriod overlap(final TimePeriod arg) {
        TimePeriod overlap = new TimePeriod();
        overlap.clear();

        if (this.overlaps(arg)) {
            if (arg.getStart().isGreaterThan(start)) {
                overlap.getStart().setValue(arg.getStart());
            } else {
                overlap.getStart().setValue(this.start);
            }

            if (arg.getEnd().isLessThan(end)) {
                overlap.getEnd().setValue(arg.getEnd());
            } else {
                overlap.getEnd().setValue(this.end);
            }
        }

        // N.B. If no overlap currently creates empty TimePeriod.
        return overlap;
    }

    public boolean overlaps(final TimePeriod arg) {
        ensureAtLeastPartResolved();
        if (end.isGreaterThan(arg.getStart()) && start.isLessThan(arg.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            int tilde = text.indexOf("~");

            Time st = new Time();
            Time et = new Time();
            if (tilde >= 0) {
                st.parseUserEntry(text.substring(0, tilde).trim());
                et.parseUserEntry(text.substring(tilde + 1).trim());
            } else {
                // Not sure how to specify the type of the Exception
                throw new ValueParseException("No tilde found", new Exception());
            }

            if (et.isLessThan(st)) {
                throw new ValueParseException("End time before start time", new Exception());
            }
            setValue(st, et);
        }
    }

    public void reset() {
        ensureAtLeastPartResolved();
        start.reset();
        end.reset();
        parentChanged();
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            clearInternal(false);
        } else {
            start.restoreFromEncodedString(data.substring(0, 3));
            end.restoreFromEncodedString(data.substring(4, 7));
        }
    }

    public String asEncodedString() {
        ensureAtLeastPartResolved();
        if (start.isEmpty() || end.isEmpty()) {
            return "NULL";
        } else {
            StringBuffer data = new StringBuffer(8);
            data.append(start.asEncodedString());
            data.append(end.asEncodedString());

            return data.toString();
        }
    }

    public void setValue(final Time start, final Time end) {
        ensureAtLeastPartResolved();
        this.start.setValue(start);
        this.end.setValue(end);
        parentChanged();
    }

    public void setValue(final TimePeriod t) {
        setValue(t.getStart(), t.getEnd());
    }

    public boolean startsBefore(final TimePeriod arg) {
        ensureAtLeastPartResolved();
        if (start.isLessThan(arg.getStart())) {
            return true;
        } else {
            return false;
        }
    }

    public TimePeriod tailDifference(final TimePeriod arg) {
        TimePeriod tail = new TimePeriod();

        if (this.endsAfter(arg)) {
            tail.getStart().setValue(arg.getEnd());
            tail.getEnd().setValue(end);
        } else {
            tail.getStart().setValue(end);
            tail.getEnd().setValue(arg.getEnd());
        }

        return tail;
    }

    public Title title() {
        Title t = new Title(getStart() == null ? "" : getStart().title().toString());
        t.append("~");
        t.append(getEnd() == null ? "" : getEnd().title().toString());

        return t;
    }
}
