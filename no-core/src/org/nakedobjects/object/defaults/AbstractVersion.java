package org.nakedobjects.object.defaults;

import org.nakedobjects.object.Version;

import java.util.Date;


public abstract class AbstractVersion implements Version {
    protected String user;
    protected Date time;
    
    public AbstractVersion(String user, Date time) {
        this.user = user;
        this.time = time;
    }

    public Date getTime() {
        return time;
    }
    
    public String getUser() {
        return user;
    }

    public Version next(String user, Date time) {
        AbstractVersion newVersion = next();
        newVersion.user = user;
        newVersion.time = time;
        return newVersion;
    }

    protected abstract AbstractVersion next();

}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */