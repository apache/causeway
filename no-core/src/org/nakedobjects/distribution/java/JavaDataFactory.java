package org.nakedobjects.distribution.java;

import org.nakedobjects.distribution.CollectionData;
import org.nakedobjects.distribution.Data;
import org.nakedobjects.distribution.DataFactory;
import org.nakedobjects.distribution.NullData;
import org.nakedobjects.distribution.ObjectData;
import org.nakedobjects.distribution.ReferenceData;
import org.nakedobjects.distribution.ValueData;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.persistence.Oid;


public class JavaDataFactory extends DataFactory {

    protected CollectionData createCollectionData(Oid oid, String type, ObjectData[] elements, boolean hasAllElements, Version version) {
        return new JavaCollectionData(oid, type, elements, hasAllElements, version);
    }

    protected NullData createNullData(String type) {
        return new JavaNullData(type);
    }

    public ObjectData createObjectData(Oid oid, String type, Data[] fieldContent, boolean hasCompleteData, Version version) {
        return new JavaObjectData(oid, type, fieldContent, hasCompleteData,  version);
    }

    protected ReferenceData createReferenceData(String type, Oid oid, Version version) {
        return new JavaReferenceData(type, oid, version);
    }

    public ValueData createValueData(String type, Object value) {
        return new JavaValueData(type, value);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */