package org.nakedobjects.io;

import java.util.Enumeration;
import java.util.Hashtable;


class ObjectData extends Data {
    private final static Transferable NO_ENTRY = new Null();
    private final Hashtable fields = new Hashtable();

    public ObjectData(Object oid, String className) {
        super(oid, className);
    }

    public ObjectData(TransferableReader data) {
        super(data);

        int size = data.readInt();
        for (int i = 0; i < size; i++) {
            String key = data.readString();
            String type = data.readString();
            if (type.equals("O")) {
                Transferable object = data.readObject();
                fields.put(key, object);
            } else {
                String value = data.readString();
                fields.put(key, value);
            }
        }
    }

    public void addField(String fieldName, Object entry) {
        if (fields.containsKey(fieldName)) {
            throw new IllegalArgumentException("Field already entered " + fieldName);
        }
        fields.put(fieldName, entry == null ? NO_ENTRY : entry);
    }

    public void writeData(TransferableWriter data) {
        super.writeData(data);

        data.writeInt(fields.size());
        for (Enumeration f = fields.keys(); f.hasMoreElements();) {
            String key = (String) f.nextElement();
            Object value = fields.get(key);

            data.writeString(key);
            if (value instanceof Data  || value instanceof Null) {
                data.writeString("O");
                data.writeObject((Transferable) value);
            } else {
                data.writeString("S");
                data.writeString((String) value);
            }
        }
    }

    public Object getEntry(String fieldName) {
        Object entry = fields.get(fieldName);
        return entry == null || entry.getClass() == NO_ENTRY.getClass() ? null : entry;
    }

    public String toString() {
        return fields.toString();
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