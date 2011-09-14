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
package org.apache.isis.runtimes.dflt.objectstores.sql.singleton;

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

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.runtimes.dflt.objectstores.sql.Sql;
import org.apache.isis.runtimes.dflt.objectstores.sql.SqlObjectStore;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.SqlDataClassFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.TestProxySystemIII;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.NumericTestClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SqlDataClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyTestClass;

/**
 * @author Kevin
 * 
 */
public class SqlIntegrationTestSingleton {
    static SqlIntegrationTestSingleton instance;

    public static SqlIntegrationTestSingleton getInstance() {
        if (instance == null) {
            instance = new SqlIntegrationTestSingleton();
        }
        return instance;
    }

    private int state = 0;

    public int getState() {
        return state;
    }

    public void setState(final int state) {
        this.state = state;
    }

    private String persistorName;
    private SqlDataClassFactory sqlDataClassFactory = null;
    private TestProxySystemIII system = null;

    public void initNOF(final String propertiesDirectory, final String propertiesFileName)
        throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, InstantiationException,
        IllegalAccessException {

        final Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesDirectory + "/" + propertiesFileName));
        this.initNOF(properties);
    }

    public void initNOF(final Properties properties) throws SQLException, ClassNotFoundException,
        InstantiationException, IllegalAccessException {
        final IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        configuration.add(properties);
        persistorName = configuration.getString("isis.persistor");

        sqlDataClassFactory = new SqlDataClassFactory();
        if (system != null) {
            system.shutDown();
        }
        system = new TestProxySystemIII();
        system.setConfiguration(configuration);
        system.init(sqlDataClassFactory);

        resetPersistorState(configuration);

    }

    // JDBC
    private Connection c = null;
    private Statement s = null;
    private SqlDataClass sqlDataClass;
    private PolyTestClass polyTestClass;

    @SuppressWarnings("unchecked")
    private void resetPersistorState(final IsisConfigurationDefault IsisConfigurationDefault) throws SQLException,
        ClassNotFoundException, InstantiationException, IllegalAccessException {
        final String jdbcClassName = IsisConfigurationDefault.getString(SqlObjectStore.BASE_NAME + ".jdbc.driver");
        if (jdbcClassName == null) {
            c = null;
            s = null;
            return;
        }
        final Class<Driver> driverClass = (Class<Driver>) Class.forName(jdbcClassName);
        final Driver driver = driverClass.newInstance();
        DriverManager.registerDriver(driver);

        // jdbc - connect to DB and drop tables.
        c =
            DriverManager.getConnection(
                IsisConfigurationDefault.getString(SqlObjectStore.BASE_NAME + ".jdbc.connection"),
                IsisConfigurationDefault.getString(SqlObjectStore.BASE_NAME + ".jdbc.user"),
                IsisConfigurationDefault.getString(SqlObjectStore.BASE_NAME + ".jdbc.password"));
        s = c.createStatement();

        // dropTable(SqlObjectStore.getTableName());
    }

    private void dropTable(final String tableName) {
        if (s == null) {
            if (persistorName == "xml") {
                // Delete the xml files..
            }

            if (tableName.equalsIgnoreCase("sqldataclass")) {
                final List<SqlDataClass> list = sqlDataClassFactory.allDataClasses();
                for (final SqlDataClass sqlDataClass : list) {
                    sqlDataClassFactory.delete(sqlDataClass);
                }
                return;
            } else if (tableName.equalsIgnoreCase("simpleclass")) {
                final List<SimpleClass> list = sqlDataClassFactory.allSimpleClasses();
                for (final SimpleClass sqlClass : list) {
                    sqlDataClassFactory.delete(sqlClass);
                }
                return;
            } else if (tableName.equalsIgnoreCase("simpleclasstwo")) {
                final List<SimpleClassTwo> list = sqlDataClassFactory.allSimpleClassTwos();
                for (final SimpleClassTwo sqlClass : list) {
                    sqlDataClassFactory.delete(sqlClass);
                }
                return;
            } else if (tableName.equalsIgnoreCase("numerictestclass")) {
                final List<NumericTestClass> list = sqlDataClassFactory.allNumericTestClasses();
                for (final NumericTestClass sqlClass : list) {
                    sqlDataClassFactory.delete(sqlClass);
                }
                return;
            } else {
                throw new IsisException("Unknown table: " + tableName);
            }
        }
        /**/
        try {
            String tableIdentifier;
            if (tableName.substring(0, 4).toUpperCase().equals("ISIS")) {
                tableIdentifier = Sql.tableIdentifier(tableName);
            } else {
                tableIdentifier = Sql.tableIdentifier("isis_" + tableName);
            }
            s.executeUpdate("DROP TABLE " + tableIdentifier);
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        /**/
    }

    public void sqlExecute(final String sqlString) throws SQLException {
        if (s != null) {
            s.executeUpdate(sqlString);
        }
    }

    public static SqlDataClassFactory getSqlDataClassFactory() {
        return getInstance().sqlDataClassFactory;
    }

    public static void drop(final String tableName) {
        getInstance().dropTable(tableName);
    }

    public void shutDown() {
        if (system != null) {
            system.shutDown();
        }
    }

    // {{ SqlDataClass support
    public static void setDataClass(final SqlDataClass person) {
        getInstance().setSqlDataClass(person);

    }

    public static SqlDataClass getDataClass() {
        return getInstance().getSqlDataClass();

    }

    private void setSqlDataClass(final SqlDataClass person) {
        this.sqlDataClass = person;

    }

    /**
     * @return the sqlDataClass
     */
    public SqlDataClass getSqlDataClass() {
        return sqlDataClass;
    }

    // }}

    // {{ PolyTestClass support
    public static void setStaticPolyTestClass(final PolyTestClass polyTestClass) {
        getInstance().setPolyTestClass(polyTestClass);
    }

    public static PolyTestClass getStaticPolyTestClass() {
        return getInstance().getPolyTestClass();
    }

    private void setPolyTestClass(final PolyTestClass polyTestClass) {
        this.polyTestClass = polyTestClass;
    }

    public PolyTestClass getPolyTestClass() {
        return polyTestClass;
    }
    // }}

}
