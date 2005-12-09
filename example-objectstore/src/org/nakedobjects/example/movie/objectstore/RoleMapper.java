package org.nakedobjects.example.movie.objectstore;

import org.nakedobjects.example.movie.bom.Person;
import org.nakedobjects.example.movie.bom.Role;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.ResolveState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;


/*
 * NOTES
 * 
 * With a role table that relates both movie and person we run into a problem in this mapper as the Role
 * object does not know the Movie it belongs to; only the Person. To make this mapper work we need to hold the
 * movie in the role. This still requires us to create the internal collection in the movie that references
 * all the roles.
 * 
 * To keep the Role as it is (without knowing the movie) then the table in the DB needs to have its own PK
 * rather than a relationship to the movie. Then we would need a table between the movies and the roles to
 * define the sets of roles for a collection.
 * 
 * Another alternative would be for the Role table to have both the FK to the movie and a PK to identify
 * itself with. This would allow us to find the set of roles for a movie using the FK, and to update the role
 * using the PK. The problem with dispensing with the PK and creating the OID from the movie and person FKs is
 * we don't know the movie (or parent object) when making the role persistent. In fact the role could only be
 * persisted if it is associated with a mover and a person - need valid objects here.
 */

public class RoleMapper implements SqlMapper {
    private static final Logger LOG = Logger.getLogger(RoleMapper.class);

    public RoleMapper() {
        super();
    }

    public NakedObject[] getInstances(Connection connection) throws SQLException {
        NakedObject[] instances = new NakedObject[45];
        int i = 0;
        String query = "select * from role";
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery(query);
        while (rs.next() && i < instances.length) {
            Oid oid = new SqlOid(rs.getInt(1));
            Person role;
            if (NakedObjects.getObjectLoader().isIdentityKnown(oid)) {
                instances[i++] = NakedObjects.getObjectLoader().getAdapterFor(oid);
                role = (Person) NakedObjects.getObjectLoader().getAdapterFor(oid).getObject();
            } else {
                NakedObject instance = NakedObjects.getObjectLoader().recreateAdapterForPersistent(oid,
                        NakedObjects.getSpecificationLoader().loadSpecification(Person.class));
                NakedObjects.getObjectLoader().start(instance, ResolveState.RESOLVING);
                role = (Person) instance.getObject();
                role.setName(rs.getString(2));
                NakedObjects.getObjectLoader().end(instance);
                instances[i++] = instance;
            }
        }
        rs.close();
        s.close();
        NakedObject[] results = new NakedObject[i];
        System.arraycopy(instances, 0, results, 0, i);
        return results;
    }

    public void insert(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("insert into role (movieFK, name, personFK) values(?, ?, ?)");
        setParameters(object, id, s);
        execute(s);
    }

    private void setParameters(NakedObject object, int id, PreparedStatement s) throws SQLException {
        Role role = (Role) object.getObject();
        s.setString(2, role.getName());
        Person actor = role.getActor();
        NakedObject actorAdapter = NakedObjects.getObjectLoader().getAdapterFor(actor);
        int actorId = ((SqlOid) actorAdapter.getOid()).getPrimaryKey();
        s.setInt(3, actorId);
    }

    public void update(Connection connection, NakedObject object, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("update role set name = ?, dob = ? where PKid = ?");
        setParameters(object, id, s);
        execute(s);
    }

    private void execute(PreparedStatement s) throws SQLException {
        LOG.debug(s);
        s.execute();
        s.close();
    }

    public void delete(Connection connection, int id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("delete from role where PKid= ?");
        s.setInt(1, id);
        execute(s);
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