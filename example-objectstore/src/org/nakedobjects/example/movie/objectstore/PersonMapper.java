package org.nakedobjects.example.movie.objectstore;

import org.nakedobjects.application.value.Date;
import org.nakedobjects.example.movie.bom.Person;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Oid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;


public class PersonMapper implements SqlMapper {

    public PersonMapper() {
        super();
    }

    public NakedObject[] getInstances(Connection connection) throws SQLException {
        NakedObject[] instances = new NakedObject[45];
        int i = 0;
        String query = "select * from person";
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery(query);
        while (rs.next() && i < instances.length) {
            Oid oid = new SqlOid(rs.getInt(1));
            Person person;
            if(NakedObjects.getObjectLoader().isIdentityKnown(oid)) {
                instances[i++] = NakedObjects.getObjectLoader().getAdapterFor(oid);
                person = (Person) NakedObjects.getObjectLoader().getAdapterFor(oid).getObject();
            } else {
                person = new Person();
                person.setName(rs.getString(2));
                instances[i++] = NakedObjects.getObjectLoader().recreateAdapterForPersistent(oid, person);
            }
        }
        rs.close();
        s.close();
        NakedObject[] results = new NakedObject[i];
        System.arraycopy(instances, 0, results, 0, i);
        return results;
    }


    public void insert(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("insert into person (name, dob, id) values(?, ?, ?)");
        setParameters(object, id, s);
        s.execute();
        s.close();
    }

    private void setParameters(NakedObject object, int id, PreparedStatement s) throws SQLException {
        s.setInt(1, id);

        Person person = (Person) object.getObject();
        s.setString(2, person.getName());
        Date date = person.getDate();
        if(date == null) {
            s.setNull(3, Types.DATE);
        } else {
            s.setDate(3, new java.sql.Date(date.longValue()));
        }
    }



    public void update(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("update person set name = ?, dob = ? where id = ?");
        setParameters(object, id, s);
        s.execute();
        s.close();
    }

    public void delete(Connection connection, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("delete from person where id= ?");
        s.setInt(1, id);
        s.execute();
        s.close();
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