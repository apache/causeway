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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.easymock.MockControl;
import org.hibernate.Hibernate;
import org.apache.isis.applib.value.Color;


public class ColorTypeTest extends TypesTestCase {

    public void testNullSafeGetNotNull() throws Exception {
        final int value = 4;

        final MockControl<ResultSet> control = MockControl.createControl(ResultSet.class);
        final ResultSet rs = (ResultSet) control.getMock();
        control.expectAndReturn(rs.getInt(names[0]), value);
        control.expectAndReturn(rs.wasNull(), false);
        control.replay(); // finished recording

        final ColorType type = new ColorType();
        final Color returned = (Color) type.nullSafeGet(rs, names, null);
        assertEquals("color", value, returned.intValue());

        control.verify();
    }

    public void testNullSafeGetIsNull() throws Exception {
        final MockControl<ResultSet> control = MockControl.createControl(ResultSet.class);
        final ResultSet rs = (ResultSet) control.getMock();
        control.expectAndReturn(rs.getInt(names[0]), 0);
        control.expectAndReturn(rs.wasNull(), true);
        control.replay(); // finished recording

        final ColorType type = new ColorType();
        assertNull(type.nullSafeGet(rs, names, null));

        control.verify();
    }

    public void testNullSafeSetNotNull() throws Exception {
        final int value = 5;
        final Color color = new Color(value);

        final MockControl<PreparedStatement> control = MockControl.createControl(PreparedStatement.class);
        final PreparedStatement ps = (PreparedStatement) control.getMock();
        ps.setInt(1, value);
        control.replay(); // finished recording

        final ColorType type = new ColorType();
        type.nullSafeSet(ps, color, 1);

        control.verify();
    }

    public void testNullSafeSetIsNull() throws Exception {
        final MockControl<PreparedStatement> control = MockControl.createControl(PreparedStatement.class);
        final PreparedStatement ps = (PreparedStatement) control.getMock();
        ps.setNull(1, Hibernate.INTEGER.sqlType());
        control.replay(); // finished recording

        final ColorType type = new ColorType();
        type.nullSafeSet(ps, null, 1);

        control.verify();
    }

    public void testBasics() {
        final ColorType type = new ColorType();
        super.basicTest(type);
        assertEquals("returned class", org.apache.isis.applib.value.Color.class, type.returnedClass());
    }

}
