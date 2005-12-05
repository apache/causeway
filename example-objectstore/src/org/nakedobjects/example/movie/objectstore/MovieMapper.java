package org.nakedobjects.example.movie.objectstore;

import org.nakedobjects.example.movie.bom.Movie;
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


public class MovieMapper implements SqlMapper {

    public MovieMapper() {
        super();
    }

    public NakedObject[] getInstances(Connection connection) throws SQLException {
        NakedObject[] instances = new NakedObject[45];
        int i = 0;
        String query = "select movie.*, person.* from movie left join person on movie.directorFK = person.pkid";
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery(query);
        while (rs.next() && i < instances.length) {
            Oid oid = new SqlOid(rs.getInt(1));
            Movie movie;
            if(NakedObjects.getObjectLoader().isIdentityKnown(oid)) {
                instances[i++] = NakedObjects.getObjectLoader().getAdapterFor(oid);
                movie = (Movie) NakedObjects.getObjectLoader().getAdapterFor(oid).getObject();
            } else {
                movie = new Movie();
                movie.setName(rs.getString(2));
                instances[i++] = NakedObjects.getObjectLoader().recreateAdapterForPersistent(oid, movie);
            }
            
            
            int directorId = rs.getInt(3);
            if (directorId != 0) {
                Oid directorOid = new SqlOid(directorId);
                Person director;
                if (NakedObjects.getObjectLoader().isIdentityKnown(directorOid)) {
                    director = (Person) NakedObjects.getObjectLoader().getAdapterFor(directorOid);
                } else {
                    director = new Person();
                    NakedObjects.getObjectLoader().recreateAdapterForPersistent(directorOid, director);
                }
                director.setName(rs.getString(5));
                movie.setDirector(director);
            }
        }
        rs.close();
        s.close();
        NakedObject[] results = new NakedObject[i];
        System.arraycopy(instances, 0, results, 0, i);
        return results;
    }


    public void insert(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("insert into movie (name, director, pkid) values(?, ?, ?)");
        setParameters(object, id, s);
        s.execute();
        s.close();
    }

    private void setParameters(NakedObject object, int id, PreparedStatement s) throws SQLException {
        s.setInt(3, id);

        Movie movie = (Movie) object.getObject();
        s.setString(1, movie.getName());
        Person director = movie.getDirector();
        if (director == null) {
            s.setNull(2, Types.INTEGER);
        } else {
            NakedObject adapter = NakedObjects.getObjectLoader().getAdapterFor(director);
            int fkId = ((SqlOid) adapter.getOid()).getPrimaryKey();
            s.setInt(2, fkId);

        }
    }

    public void update(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("update movie set name = ?, director = ? where id = ?");
        setParameters(object, id, s);
        s.execute();
        s.close();
    }

    public void delete(Connection connection, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("delete from movie where id= ?");
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