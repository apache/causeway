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
package org.apache.isis.objectstore.jdo.datanucleus;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class Utils {

    private Utils(){}

    public static IsisSystemWithFixtures.Builder systemBuilder() {
        return IsisSystemWithFixtures.builder()
        .with(configurationForDataNucleusDb())
        .with(new DataNucleusPersistenceMechanismInstaller());
    }

    public static IsisSystemWithFixtures.Listener listenerToDeleteFrom(final String... tables) {
        return new IsisSystemWithFixtures.ListenerAdapter(){

            @Override
            public void postSetupSystem(boolean firstTime) throws Exception {
                Connection connection = getConnection();
                try {
                    final Statement statement = connection.createStatement();
                    for(String table: tables) {
                        statement.executeUpdate("DELETE FROM " + table);
                    }
                } catch(Exception ex) {
                    connection.rollback();
                    throw ex;
                } finally {
                    connection.commit();
                }
            }

            private Connection getConnection() {
                final DataNucleusObjectStore objectStore = (DataNucleusObjectStore) IsisContext.getPersistenceSession().getObjectStore();
                return objectStore.getJavaSqlConnection();
            }
        };
    }

    public static IsisConfiguration configurationForDataNucleusDb() {
        final IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        Properties props = new Properties();
        
        props.put("isis.persistor.datanucleus.impl.javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");

        // last one wins!
        configureHsqlDbFileBased(props);
        configureForMsSqlServer(props);
        configureHsqlDbInMemory(props);

        props.put("isis.persistor.datanucleus.impl.datanucleus.autoCreateSchema", "true");
        props.put("isis.persistor.datanucleus.impl.datanucleus.validateTables", "true");
        props.put("isis.persistor.datanucleus.impl.datanucleus.validateConstraints", "true");
        
        props.put("isis.persistor.datanucleus.impl.datanucleus.cache.level2.type", "none");

        configuration.add(props);
        return configuration;
    }


    private static void configureHsqlDbInMemory(Properties props) {
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionUserName", "sa");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionPassword", "");
    }

    private static void configureHsqlDbFileBased(Properties props) {
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL", "jdbc:hsqldb:file:hsql-db/test;hsqldb.write_delay=false;shutdown=true");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionUserName", "sa");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionPassword", "");
    }

    private static void configureForMsSqlServer(Properties props) {
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionDriverName", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL", "jdbc:sqlserver://127.0.0.1:1433;instance=SQLEXPRESS;databaseName=jdo;");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionUserName", "jdo");
        props.put("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionPassword", "jdopass");
    }

    

    public static long toMillis(int year, int monthOfYear, int dayOfMonth) {
        LocalDate d = new LocalDate(year, monthOfYear, dayOfMonth);
        return d.toDateMidnight().getMillis();
    }

    public static long toMillis(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute) {
        LocalDateTime d = new LocalDateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute);
        return d.toDateTime().getMillis();
    }

}
