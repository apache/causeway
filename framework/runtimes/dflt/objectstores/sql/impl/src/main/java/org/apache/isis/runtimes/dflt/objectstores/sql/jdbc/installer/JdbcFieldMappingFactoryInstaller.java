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

package org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.installer;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.runtimes.dflt.objectstores.sql.FieldMappingFactoryInstaller;
import org.apache.isis.runtimes.dflt.objectstores.sql.FieldMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcBinaryValueMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcColorValueMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcDateMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcDateTimeMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcGeneralValueMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcMoneyValueMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcObjectReferenceFieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcObjectReferenceMappingFactory;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcTimeMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcTimestampMapper;

public class JdbcFieldMappingFactoryInstaller implements FieldMappingFactoryInstaller {

    @Override
    public void load(final FieldMappingLookup lookup) {
        lookup.addFieldMappingFactory(boolean.class, new JdbcBinaryValueMapper.Factory(JdbcConnector.TYPE_BOOLEAN()));
        lookup.addFieldMappingFactory(short.class, new JdbcBinaryValueMapper.Factory(JdbcConnector.TYPE_SHORT()));
        lookup.addFieldMappingFactory(int.class, new JdbcBinaryValueMapper.Factory(JdbcConnector.TYPE_INT()));
        lookup.addFieldMappingFactory(long.class, new JdbcBinaryValueMapper.Factory(JdbcConnector.TYPE_LONG()));
        lookup.addFieldMappingFactory(Float.class, new JdbcBinaryValueMapper.Factory(JdbcConnector.TYPE_FLOAT()));
        lookup.addFieldMappingFactory(double.class, new JdbcBinaryValueMapper.Factory(JdbcConnector.TYPE_DOUBLE()));
        lookup.addFieldMappingFactory(char.class, new JdbcGeneralValueMapper.Factory("CHAR(2)"));

        lookup.addFieldMappingFactory(Money.class, new JdbcMoneyValueMapper.Factory("FLOAT", "VARCHAR(3)"));
        lookup.addFieldMappingFactory(Percentage.class, new JdbcGeneralValueMapper.Factory("FLOAT"));
        lookup
            .addFieldMappingFactory(Password.class, new JdbcGeneralValueMapper.Factory(JdbcConnector.TYPE_PASSWORD()));
        lookup.addFieldMappingFactory(Color.class, new JdbcColorValueMapper.Factory(JdbcConnector.TYPE_LONG()));
        lookup.addFieldMappingFactory(String.class, new JdbcGeneralValueMapper.Factory(JdbcConnector.TYPE_STRING()));

        lookup.addFieldMappingFactory(Date.class, new JdbcDateMapper.Factory());
        lookup.addFieldMappingFactory(Time.class, new JdbcTimeMapper.Factory());
        lookup.addFieldMappingFactory(DateTime.class, new JdbcDateTimeMapper.Factory());
        lookup.addFieldMappingFactory(TimeStamp.class, new JdbcTimestampMapper.Factory());

        lookup.addFieldMappingFactory(java.sql.Date.class, new JdbcDateMapper.Factory());
        lookup.addFieldMappingFactory(java.sql.Time.class, new JdbcTimeMapper.Factory());
        lookup.addFieldMappingFactory(java.util.Date.class, new JdbcDateTimeMapper.Factory());
        lookup.addFieldMappingFactory(java.sql.Timestamp.class, new JdbcTimestampMapper.Factory());

        lookup.setReferenceFieldMappingFactory(new JdbcObjectReferenceFieldMapping.Factory());

        lookup.setObjectReferenceMappingfactory(new JdbcObjectReferenceMappingFactory());

    }

}
