package org.nakedobjects.persistence.sql.jdbc;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.ValueFieldSpecification;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;
import org.nakedobjects.persistence.sql.ValueMapper;

public class JdbcDateMapper implements ValueMapper {

    public String valueAsDBString(NakedValue value) throws SqlObjectStoreException {
       	if(value == null) {
    		return "NULL";
    	} else {
    		String ts = value.asEncodedString();
    		if(ts.equals("NULL")) {
    			return ts;
    		}
        	String dbts = ts.substring(0,4) + "-" + ts.substring(4,6) + "-" + ts.substring(6,8);
        	return "'" + dbts + "'";
    	}

    }

    public void setFromDBColumn(String columnName, FieldSpecification field, NakedObject object, Results rs) throws SqlObjectStoreException {       
        String val = rs.getString(columnName);
		
        // convert date to yyyymmdd
        val = val.substring(0,4) + val.substring(5,7) + val.substring(8,10);
		
		val = val == null ? "NULL" : val;
		
		((ValueFieldSpecification) field).restoreValue(object, val);
    }
    
    public String columnType() {
        return "DATETIME";
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