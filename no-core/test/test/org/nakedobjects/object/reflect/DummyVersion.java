package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.Version;

import java.util.Date;

public class DummyVersion implements Version {
    private final int value;

    public DummyVersion() {
        this(-13);
    }

    public DummyVersion(int value) {
        this.value = value;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof DummyVersion) {
            DummyVersion other = (DummyVersion) obj;
            return other.value == value;
        } 
        
        return false;
    }
    
    public boolean different(Version version) {
        return true;
    }

    public String toString() {
        return "TestVersion#" + value;
    }

    public String getUser() {
        return null;
    }

    public Date getTime() {
        return null;
    }

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