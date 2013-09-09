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

package org.apache.isis.objectstore.sql.jdbc.installer;

import org.apache.isis.applib.value.Color;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.applib.value.Image;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.Percentage;
import org.apache.isis.applib.value.Time;
import org.apache.isis.applib.value.TimeStamp;
import org.apache.isis.objectstore.sql.Defaults;
import org.apache.isis.objectstore.sql.FieldMappingFactoryInstaller;
import org.apache.isis.objectstore.sql.FieldMappingLookup;
import org.apache.isis.objectstore.sql.jdbc.JdbcBinaryValueMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcColorValueMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcDateMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcDateTimeMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcGeneralValueMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcImageValueMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcMoneyValueMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcObjectReferenceFieldMapping;
import org.apache.isis.objectstore.sql.jdbc.JdbcObjectReferenceMappingFactory;
import org.apache.isis.objectstore.sql.jdbc.JdbcPasswordValueMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcTimeMapper;
import org.apache.isis.objectstore.sql.jdbc.JdbcTimestampMapper;

public class JdbcFieldMappingFactoryInstaller implements FieldMappingFactoryInstaller {

    @Override
    public void load(final FieldMappingLookup lookup) {

        lookup.addFieldMappingFactory(Image.class,
            new JdbcImageValueMapper.Factory(Defaults.TYPE_STRING(), Defaults.TYPE_BLOB()));

        lookup.addFieldMappingFactory(Boolean.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_BOOLEAN()));
        lookup.addFieldMappingFactory(Short.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_SHORT()));
        lookup.addFieldMappingFactory(Integer.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_INT()));
        lookup.addFieldMappingFactory(Long.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_LONG()));
        lookup.addFieldMappingFactory(Float.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_FLOAT()));
        lookup.addFieldMappingFactory(Double.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_DOUBLE()));
        lookup.addFieldMappingFactory(char.class, new JdbcGeneralValueMapper.Factory("CHAR(2)"));

        lookup.addFieldMappingFactory(Money.class, new JdbcMoneyValueMapper.Factory("FLOAT", "VARCHAR(3)"));
        lookup.addFieldMappingFactory(Percentage.class, new JdbcGeneralValueMapper.Factory("FLOAT"));
        lookup.addFieldMappingFactory(Password.class, new JdbcPasswordValueMapper.Factory(Defaults.TYPE_PASSWORD(),
            Defaults.PASSWORD_SEED(), Defaults.PASSWORD_ENC_LENGTH()));
        lookup.addFieldMappingFactory(Color.class, new JdbcColorValueMapper.Factory(Defaults.TYPE_LONG()));
        lookup.addFieldMappingFactory(String.class, new JdbcGeneralValueMapper.Factory(Defaults.TYPE_STRING()));

        lookup.addFieldMappingFactory(Date.class, new JdbcDateMapper.Factory());
        lookup.addFieldMappingFactory(Time.class, new JdbcTimeMapper.Factory());
        lookup.addFieldMappingFactory(DateTime.class, new JdbcDateTimeMapper.Factory());
        lookup.addFieldMappingFactory(TimeStamp.class, new JdbcTimestampMapper.Factory());

        lookup.addFieldMappingFactory(java.sql.Date.class, new JdbcDateMapper.Factory());
        lookup.addFieldMappingFactory(java.sql.Time.class, new JdbcTimeMapper.Factory());
        lookup.addFieldMappingFactory(java.util.Date.class, new JdbcDateTimeMapper.Factory());
        lookup.addFieldMappingFactory(java.sql.Timestamp.class, new JdbcTimestampMapper.Factory());

        lookup.addFieldMappingFactory(boolean.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_BOOLEAN()));
        lookup.addFieldMappingFactory(short.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_SHORT()));
        lookup.addFieldMappingFactory(int.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_INT()));
        lookup.addFieldMappingFactory(long.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_LONG()));
        lookup.addFieldMappingFactory(float.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_FLOAT()));
        lookup.addFieldMappingFactory(double.class, new JdbcBinaryValueMapper.Factory(Defaults.TYPE_DOUBLE()));

        lookup.setReferenceFieldMappingFactory(new JdbcObjectReferenceFieldMapping.Factory());

        lookup.setObjectReferenceMappingfactory(new JdbcObjectReferenceMappingFactory());

    }

}
