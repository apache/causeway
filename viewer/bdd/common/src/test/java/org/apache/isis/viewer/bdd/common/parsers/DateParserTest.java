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
package org.apache.isis.viewer.bdd.common.parsers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

public class DateParserTest {

    /**
     * Tracking down problem in ISIS-18...
     * <p>
     * This fails because 'MMM' is gonna be different in different languages.
     */
    @Ignore
    @Test
    public void parsesUnder_enUK_butNotUnder_deDE() throws Exception {
        final DateParser dateParser = new DateParser();
        dateParser.setDateFormat("dd-MMM-yyyy");
        dateParser.setTimeFormat("hh:mm");
        final Date parse = dateParser.parse("02-May-2010 09:20");
        assertThat(parse, is(not(nullValue())));
    }

}
