package org.nakedobjects.object.value;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.Title;
import org.nakedobjects.object.ValueParseException;


public class TimePeriod extends AbstractNakedValue {
    private final Time end = new Time();
    private final Time start = new Time();

    public TimePeriod() {
     //   start.clear();
      //  end.clear();
        
        // TODO fix and remove
        start.setValue(12 * Time.HOUR);
        end.setValue(13 * Time.HOUR);
    }

    public TimePeriod(TimePeriod existing) {
        setValue(existing);
    }

    public void clear() {
        start.clear();
        end.clear();
    }

    public void copyObject(Naked object) {
        if (!(object instanceof TimePeriod)) {
            throw new IllegalArgumentException("Can only copy the value of a TimePeriod object");
        }

        TimePeriod tp = (TimePeriod) object;

        if (tp.isEmpty()) {
            clear();
        } else {
            start.setValue(tp.getStart());
            end.setValue(tp.getEnd());
        }
    }

    public boolean endsAfter(TimePeriod arg) {
        if (end.isGreaterThan(arg.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean entirelyContains(TimePeriod arg) {
        return arg.getStart().isBetween(start, end) && arg.getEnd().isBetween(start, end);
    }

    public Time getEnd() {
        return end;
    }

    public Time getStart() {
        return start;
    }

    public boolean isEmpty() {
        return (start.isEmpty()) && (end.isEmpty());
    }

    public boolean isEqualTo(TimePeriod arg) {
        if (start.isEqualTo(arg.getStart()) && end.isEqualTo(arg.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSameAs(Naked object) {
        if (object instanceof TimePeriod) {
            TimePeriod tp = (TimePeriod) object;

            return (start.isEqualTo(tp.getStart())) && (end.isEqualTo(tp.getEnd()));
        } else {
            return false;
        }
    }

    public TimePeriod leadDifference(TimePeriod arg) {
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

    public TimePeriod overlap(TimePeriod arg) {
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

        //N.B. If no overlap currently creates empty TimePeriod.  
        return overlap;
    }

    public boolean overlaps(TimePeriod arg) {
        if (end.isGreaterThan(arg.getStart()) && start.isLessThan(arg.getEnd())) {
            return true;
        } else {
            return false;
        }
    }

    public void parse(String text) throws ValueParseException {
        if (text.trim().equals("")) {
            clear();
        } else {
            int tilde = text.indexOf("~");

            if (tilde >= 0) {
                start.parse(text.substring(0, tilde).trim());
                end.parse(text.substring(tilde + 1).trim());
            } else {
                // Not sure how to specify the type of the Exception
                throw new ValueParseException(new Exception(), "No tilde found");
            }

            if (end.isLessThan(start)) {
                throw new ValueParseException(new Exception(), "End time before start time");
            }
        }
    }

    public void reset() {
        start.reset();
        end.reset();
    }

    public void restoreString(String data) {
        if (data.equals("NULL")) {
            clear();
        } else {
            start.restoreString(data.substring(0, 3));
            end.restoreString(data.substring(4, 7));
        }
    }

    public String saveString() {
        if (start.isEmpty() || end.isEmpty()) {
            return "NULL";
        } else {
            StringBuffer data = new StringBuffer(8);
            data.append(start.saveString());
            data.append(end.saveString());

            return data.toString();
        }
    }

    public void setValue(Time start, Time end) {
        this.start.setValue(start);
        this.end.setValue(end);
    }

    public void setValue(TimePeriod t) {
        start.setValue(t.getStart());
        end.setValue(t.getEnd());
    }

    public boolean startsBefore(TimePeriod arg) {
        if (start.isLessThan(arg.getStart())) {
            return true;
        } else {
            return false;
        }
    }

    public TimePeriod tailDifference(TimePeriod arg) {
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
        Title t = new Title(start);
        t.append("~");
        t.append(end);

        return t;
    }
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
