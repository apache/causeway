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
package org.apache.isis.applib.util.schema;

import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.applib.services.schema.SchemaValueMarshaller;
import org.apache.isis.schema.common.v2.ValueDto;

public class CommonDtoUtils_setValueOn_Test {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Mock
    private SchemaValueMarshaller mockDtoContext;

    ValueDto valueDto;
    @Before
    public void setUp() throws Exception {
        valueDto = new ValueDto();
    }

    @Test
    public void not_implemented() {
        fail("setValueOn not implemented");
    }

//    @Test
//    public void when_blob_is_null() {
//        CommonDtoUtils.setValueOn(valueDto, ValueTypeAndSemantics.of(ValueType.BLOB, null), null, mockDtoContext);
//        final BlobDto blobDto = valueDto.getBlob();
//        Assert.assertThat(blobDto, is(nullValue()));
//    }
//
//    @Test
//    public void when_blob_is_not_null() {
//        final Blob val = new Blob("image.png", "image/png", new byte[]{1,2,3,4,5});
//        CommonDtoUtils.setValueOn(valueDto, ValueTypeAndSemantics.of(ValueType.BLOB, null), val, mockDtoContext);
//        final BlobDto blobDto = valueDto.getBlob();
//        Assert.assertThat(blobDto, is(notNullValue()));
//        Assert.assertThat(blobDto.getBytes(), is(val.getBytes()));
//        Assert.assertThat(blobDto.getName(), is(val.getName()));
//        Assert.assertThat(blobDto.getMimeType(), is(val.getMimeType().toString()));
//    }
//
//    @Test
//    public void when_clob_is_null() {
//        CommonDtoUtils.setValueOn(valueDto, ValueTypeAndSemantics.of(ValueType.CLOB, null), null, mockDtoContext);
//        final ClobDto clobDto = valueDto.getClob();
//        Assert.assertThat(clobDto, is(nullValue()));
//    }
//
//    @Test
//    public void when_clob_is_not_null() {
//        final Clob val = new Clob("image.png", "image/png", new char[]{1,2,3,4,5});
//        CommonDtoUtils.setValueOn(valueDto, ValueTypeAndSemantics.of(ValueType.CLOB, null), val, mockDtoContext);
//        final ClobDto clobDto = valueDto.getClob();
//        Assert.assertThat(clobDto, is(notNullValue()));
//        Assert.assertThat(clobDto.getChars(), is(val.getChars()));
//        Assert.assertThat(clobDto.getName(), is(val.getName()));
//        Assert.assertThat(clobDto.getMimeType(), is(val.getMimeType().toString()));
//    }
}