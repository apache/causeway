package org.nakedobjects.persistence.sql.jdbc;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.ValueMapper;


public class JdbcTimeMapper implements ValueMapper {

    public String valueAsDBString(NakedValue value) throws SqlObjectStoreException {
        // converting to milliseconds
        //return "'" + new java.sql.Time(((Time) value).longValue() *
        // 1000).toString() + "'";
        String ts = new String(value.asEncodedString());
        if (ts.equals("NULL")) {
            return ts;
        }
        String dbts = ts.substring(0, 2) + ":" + ts.substring(2, 4) + ":00";
        return "'" + dbts + "'";
    }

    public void setFromDBColumn(String columnName, NakedObjectField field, NakedObject object, Results rs)
            throws SqlObjectStoreException {
        String val = rs.getString(columnName);
        // convert date to hhmm
        val = val.substring(0, 2) + val.substring(3, 5);
        val = val == null ? "NULL" : val;
        object.initValue((OneToOneAssociation) field, val);
        //        ((ValueFieldSpecification) field).restoreValue(object, val);
    }

    public String columnType() {
        return "TIME";
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