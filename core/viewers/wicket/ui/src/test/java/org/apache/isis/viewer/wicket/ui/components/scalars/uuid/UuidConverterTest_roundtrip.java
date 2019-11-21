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
package org.apache.isis.viewer.wicket.ui.components.scalars.uuid;

import java.util.Locale;
import java.util.UUID;

import org.apache.wicket.util.convert.ConversionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UuidConverterTest_roundtrip {

    final UUID valid = UUID.randomUUID();

    private UuidConverter converter;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        converter = newConverter();
    }

    @Test
    public void happy_case() {

        Assert.assertEquals(
                valid, converter.convertToObject(valid.toString(), Locale.ENGLISH));
        Assert.assertEquals(
                valid.toString(), converter.convertToString(valid, Locale.ENGLISH));
    }

    @Test
    public void when_null() {
        Assert.assertNull(converter.convertToObject(null, Locale.ENGLISH));
        Assert.assertNull(converter.convertToObject("", Locale.ENGLISH));
        Assert.assertNull(converter.convertToString(null, Locale.ENGLISH));
    }

    @Test
    public void invalid() {
        exception.expect(ConversionException.class);
        exception.expectMessage("Failed to convert 'junk' to a UUID");
        Assert.assertNull(converter.convertToObject("junk", Locale.ENGLISH));
    }

    private UuidConverter newConverter() {
        return new
                UuidConverter();
    }

}
