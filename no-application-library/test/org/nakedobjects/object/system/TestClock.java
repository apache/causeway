package org.nakedobjects.object.system;

import org.nakedobjects.application.system.Clock;
import org.nakedobjects.application.valueholder.Date;
import org.nakedobjects.application.valueholder.DateTime;
import org.nakedobjects.application.valueholder.Time;
import org.nakedobjects.application.valueholder.TimeStamp;

import java.util.Calendar;

public class TestClock implements Clock {

    public TestClock() {
        Date.setClock(this);
        Time.setClock(this);
        DateTime.setClock(this);
        TimeStamp.setClock(this);
    }
    
    public long getTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
              
        c.set(Calendar.YEAR, 2003);
        c.set(Calendar.MONTH, 7);
        c.set(Calendar.DAY_OF_MONTH, 17);
        
        c.set(Calendar.HOUR_OF_DAY, 21);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 25);
        
        return c.getTime().getTime();
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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