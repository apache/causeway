package org.nakedobjects.object.defaults.value;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.defaults.Title;


public class DatePeriod extends AbstractNakedValue {
    private final Date end = new Date();
    private final Date start = new Date();

    public DatePeriod() {
        start.clear();
        end.clear();
    }

    public void clear() {
        getStart().clear();
        getEnd().clear();
    }

    public boolean contains(Date d) {
        return d.isGreaterThanOrEqualTo(start) && d.isLessThanOrEqualTo(end);
    }

    public void copyObject(Naked object) {
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

    public boolean endsAfter(DatePeriod arg) {
        if (getEnd().isGreaterThan(arg.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    public Date getEnd() {
        return end;
    }

    public Date getStart() {
        return start;
    }

    public boolean isDatePeriod(Naked object) {
        return object instanceof DatePeriod;
    }

    public boolean isEmpty() {
        return (getStart().isEmpty()) && (getEnd().isEmpty());
    }

    public boolean isSameAs(Naked object) {
        if (!(object instanceof DatePeriod)) {
            return false;
        } else {
            DatePeriod dp = (DatePeriod) object;

            return (getStart().isEqualTo(dp.getStart())) && (getEnd().isEqualTo(dp.getEnd()));
        }
    }

    public void leadDifference(DatePeriod arg) {
        if (!this.overlaps(arg)) {
            throw new IllegalArgumentException("No overlap");
        } else {
            if (this.startsBefore(arg)) {
                getEnd().setValue(arg.getStart());
                getEnd().add(0, 0, -1);
            } else {
                getEnd().setValue(getStart());
                getEnd().add(0, 0, -1);
                getStart().setValue(arg.getStart());
            }
        }
    }

    public void overlap(DatePeriod arg) {
        if (!this.overlaps(arg)) {
            throw new IllegalArgumentException("No overlap");
        } else {
            if (arg.getStart().isGreaterThan(getStart())) {
                getStart().setValue(arg.getStart());
            } else {
                getStart().setValue(getStart());
            }

            if (arg.getEnd().isLessThan(getEnd())) {
                getEnd().setValue(arg.getEnd());
            } else {
                getEnd().setValue(getEnd());
            }
        }
    }

    public boolean overlaps(DatePeriod arg) {
        return (getEnd().isGreaterThan(arg.getStart()) && start.isLessThan(arg.getEnd()));
    }

    public void parse(String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            int dash = text.indexOf("~");

            if(dash == -1) {
               throw new ValueParseException("No tilde found");
            } 
            getStart().parse(text.substring(0, dash).trim());
            getEnd().parse(text.substring(dash + 1).trim());
            
            if (getEnd().isLessThan(getStart())) {
                throw new ValueParseException("End date must be before start date");
            } 
        }
    }

    public void reset() {
        getStart().reset();
        getEnd().reset();
    }

    public void restoreString(String data) {
        if (data == null || data.equals("NULL")) {
            clear();
        } else {
            int split = data.indexOf('~');
            getStart().restoreString(data.substring(0, split));
            getEnd().restoreString(data.substring(split + 1));
        }
    }

    public String saveString() {
        if (getStart().isEmpty() || getEnd().isEmpty()) {
            return "NULL";
        } else {
            return getStart().saveString() + "~" + getEnd().saveString();
        }
    }

    public void setValue(Date start, Date end) {
        getStart().setValue(start);
        getEnd().setValue(end);
    }

    public void setValue(DatePeriod dp) {
        getStart().setValue(dp.getStart());
        getEnd().setValue(dp.getEnd());
    }

    public boolean startsBefore(DatePeriod arg) {
        if (getStart().isLessThan(arg.getStart())) {
            return true;
        } else {
            return false;
        }
    }

    public void tailDifference(DatePeriod arg) {
        if (!this.overlaps(arg)) {
            throw new IllegalArgumentException("No overlap");
        } else {
            if (this.endsAfter(arg)) {
                getStart().setValue(arg.getEnd());
                getStart().add(0, 0, 1);
            } else {
                getStart().setValue(getEnd());
                getStart().add(0, 0, 1);
                getEnd().setValue(arg.getEnd());
            }
        }
    }

    public Title title() {
        Title t = new Title(getStart());
        t.append("~");
        t.append(getEnd());

        return t;
    }

    /*
     * To be added:
     *
     * method to test that a magnitude falls within the range method to test if
     * two ranges are contiguous method to return the enclosing range for two
     * ranges method to test if one range falls entirely within another
     */
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
