package org.nakedobjects.persistence.sql2.mysql;

import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.OidGenerator;
import org.nakedobjects.object.SimpleOid;
import org.nakedobjects.persistence.sql2.SqlOid;

import java.sql.ResultSet;
import java.sql.SQLException;


public class MySqlOidGenerator implements OidGenerator {
    private long number;

    public void init() {
        Connector db;
        db = new Connector();
        try {
            db.open();
            if(!db.hasTable("NO_SERIAL_ID")) {
                db.update("create table no_serial_id (number INTEGER)");
                db.update("insert into no_serial_id values (1)");
            }

            ResultSet rs = db.select("select number from no_serial_id");
            rs.next();
            number = rs.getLong("number");
            db.close();
        } catch (SQLException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    public String name() {
        return "Sql Oids";
    }

    public synchronized Object next() {
        return new SqlOid((int) number++, "");
    }

    public void shutdown() {
        Connector db;
        db = new Connector();
        try {
            db.open();
            db.update("update no_serial_id set number = " + number);
            db.close();
        } catch (ObjectStoreException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
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