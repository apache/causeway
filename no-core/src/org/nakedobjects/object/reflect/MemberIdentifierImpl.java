package org.nakedobjects.object.reflect;

import org.nakedobjects.object.NakedObjectSpecification;

public class MemberIdentifierImpl implements MemberIdentifier {
    private final String className;
    private final String name;
    private final String[] parameters;
    private final boolean isField;
    
    public MemberIdentifierImpl(String className) {
        this.className = className;
        name = "";
        parameters = new String[0];
        isField = false;
    }

    public MemberIdentifierImpl(String className, String fieldName) {
        this.className = className;
        name = fieldName;
        parameters = new String[0];
        isField = true;
    }

    public MemberIdentifierImpl(String className, String methodName, NakedObjectSpecification[] specifications) {
        this.className = className;
        name =  methodName;
        parameters = new String[specifications == null ? 0 : specifications.length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = specifications[i].getFullName();
        }
        isField = false;
    }
    
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(obj instanceof MemberIdentifierImpl) {
            MemberIdentifierImpl other = (MemberIdentifierImpl) obj;
            return equals(other.className, className) && equals(other.name, other.name) && equals(other.parameters, parameters);
        }
        return false;
    }

    private boolean equals(String[] a, String[] b) {
        if(a == null && b == null) {
            return true;
       }

        if(a == null && b != null) {
            return false;
        }

        if(a != null && b == null) {
            return false;
        }

        if(a.length != b.length) {
            return false;
        }
        
        for (int i = 0; i < b.length; i++) {
            if(!a[i].equals(b[i])) {
                return false;
            }
        }
        
        return true;
    }

    private boolean equals(String a, String b) {
        if(a == b) {
            return true;
        }
        
        if(a != null) {
            return a.equals(b);
        }
        
        return false;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getName() {
        return name;
    }
    
    public String[] getParameters() {
        return parameters;
    }
    
    public boolean isField() {
        return isField;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(className);
        str.append('#');
        str.append(name);
        str.append('(');
        for (int i = 0; i < parameters.length; i++) {
            if(i > 0) {
                str.append(", ");
            }
            str.append(parameters[i]);
        }
        str.append(')');
        return str.toString();
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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