package org.nakedobjects.distribution.java;

import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.object.persistence.Oid;


public class JavaObjectData implements ObjectData {
    private final Object fieldContent[];
    private final Oid oid;
    private final String type;
    private boolean resolved;
    private long version;

    public JavaObjectData(Oid oid, String type, Object[] fieldContent, boolean resolved) {
        this.oid = oid;
        this.type = type;
        this.fieldContent = fieldContent;
        this.resolved = resolved;
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
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("JavaDataObject [type=");
        str.append(type);
        str.append(",oid=");
        str.append(oid); 
        str.append(",fields=");
        for (int i = 0; i < fieldContent.length; i++) {
            if(i > 0) {
                str.append(";");
            }
	        if(fieldContent[i] == null) {
	            str.append("null");
	        } else {
	            String name = fieldContent[i].getClass().getName();
                str.append(name.substring(name.lastIndexOf('.') + 1));
	        }
        }
        return str.toString();
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