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


public class DatePeriod extends BusinessValueHolder {
    private final Date end = new Date();
    private final Date start = new Date();

    public DatePeriod() {
        this((BusinessObject) null);
    }

    public DatePeriod(final DatePeriod period) {
        this(null, period);
    }

    public DatePeriod(final BusinessObject parent) {
        super(parent);
        clear();
    }

    public DatePeriod(final BusinessObject parent, final DatePeriod period) {
        super(parent);
        this.start.copyObject(period.start);
        this.end.copyObject(period.end);
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

    public boolean contains(final Date d) {
        this.ensureAtLeastPartResolved();
        return d.isGreaterThanOrEqualTo(start) && d.isLessThanOrEqualTo(end);
    }

    public void copyObject(final BusinessValueHolder object) {
        if (!(object instanceof DatePeriod)) {
            throw new IllegalArgumentException();
        }

        DatePeriod dp = (DatePeriod) object;

        if (dp.isEmpty()) {
            clear();
        } else {
            setValue(dp);
        }
    }

    public boolean endsAfter(final DatePeriod arg) {
        if (getEndDate().isGreaterThan(arg.getEndDate())) {
            return true;
        } else {
            return false;
        }
    }

    public Date getEndDate() {
        this.ensureAtLeastPartResolved();
        return end;
    }

    public Date getStart() {
        this.ensureAtLeastPartResolved();
        return start;
    }

    public boolean isDatePeriod(final BusinessValueHolder object) {
        return object instanceof DatePeriod;
    }

    public boolean isEmpty() {
        return (getStart().isEmpty()) && (getEndDate().isEmpty());
    }

    public boolean isSameAs(final BusinessValueHolder object) {
        if (!(object instanceof DatePeriod)) {
            return false;
        } else {
            DatePeriod dp = (DatePeriod) object;

            return (getStart().isEqualTo(dp.getStart())) && (getEndDate().isEqualTo(dp.getEndDate()));
        }
    }

    public void leadDifference(final DatePeriod arg) {
        if (!this.overlaps(arg)) {
            throw new IllegalArgumentException("No overlap");
        } else {
            if (this.startsBefore(arg)) {
                getEndDate().setValue(arg.getStart());
                getEndDate().add(0, 0, -1);
            } else {
                getEndDate().setValue(getStart());
                getEndDate().add(0, 0, -1);
                getStart().setValue(arg.getStart());
            }
        }
    }

    public void overlap(final DatePeriod arg) {
        if (!this.overlaps(arg)) {
            throw new IllegalArgumentException("No overlap");
        } else {
            if (arg.getStart().isGreaterThan(getStart())) {
                getStart().setValue(arg.getStart());
            } else {
                getStart().setValue(getStart());
            }

            if (arg.getEndDate().isLessThan(getEndDate())) {
                getEndDate().setValue(arg.getEndDate());
            } else {
                getEndDate().setValue(getEndDate());
            }
        }
    }

    public boolean overlaps(final DatePeriod arg) {
        return (getEndDate().isGreaterThan(arg.getStart()) && start.isLessThan(arg.getEndDate()));
    }

    public void parseUserEntry(final String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            int dash = text.indexOf("~");

            if (dash == -1) {
                throw new ValueParseException("No tilde found");
            }
            Date sd = new Date();
            Date ed = new Date();
            sd.parseUserEntry(text.substring(0, dash).trim());
            ed.parseUserEntry(text.substring(dash + 1).trim());

            if (ed.isLessThan(sd)) {
                throw new ValueParseException("End date must be before start date");
            }
            setValue(sd, ed);
        }
    }

    public void reset() {
        ensureAtLeastPartResolved();
        getStart().reset();
        getEndDate().reset();
        parentChanged();
    }

    public void restoreFromEncodedString(final String data) {
        if (data == null || data.equals("NULL")) {
            clearInternal(false);
        } else {
            int split = data.indexOf('~');
            getStart().restoreFromEncodedString(data.substring(0, split));
            getEndDate().restoreFromEncodedString(data.substring(split + 1));
        }
    }

    public String asEncodedString() {
        if (getStart().isEmpty() || getEndDate().isEmpty()) {
            return "NULL";
        } else {
            return getStart().asEncodedString() + "~" + getEndDate().asEncodedString();
        }
    }

    public void setValue(final Date start, final Date end) {
        getStart().setValue(start);
        getEndDate().setValue(end);
        parentChanged();
    }

    public void setValue(final DatePeriod dp) {
        setValue(dp.getStart(), dp.getEndDate());
    }

    public boolean startsBefore(final DatePeriod arg) {
        if (getStart().isLessThan(arg.getStart())) {
            return true;
        } else {
            return false;
        }
    }

    public void tailDifference(final DatePeriod arg) {
        if (!this.overlaps(arg)) {
            throw new IllegalArgumentException("No overlap");
        } else {
            if (this.endsAfter(arg)) {
                getStart().setValue(arg.getEndDate());
                getStart().add(0, 0, 1);
            } else {
                getStart().setValue(getEndDate());
                getStart().add(0, 0, 1);
                getEndDate().setValue(arg.getEndDate());
            }
        }
    }

    public Title title() {
        Title t = new Title(getStart() == null ? "" : getStart().title().toString());
        t.append("~");
        t.append(getEndDate() == null ? "" : getEndDate().title().toString());

        return t;
    }

    /*
     * To be added:
     * 
     * method to test that a magnitude falls within the range method to test if two ranges are contiguous
     * method to return the enclosing range for two ranges method to test if one range falls entirely within
     * another
     */
}
