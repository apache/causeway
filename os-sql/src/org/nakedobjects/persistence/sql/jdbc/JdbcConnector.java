package org.nakedobjects.persistence.sql.jdbc;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.persistence.sql.AbstractDatabaseConnector;
import org.nakedobjects.persistence.sql.Results;
import org.nakedobjects.persistence.sql.SqlObjectStoreException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;


public class JdbcConnector extends AbstractDatabaseConnector {
    private static final Logger LOG = Logger.getLogger(JdbcConnector.class);
    private Connection connection;
    private boolean isUsed;

    public void close() throws SqlObjectStoreException {
        try {
            if(connection != null) {
                LOG.debug("close");
                connection.close();
            }
        } catch (SQLException e) {
            throw new SqlObjectStoreException("Failed to close", e);
        }
    }

    public int count(String sql) throws SqlObjectStoreException {
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
            throw new SqlObjectStoreException("Failed count", e);
        }
    }

    public void delete(String sql) throws SqlObjectStoreException {
        update(sql);
    }

    public void open() throws SqlObjectStoreException {
        try {
            Configuration params = NakedObjects.getConfiguration();
            String BASE = "sql-object-store.jdbc.";
            String driver = params.getString(BASE + "driver");
            String url = params.getString(BASE + "connection");
            String user = params.getString(BASE + "user");
            String password = params.getString(BASE + "password");

            if(driver == null) {
                throw new SqlObjectStoreException("No driver specified for database connection");
            }
            if(url == null) {
                throw new SqlObjectStoreException("No connection URL specified to database");
            }
            if(user == null) {
                throw new SqlObjectStoreException("No user specified for database connection");
            }
            if(password == null) {
                throw new SqlObjectStoreException("No password specified for database connection");
            }
            
            Class.forName(driver);
            LOG.info("Connecting to " + url + " as " + user);
            connection = DriverManager.getConnection(url, user, password);
            
            if(connection == null) {
               	throw new SqlObjectStoreException("No connection established to " + url);
            }
    
        } catch (SQLException e) {
            throw new SqlObjectStoreException("Failed to start", e);
        } catch (ClassNotFoundException e) {
            throw new SqlObjectStoreException("Could not find database driver", e);
        }
    }
/*
   public void executeStoredProcedure(StoredProcedure storedProcedure) {
       Parameter[] parameters = storedProcedure.getParameters();
    	StringBuffer sql = new StringBuffer("{call ");
    	sql.append(storedProcedure.getName());
    	sql.append(" (");
    	for (int i = 0, no = parameters.length; i < no; i++) {
    		sql.append(i == 0 ? "?" : ",?");
		}
    	sql.append(")}");
        LOG.debug("SQL: " + sql);
 
       CallableStatement statement;
         try {
         	statement = connection.prepareCall(sql.toString());
          	
         	for (int i = 0; i < parameters.length; i++) {
         		LOG.debug("  setup param " + i + " " + parameters[i]);
         		parameters[i].setupParameter(i + 1, parameters[i].getName(), storedProcedure);
     		}
         	 LOG.debug("   execute ");
              statement.execute();
         	 for (int i = 0; i < parameters.length; i++) {
 	        	parameters[i].retrieve(i + 1, parameters[i].getName(), storedProcedure);
         		LOG.debug("  retrieve param " + i + " " + parameters[i]);
     		}
          } catch (SQLException e) {
            throw new NakedObjectRuntimeException(e);
         }   


   }
    
    
    public MultipleResults executeStoredProcedure(String name, Parameter[] parameters) {
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
        	
        	StoredProcedure storedProcedure = new JdbcStoredProcedure(statement);
        	
        	for (int i = 0; i < parameters.length; i++) {
        		LOG.debug("  setup param " + i + " " + parameters[i]);
        		parameters[i].setupParameter(i + 1, parameters[i].getName(), storedProcedure);
    		}
        	 LOG.debug("   execute ");
             statement.execute();
        	 for (int i = 0; i < parameters.length; i++) {
	        	parameters[i].retrieve(i + 1, parameters[i].getName(), storedProcedure);
        		LOG.debug("  retrieve param " + i + " " + parameters[i]);
    		}
        	 
        	 return new JdbcResults(statement);
        } catch (SQLException e) {
           throw new NakedObjectRuntimeException(e);
        }   
        
    }
*/

     public Results select(String sql) {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
           return new JdbcResults(statement.executeQuery());
        } catch (SQLException e) {
           throw new NakedObjectRuntimeException(e);
        }   
    }

    public int update(String sql) throws SqlObjectStoreException {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            int updateCount = statement.executeUpdate();
            statement.close();
            return updateCount;
        } catch (SQLException e) {
            throw new SqlObjectStoreException("SQL error", e);
        }
    }

 
    
    
    
    public boolean hasTable(String tableName) throws SqlObjectStoreException {
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
            throw new SqlObjectStoreException(e);
        }
    }

	public void insert(String sql) throws SqlObjectStoreException {
		update(sql);
	}
	
	public void insert(String sql, Object oid) throws SqlObjectStoreException {
        LOG.debug("SQL: " + sql);
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
  /* require 3.0    
   *       ResultSet rs = statement.getGeneratedKeys();
            if(rs.next()) {
            	int id = rs.getInt(1);
            }
      */      statement.close();
        } catch (SQLException e) {
            throw new SqlObjectStoreException("SQL error", e);
        }
	}

    public Connection getConnection() {
		return connection;
	}

    public void commit() throws SqlObjectStoreException {
        try {
            LOG.debug("commit");
            connection.commit();
        } catch (SQLException e) {
            throw new SqlObjectStoreException("Commit error", e);
        }
    }

    public void rollback() throws SqlObjectStoreException {
        try {
            LOG.debug("commit");
            connection.rollback();
        } catch (SQLException e) {
            throw new SqlObjectStoreException("Rollback error", e);
        }
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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