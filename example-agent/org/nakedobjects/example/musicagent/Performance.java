package org.nakedobjects.example.musicagent;

import org.nakedobjects.application.Title;
import org.nakedobjects.application.value.Date;
import org.nakedobjects.application.value.TextString;
import org.nakedobjects.application.value.Time;

import java.util.Vector;


public class Performance extends BaseObject {

    public static String fieldOrder() {
        return "description, date, start time, end time";
    }

    private final Date date = new Date();
    private final TextString description = new TextString();
    private final Time endTime = new Time();
    private final Vector parts = new Vector();
    private final Time startTime = new Time();

    public void addParts(Part part) {
        parts.add(part);
        part.setPerformance(this);
    }

    public Date getDate() {
        return date;
    }

    public TextString getDescription() {
        return description;
    }

    public Time getEndTime() {
        return endTime;
    }

    public Vector getParts() {
        return parts;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void removeParts(Part part) {
        parts.remove(part);
        part.setPerformance(null);
    }

    public Title title() {
        return date.title().append(description);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */

