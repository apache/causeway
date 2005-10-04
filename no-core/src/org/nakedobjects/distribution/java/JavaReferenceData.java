package org.nakedobjects.distribution.java;

import org.nakedobjects.distribution.ReferenceData;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.ToString;


public class JavaReferenceData implements ReferenceData {
    private final boolean hasVersion;
    private final Oid oid;
    private final String type;
    private final long version;

    public JavaReferenceData(String type, Oid oid, boolean hasVersion, long version) {
        this.type = type;
        this.oid = oid;
        this.hasVersion = hasVersion;
        this.version = version;
    }

    public Oid getOid() {
        return oid;
    }

    public String getType() {
        return type;
    }

    public long getVersion() {
        return version;
    }

    public boolean hasVersion() {
        return hasVersion;
    }

    public String toString() {
        ToString str = new ToString(this);
        str.append("type", type);
        str.append("oid", oid);
        str.append("version", version);
        return str.toString();
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