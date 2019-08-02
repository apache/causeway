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
package org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BigDecimalConverterWithScaleTest_roundtrip {

    final BigDecimal bd_123_45_scale2 = new BigDecimal("123.45").setScale(2);
    final BigDecimal bd_123_4500_scale2 = new BigDecimal("123.4500").setScale(2);

    final BigDecimal bd_789123_45_scale2 = new BigDecimal("789123.45").setScale(2);

    final BigDecimal bd_123_45_scale4 = new BigDecimal("123.45").setScale(4);
    final BigDecimal bd_123_4500_scale4 = new BigDecimal("123.4500").setScale(4);

    private BigDecimalConverterWithScale converter;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        converter = newConverter(2);
    }

    @Test
    public void scale2_english() {

        // when
        final BigDecimal actual = converter.convertToObject("123.45", Locale.ENGLISH);
        Assert.assertEquals(bd_123_4500_scale2, actual);
        Assert.assertEquals(bd_123_45_scale2, actual);

        Assert.assertNotEquals(bd_123_4500_scale4, actual);
        Assert.assertNotEquals(bd_123_45_scale4, actual);

        // when
        String actualStr = converter.convertToString(actual, Locale.ENGLISH);
        Assert.assertEquals("123.45", actualStr);
    }

    @Test
    public void scale4_english() {
        converter = newConverter(4);

        final BigDecimal actual = converter.convertToObject("123.45", Locale.ENGLISH);
        Assert.assertNotEquals(bd_123_4500_scale2, actual);
        Assert.assertNotEquals(bd_123_45_scale2, actual);

        Assert.assertEquals(bd_123_4500_scale4, actual);
        Assert.assertEquals(bd_123_45_scale4, actual);

        // when
        String actualStr = converter.convertToString(actual, Locale.ENGLISH);
        Assert.assertEquals("123.4500", actualStr);
    }


    @Test
    public void scaleNull_english() {
        converter = newConverter(null);

        final BigDecimal actual = converter.convertToObject("123.45", Locale.ENGLISH);
        Assert.assertEquals(bd_123_4500_scale2, actual);
        Assert.assertEquals(bd_123_45_scale2, actual);

        final BigDecimal actual2 = converter.convertToObject("123.4500", Locale.ENGLISH);
        Assert.assertEquals(bd_123_4500_scale4, actual2);
        Assert.assertEquals(bd_123_45_scale4, actual2);
    }


    @Test
    public void scale2_italian() {

        final BigDecimal actual = converter.convertToObject("123,45", Locale.ITALIAN);
        Assert.assertEquals(bd_123_4500_scale2, actual);
        Assert.assertEquals(bd_123_45_scale2, actual);

        Assert.assertNotEquals(bd_123_4500_scale4, actual);
        Assert.assertNotEquals(bd_123_45_scale4, actual);
    }


    @Test
    public void scale2_english_withThousandSeparators() {

        exception.expect(ConversionException.class);
        exception.expectMessage("Thousands separator ',' is not allowed in input");

        converter.convertToObject("789,123.45", Locale.ENGLISH);
    }

    @Test
    public void scale2_english_withoutThousandSeparators() {

        // when
        final BigDecimal actual = converter.convertToObject("789123.45", Locale.ENGLISH);
        Assert.assertEquals(bd_789123_45_scale2, actual);

        // when
        String actualStr = converter.convertToString(actual, Locale.ENGLISH);
        Assert.assertEquals("789123.45", actualStr);
    }

    @Test
    public void scale2_english_tooLargeScale() {

        exception.expect(ConversionException.class);
        exception.expectMessage("No more than 2 digits can be entered after the decimal place");

        converter.convertToObject("123.454", Locale.ENGLISH);
    }

    private BigDecimalConverterWithScale newConverter(Integer scale) {
        return new BigDecimalConverterWithScale(scale);
    }

}
