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

import org.junit.Assert;
import org.junit.Test;

public class BigDecimalConverterTest {

    final BigDecimal bd_123_45_scale2 = new BigDecimal("123.45").setScale(2);
    final BigDecimal bd_123_45_scale4 = new BigDecimal("123.45").setScale(4);
    final BigDecimal bd_123_4500_scale2 = new BigDecimal("123.4500").setScale(2);
    final BigDecimal bd_123_4500_scale4 = new BigDecimal("123.4500").setScale(4);
    
    @Test
    public void test_scale2() {
        final BigDecimal actual = new BigDecimalConverter(2).convertToObject("123.45", null);
        Assert.assertEquals(bd_123_4500_scale2, actual);
        Assert.assertEquals(bd_123_45_scale2, actual);
        
        Assert.assertNotEquals(bd_123_4500_scale4, actual);
        Assert.assertNotEquals(bd_123_45_scale4, actual);
    }
    
    @Test
    public void test_scale4() {
        final BigDecimal actual = new BigDecimalConverter(4).convertToObject("123.45", null);
        Assert.assertNotEquals(bd_123_4500_scale2, actual);
        Assert.assertNotEquals(bd_123_45_scale2, actual);
        
        Assert.assertEquals(bd_123_4500_scale4, actual);
        Assert.assertEquals(bd_123_45_scale4, actual);
    }
    
    @Test
    public void test_scaleNull() {
        final BigDecimal actual = new BigDecimalConverter(null).convertToObject("123.45", null);
        Assert.assertEquals(bd_123_4500_scale2, actual);
        Assert.assertEquals(bd_123_45_scale2, actual);
        
        Assert.assertNotEquals(bd_123_4500_scale4, actual);
        Assert.assertNotEquals(bd_123_45_scale4, actual);
    }
    

}
