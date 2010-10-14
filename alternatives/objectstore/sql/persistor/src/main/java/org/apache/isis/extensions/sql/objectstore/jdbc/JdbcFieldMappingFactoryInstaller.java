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


package org.apache.isis.extensions.sql.objectstore.jdbc;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.extensions.sql.objectstore.FieldMappingFactoryInstaller;
import org.apache.isis.extensions.sql.objectstore.FieldMappingLookup;


public class JdbcFieldMappingFactoryInstaller implements FieldMappingFactoryInstaller {

    public void load(FieldMappingLookup lookup) {
        lookup.addFieldMappingFactory(boolean.class, new JdbcGeneralValueMapper.Factory("CHAR(1)"));
        lookup.addFieldMappingFactory(short.class, new JdbcGeneralValueMapper.Factory("INT"));
        lookup.addFieldMappingFactory(int.class, new JdbcGeneralValueMapper.Factory("INT"));
        lookup.addFieldMappingFactory(long.class, new JdbcGeneralValueMapper.Factory("INT"));
        lookup.addFieldMappingFactory(float.class, new JdbcGeneralValueMapper.Factory("FLOAT"));
        lookup.addFieldMappingFactory(double.class, new JdbcGeneralValueMapper.Factory("FLOAT"));
        lookup.addFieldMappingFactory(char.class, new JdbcGeneralValueMapper.Factory("CHAR(2)"));

        lookup.addFieldMappingFactory(Money.class, new JdbcGeneralValueMapper.Factory("FLOAT"));
        lookup.addFieldMappingFactory(Percentage.class, new JdbcGeneralValueMapper.Factory("FLOAT"));
        lookup.addFieldMappingFactory(Password.class, new JdbcGeneralValueMapper.Factory("VARCHAR(12)"));
        lookup.addFieldMappingFactory(Color.class, new JdbcGeneralValueMapper.Factory("INT"));
        lookup.addFieldMappingFactory(String.class, new JdbcGeneralValueMapper.Factory("VARCHAR(65)"));

        lookup.addFieldMappingFactory(Date.class, new JdbcDateMapper.Factory());
        lookup.addFieldMappingFactory(Time.class, new JdbcTimeMapper.Factory());
        lookup.addFieldMappingFactory(DateTime.class, new JdbcDateTimeMapper.Factory());
        lookup.addFieldMappingFactory(TimeStamp.class, new JdbcTimestampMapper.Factory());

        lookup.addFieldMappingFactory(java.sql.Date.class, new JdbcDateMapper.Factory());
        lookup.addFieldMappingFactory(java.sql.Time.class, new JdbcTimeMapper.Factory());
        lookup.addFieldMappingFactory(java.util.Date.class, new JdbcDateTimeMapper.Factory());
        lookup.addFieldMappingFactory(java.sql.Timestamp.class, new JdbcTimestampMapper.Factory());

        lookup.setReferenceFieldMappingFactory(new JdbcObjectReferenceFieldMapping.Factory());
        
        lookup .setObjectReferenceMappingfactory(new JdbcObjectReferenceMappingFactory());

    }

}

