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

package org.nakedobjects.object;

import org.nakedobjects.object.defaults.AbstractNakedObject;
import org.nakedobjects.object.defaults.Title;
import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.FloatingPointNumber;
import org.nakedobjects.object.defaults.value.Label;
import org.nakedobjects.object.defaults.value.Logical;
import org.nakedobjects.object.defaults.value.Money;
import org.nakedobjects.object.defaults.value.Option;
import org.nakedobjects.object.defaults.value.Percentage;
import org.nakedobjects.object.defaults.value.TextString;
import org.nakedobjects.object.defaults.value.Time;
import org.nakedobjects.object.defaults.value.TimeStamp;
import org.nakedobjects.object.defaults.value.URLString;
import org.nakedobjects.object.defaults.value.WholeNumber;


public class ValueObjectExample extends AbstractNakedObject {
    private final Date date = new Date();
    private final FloatingPointNumber floatingPoint = new FloatingPointNumber();
    private final Label label = new Label();
    private final Logical logical = new Logical();
    private final Money money = new Money();
    private final Option option = new Option(new String[] {
                "one", "two", "three", "four"
            });
    private final Percentage percentage = new Percentage();
    private final TextString textString = new TextString();
    private final Time time = new Time();
    private final TimeStamp timestamp = new TimeStamp();
    private final URLString urlString = new URLString();
    private final WholeNumber wholeNumber = new WholeNumber();

    public Title title() {
        return new Title("test values");
    }

    public TextString getTextString() {
        return textString;
    }

    public Date getDate() {
        return date;
    }

    public FloatingPointNumber getFloatingPoint() {
        return floatingPoint;
    }

    public Label getLabel() {
        return label;
    }

    public Logical getLogical() {
        return logical;
    }

    public Money getMoney() {
        return money;
    }

    public Option getOption() {
        return option;
    }

    public Percentage getPercentage() {
        return percentage;
    }

    public Time getTime() {
        return time;
    }

    public TimeStamp getTimeStamp() {
        return timestamp;
    }

    public URLString getUrlString() {
        return urlString;
    }

    public WholeNumber getWholeNumber() {
        return wholeNumber;
    }
}
