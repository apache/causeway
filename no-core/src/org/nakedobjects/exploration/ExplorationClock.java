package org.nakedobjects.exploration;

import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.DateTime;
import org.nakedobjects.object.value.Time;
import org.nakedobjects.object.value.TimeStamp;
import org.nakedobjects.system.SystemClock;

import java.util.Calendar;


/**
 * This clock, used by Exploration, can be set to specific time. If not set it
 * will provide the time provided by the system clock.
 * 
 * @see org.nakedobjects.system.SystemClock
 * 
 * TODO add a mechanism to allow the clock to tick - each time the getTime
 * method is called the time should be updated so it moved on whatever the
 * elapsed time has been since the last call.
 */

public class ExplorationClock extends SystemClock {
    private Calendar time = null;

    public ExplorationClock() {
        Date.setClock(this);
        Time.setClock(this);
        DateTime.setClock(this);
        TimeStamp.setClock(this);
    }

    public static  ExplorationClock initialize() {
        ExplorationClock clock = new ExplorationClock();
        
        return clock;
    }
    
    public long getTime() {
        if (time == null) {
            return super.getTime();
        } else {
            return time.getTime().getTime();
        }
    }

    public void setTime(int hour, int min) {
        getCalendar();
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, min);
    }

    public void setDate(int year, int month, int day) {
        getCalendar();
        time.set(Calendar.YEAR, year);
        time.set(Calendar.MONTH, month - 1);
        time.set(Calendar.DAY_OF_MONTH, day);
    }

    private void getCalendar() {
        if (time == null) {
            time = Calendar.getInstance();
        }
    }

    public void reset() {
        time = null;
    }
    
    public String toString() {
        return time.getTime().toString();
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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