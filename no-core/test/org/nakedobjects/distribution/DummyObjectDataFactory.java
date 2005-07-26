package org.nakedobjects.distribution;

import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.utility.NotImplementedException;


class DummyObjectDataFactory extends DataFactory {
    protected ObjectData createObjectData(Oid oid, String type, Object[] fieldContent, boolean resolved, long version) {
        return new DummyObjectData(oid, type, fieldContent, resolved, version);
    }

    public ValueData createValueData(String fullName, Object object) {
        return new DummyValueData(object, fullName);
    }

    protected ExceptionData createExceptionData(String type, String message, String trace) {
        throw new NotImplementedException();
    }
}

class DummyValueData implements ValueData {
    public Object value;
    public String type;
    
    
    public DummyValueData(Object value, String type) {
        super();
        this.value = value;
        this.type = type;
    }
    
    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
    
}

class DummyObjectData implements ObjectData {
    private final Oid oid;
    private final String type;
    private final Object[] fieldContent;
    private final boolean resolved;
    private final long version;

    public DummyObjectData(Oid oid, String type, Object[] fieldContent, boolean resolved, long version) {
        this.oid = oid;
        this.type = type;
        this.fieldContent = fieldContent;
        this.resolved = resolved;
        this.version = version;
    }

    public Object[] getFieldContent() {
        return fieldContent;
    }

    public Oid getOid() {
        return oid;
    }

    public String getType() {
        return type;
    }

    public boolean isResolved() {
        return resolved;
    }

    public long getVersion() {
        return version;
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