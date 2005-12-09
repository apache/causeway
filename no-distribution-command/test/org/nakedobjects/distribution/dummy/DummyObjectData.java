package org.nakedobjects.distribution.dummy;

import org.nakedobjects.distribution.Data;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.Version;
import org.nakedobjects.utility.ToString;


public class DummyObjectData extends DummyReferenceData implements ObjectData {
    private Data[] fieldContent;
    private final boolean resolved;

    public DummyObjectData(Oid oid, String type, boolean resolved, final Version version) {
        super(oid, type, version);
        this.resolved = resolved;
    }

    public Data[] getFieldContent() {
        return fieldContent;
    }

    public boolean hasCompleteData() {
        return resolved;
    }

    public void setFieldContent(Data[] fieldContent) {
        this.fieldContent = fieldContent;        
    }
    
    /*public String toString() {
        ToString str = new ToString(this);
        toString(str);  
        return str.toString();
    }
*/
    protected void toString(ToString str) {
        super.toString(str);
        str.append("fields", fieldContent == null ? 0  : fieldContent.length);
        str.append("resolved", resolved);
    }
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        
        if(super.equals(obj)) {
            if (obj instanceof DummyObjectData) {
                DummyObjectData ref = (DummyObjectData) obj;
                if (resolved == ref.resolved) {
                    if(fieldContent == null && ref.fieldContent == null) {
                        return true;
                    }
                    
                    if(ref.fieldContent == null) {
                        return false;
                    }
                    
                    for (int i = 0; i < fieldContent.length; i++) {
                        if( !(fieldContent[i] == null ? ref.fieldContent[i] == null : fieldContent[i].equals(ref.fieldContent[i]))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;

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