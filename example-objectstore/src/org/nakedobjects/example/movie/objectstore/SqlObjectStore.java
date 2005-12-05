package org.nakedobjects.example.movie.objectstore;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.persistence.objectstore.NakedObjectStore;
import org.nakedobjects.object.transaction.CreateObjectCommand;
import org.nakedobjects.object.transaction.DestroyObjectCommand;
import org.nakedobjects.object.transaction.PersistenceCommand;
import org.nakedobjects.object.transaction.SaveObjectCommand;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;

import com.mysql.jdbc.Driver;


public class SqlObjectStore implements NakedObjectStore {
    private Hashtable mappers = new Hashtable();

    public void abortTransaction() {}

    public CreateObjectCommand createCreateObjectCommand(NakedObject object) {
        return new CreateCommand(object, getMapper(object.getSpecification()));
    }

    private static class CreateCommand implements CreateObjectCommand, SqlCommand {
        private final NakedObject object;
        private final SqlMapper mapper;

        public CreateCommand(NakedObject object, SqlMapper mapper) {
            this.object = object;
            this.mapper = mapper;
        }

        public void execute(Connection connection) throws SQLException {
            int id = ((SqlOid) object.getOid()).getPrimaryKey();
            mapper.insert(connection, object, id);
        }

        public void execute() throws ObjectPerstsistenceException {}

        public NakedObject onObject() {
            return object;
        }
    }

    private static class DestroyCommand implements DestroyObjectCommand, SqlCommand {
        private final NakedObject object;
        private final SqlMapper mapper;

        public DestroyCommand(NakedObject object, SqlMapper mapper) {
            this.object = object;
            this.mapper = mapper;
        }

        public void execute(Connection connection) throws SQLException {
            int id = ((SqlOid) object.getOid()).getPrimaryKey();
            mapper.delete(connection, id);
        }

        public void execute() throws ObjectPerstsistenceException {}

        public NakedObject onObject() {
            return object;
        }
    }

    public DestroyObjectCommand createDestroyObjectCommand(NakedObject object) {
        return new DestroyCommand(object, getMapper(object.getSpecification()));
    }

    public SaveObjectCommand createSaveObjectCommand(NakedObject object) {
        return new SaveCommand(object, getMapper(object.getSpecification()));
    }

    private static class SaveCommand implements SaveObjectCommand, SqlCommand {
        private final NakedObject object;
        private final SqlMapper mapper;

        public SaveCommand(NakedObject object, SqlMapper mapper) {
            this.object = object;
            this.mapper = mapper;
        }

        public void execute(Connection connection) throws SQLException {
            int id = ((SqlOid) object.getOid()).getPrimaryKey();
            mapper.update(connection, object, id);
        }

        public void execute() throws ObjectPerstsistenceException {}

        public NakedObject onObject() {
            return object;
        }
    }

    public void endTransaction() {}

    public NakedObject[] getInstances(InstancesCriteria criteria) {
        return null;
    }

    public NakedObject[] getInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/movies", "rcm", "rcm");
            return getMapper(specification).getInstances(connection);
        } catch (SQLException e) {
            throw new ObjectStoreException("Failed to getInstaneces", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new ObjectStoreException("Failed to close connection", e);
                }
            }
        }
    }

    private SqlMapper getMapper(NakedObjectSpecification specification) {
        SqlMapper mapper = (SqlMapper) mappers.get(specification.getFullName());
        if (mapper == null) {
            throw new ObjectStoreException("No mapper for " + specification.getFullName());
        }
        return mapper;
    }

    public NakedClass getNakedClass(String name) {
        throw new ObjectNotFoundException("this is needed!!!!");
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException,
            ObjectPerstsistenceException {
        return null;
    }

    public boolean hasInstances(NakedObjectSpecification specification, boolean includeSubclasses) {
        return true;
    }

    public String name() {
        return null;
    }

    public int numberOfInstances(NakedObjectSpecification specification, boolean includedSubclasses) {
        return 1;
    }

    public void resolveField(NakedObject object, NakedObjectField field) {}

    public void resolveImmediately(NakedObject object) {}

    public void runTransaction(PersistenceCommand[] commands) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/movies", "rcm", "rcm");
            connection.setAutoCommit(false);
            for (int i = 0; i < commands.length; i++) {
                ((SqlCommand) commands[i]).execute(connection);
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            throw new ObjectStoreException("Failed to run transaction", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new ObjectStoreException("Failed to close connection", e);
                }
            }
        }

    }

    public void startTransaction() {}

    public void reset() {}

    public void init() {
        String driver = Driver.class.getName();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new ObjectStoreException("Failed to loade JDBC driver " + driver, e);
        }
    }

    public void shutdown() {}

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public void addMapper(Class cls, SqlMapper mapper) {
        mappers.put(cls.getName(), mapper);
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