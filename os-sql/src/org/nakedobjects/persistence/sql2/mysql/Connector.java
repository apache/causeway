package org.nakedobjects.persistence.sql2.mysql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.utility.ConfigurationParameters;


public class Connector {
    private static final Logger LOG = Logger.getLogger(Connector.class);
    private Connection connection;

    public void close() throws ObjectStoreException {
        try {
            if(connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new ObjectStoreException("Failed to close", e);
        }
    }

    public int count(String sql) throws ObjectStoreException {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            result.next();
            int count = result.getInt(1);
            statement.close();
            return count;
        } catch (SQLException e) {
            throw new ObjectStoreException("Failed count", e);
        }
    }

    public void delete(String sql) throws ObjectStoreException {
        update(sql);
    }

    public void open() throws ObjectStoreException {
        try {
            ConfigurationParameters params = ConfigurationParameters.getInstance();
            String BASE = "sql-object-store-2.";
            String driver = params.getString(BASE + "driver");
            String url = params.getString(BASE + "connection");
            String user = params.getString(BASE + "user");
            String password = params.getString(BASE + "password");

            if(driver == null) {
                throw new ObjectStoreException("No driver specified for database connection");
            }
            if(url == null) {
                throw new ObjectStoreException("No connection URL specified to database");
            }
            if(user == null) {
                throw new ObjectStoreException("No user specified for database connection");
            }
            if(password == null) {
                throw new ObjectStoreException("No password specified for database connection");
            }
            
            Class.forName(driver);
            LOG.info("Connecting to " + url + " as " + user);
            connection = DriverManager.getConnection(url, user, password);
            
            if(connection == null) {
               	throw new ObjectStoreException("No connection established to " + url);
            }
    
        } catch (SQLException e) {
            throw new ObjectStoreException("Failed to start", e);
        } catch (ClassNotFoundException e) {
            throw new ObjectStoreException("Could not find database driver", e);
        }
    }

    public CallableStatement  executeStoredProcedure(String name, Parameter[] parameters) {
    	StringBuffer sql = new StringBuffer("{call ");
    	sql.append(name);
    	sql.append(" (");
    	for (int i = 0; i < parameters.length; i++) {
    		sql.append(i == 0 ? "?" : ",?");
		}
    	sql.append(")}");
        LOG.debug("SQL: " + sql);
        
       CallableStatement statement;
        try {
        	statement = connection.prepareCall(sql.toString());
        	for (int i = 0; i < parameters.length; i++) {
        		LOG.debug("  setup param " + i + " " + parameters[i]);
        		parameters[i].setupParameter(i + 1, statement);
    		}
        	 LOG.debug("   execute ");
             statement.execute();
        	 for (int i = 0; i < parameters.length; i++) {
	        	parameters[i].retrieve(i + 1, statement);
        		LOG.debug("  retrieve param " + i + " " + parameters[i]);
    		}
        	 
        	 return statement;
        } catch (SQLException e) {
           throw new NakedObjectRuntimeException(e);
        }   
    }


    public ResultSet callStoredProcedure(String name, Parameter[] parameters) {
         try {
        	 return executeStoredProcedure(name, parameters).getResultSet();
        } catch (SQLException e) {
           throw new NakedObjectRuntimeException(e);
        }   
    }

    public ResultSet select(String sql) {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
           return statement.executeQuery();
        } catch (SQLException e) {
           throw new NakedObjectRuntimeException(e);
        }   
    }

    public void update(String sql) throws ObjectStoreException {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new ObjectStoreException("SQL error", e);
        }
    }

 
    
    
    
    public boolean hasTable(String tableName) throws ObjectStoreException {
        try {
            ResultSet set = connection.getMetaData().getTables(null, null, tableName, null);
            if(set.next()) {
                LOG.debug("Found " + set.getString("TABLE_NAME"));
                set.close();
                return true;
            } else {
                set.close();
                return false;
            }
        } catch (SQLException e) {
            throw new ObjectStoreException(e);
        }
    }

	public Lookup loadLookup(String key, String value, String table) throws ObjectStoreException {
		try{
		Hashtable lookup = new Hashtable();
		ResultSet rs = select("select " + key + "," + value + " from " + table);
		while(rs.next()) {
			lookup.put(rs.getString(key), rs.getString(value));
		}
		return new Lookup(lookup);
	} catch (SQLException e) {
        throw new ObjectStoreException(e);
    }
	}

	public void insert(String sql) throws ObjectStoreException {
		update(sql);
	}
	
	public void insert(String sql, Object oid) throws ObjectStoreException {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if(rs.next()) {
            	int id = rs.getInt(1);
            }
            statement.close();
        } catch (SQLException e) {
            throw new ObjectStoreException("SQL error", e);
        }
	}

	public Connection getConnection() {
		return connection;
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