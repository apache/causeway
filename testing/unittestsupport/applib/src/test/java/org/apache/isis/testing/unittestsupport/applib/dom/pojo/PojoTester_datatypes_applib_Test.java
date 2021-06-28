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
package org.apache.isis.testing.unittestsupport.applib.dom.pojo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ApplibBlobHolder;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ApplibClobHolder;

import lombok.val;

import org.opentest4j.AssertionFailedError;

public class PojoTester_datatypes_applib_Test {

    @Test
    public void exercise_blob() {

        // given
        val holder = new ApplibBlobHolder();
        Assertions.assertThat(holder).extracting(ApplibBlobHolder::getSomeBlob).isNull();

        // when
        PojoTester.create()
                .usingData(DataForApplib.blobs())
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(ApplibBlobHolder::getSomeBlob).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_blob_broken() {

        // given
        val holder = new ApplibBlobHolder().butBroken();

        // when
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                        .usingData(DataForApplib.blobs())
                        .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_clob() {

        // given
        val holder = new ApplibClobHolder();
        Assertions.assertThat(holder).extracting(ApplibClobHolder::getSomeClob).isNull();

        // when
        PojoTester.create()
                .usingData(DataForApplib.clobs())
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(ApplibClobHolder::getSomeClob).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_clob_broken() {

        // given
        val holder = new ApplibClobHolder().butBroken();

        // when
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                        .usingData(DataForApplib.clobs())
                        .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }


}
