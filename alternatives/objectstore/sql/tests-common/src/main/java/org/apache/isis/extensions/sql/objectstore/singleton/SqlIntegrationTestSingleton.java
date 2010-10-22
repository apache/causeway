/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


/**
 * 
 */
package org.apache.isis.extensions.sql.objectstore.singleton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.isis.metamodel.config.internal.PropertiesConfiguration;
import org.apache.isis.runtime.testsystem.SqlDataClassFactory;
import org.apache.isis.runtime.testsystem.TestProxySystemIII;
import org.apache.isis.runtime.testsystem.dataclasses.SimpleClass;
import org.apache.isis.runtime.testsystem.dataclasses.SqlDataClass;
//import org.postgresql.util.PSQLException;

/**
 * @author Kevin
 *
 */
public class SqlIntegrationTestSingleton {
	static SqlIntegrationTestSingleton instance;
	public static  SqlIntegrationTestSingleton getInstance(){
		if (instance == null){
			instance = new SqlIntegrationTestSingleton();
		}
		return instance;
	}
	
	
	private int state = 0;
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	private String persistorName;
	private SqlDataClassFactory sqlDataClassFactory = null;
	private TestProxySystemIII system = null;
	public void initNOF(String propertiesDirectory, String propertiesFileName) throws FileNotFoundException, IOException,
		SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException{
		
		Properties properties = new Properties();
		properties.load(new FileInputStream(propertiesDirectory+"/"+propertiesFileName));
		this.initNOF(properties);
	}
	
	public void initNOF(Properties properties) throws  SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.add(properties);
        persistorName = configuration.getString("isis.persistence");
        
		resetPersistorState(configuration);

		sqlDataClassFactory = new SqlDataClassFactory();
		if (system != null){
			system.shutDown();
		}
        system = new TestProxySystemIII();
        system.setConfiguration(configuration);
        system.init(sqlDataClassFactory);
	}

    // JDBC
    private Connection c = null;
    private Statement s = null;
	private SqlDataClass sqlDataClass;
    
	@SuppressWarnings("unchecked")
	private void resetPersistorState(PropertiesConfiguration PropertiesConfiguration) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException{
		String jdbcClassName = PropertiesConfiguration.getString("isis.persistence.sql.jdbc.driver");
		if (jdbcClassName == null){
			c = null;
			s = null;
			return;
		}
		Class<Driver> driverClass = (Class<Driver>) Class.forName(jdbcClassName);
		Driver driver = driverClass.newInstance();
		DriverManager.registerDriver(driver);
		
        // jdbc - connect to DB and drop tables.
    	c = DriverManager.getConnection(
	    		PropertiesConfiguration.getString("isis.persistence.sql.jdbc.connection"),
                PropertiesConfiguration.getString("isis.persistence.sql.jdbc.user"), 
                PropertiesConfiguration.getString("isis.persistence.sql.jdbc.password"));
		s = c.createStatement();
			
	    dropTable("no_services");
	}
	private void dropTable(String tableName) {
		if (s == null){
			if (persistorName == "xml"){
				// Delete the xml files..
			}
			
			if (tableName == "sqldataclass"){
				List<SqlDataClass> list = sqlDataClassFactory.allDataClasses();
				for (SqlDataClass sqlDataClass : list) {
					sqlDataClassFactory.delete(sqlDataClass);
				}
				return;
			}
			if (tableName == "simpleclass"){
				List<SimpleClass> list = sqlDataClassFactory.allSimpleClasses();
				for (SimpleClass sqlClass : list) {
					sqlDataClassFactory.delete(sqlClass);
				}
				return;
			}
			return;
		}
		/**/
		try {
			s.executeUpdate("DROP TABLE "+tableName+";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		/**/
	}
	
	public void sqlExecute(String sqlString) throws SQLException{
		if (s != null){
			s.executeUpdate(sqlString);
		}
	}

	public static SqlDataClassFactory getSqlDataClassFactory(){
		return getInstance().sqlDataClassFactory;
	}
    
	public static void drop(String tableName){
		getInstance().dropTable(tableName);
	}
	
	public void shutDown(){
		if (system != null){
			system.shutDown();
		}
	}
	
	public static void setPerson(SqlDataClass person) {
		getInstance().setSqlDataClass(person);
		
	}
	public static SqlDataClass getPerson() {
		return getInstance().getSqlDataClass();
		
	}
	
	private void setSqlDataClass(SqlDataClass person) {
		this.sqlDataClass = person;
		
	}
	/**
	 * @return the sqlDataClass
	 */
	public SqlDataClass getSqlDataClass() {
		return sqlDataClass;
	}
}
