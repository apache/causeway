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
package org.apache.isis.core.metamodel.facets.value;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaSqlDateValueSemantics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import lombok.val;

public class JavaSqlDateValueSemanticsProviderTest extends ValueSemanticsProviderAbstractTestCase {

    private JavaSqlDateValueSemantics value;
    private Date date;

    @Before
    public void setUpObjects() throws Exception {

        date = new Date(0);

        ValueSemanticsAbstract<LocalDate> delegate =
                new LocalDateValueSemantics();

        setSemantics(value = new JavaSqlDateValueSemantics() {

            @Override
            public ValueSemanticsAbstract<LocalDate> getDelegate() {
                return delegate;
            }

        });
    }

    @Test
    public void testInvalidParse() throws Exception {
        try {
            value.parseTextRepresentation(null, "date");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals(DateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(new Date(0)), value.simpleTextPresentation(null, date));
    }

    @Test
    public void testParse() throws Exception {
        val parsedDate = value.parseTextRepresentation(null, "1980-01-01");
        assertEquals("1980-01-01", parsedDate.toString());
    }

}
