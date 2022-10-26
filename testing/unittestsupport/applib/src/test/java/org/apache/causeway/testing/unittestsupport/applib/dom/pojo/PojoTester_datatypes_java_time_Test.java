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
package org.apache.causeway.testing.unittestsupport.applib.dom.pojo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import org.apache.causeway.testing.unittestsupport.applib.dom.pojo.holders.JavaLocalDateHolder;
import org.apache.causeway.testing.unittestsupport.applib.dom.pojo.holders.JavaLocalDateTimeHolder;
import org.apache.causeway.testing.unittestsupport.applib.dom.pojo.holders.JavaLocalTimeHolder;
import org.apache.causeway.testing.unittestsupport.applib.dom.pojo.holders.JavaOffsetDateTimeHolder;

import lombok.val;

public class PojoTester_datatypes_java_time_Test {

    @Test
    public void exercise_offset_date_time() {

        // given
        val holder = new JavaOffsetDateTimeHolder();
        Assertions.assertThat(holder).extracting(JavaOffsetDateTimeHolder::getOffsetDateTime).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(JavaOffsetDateTimeHolder::getOffsetDateTime).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_offset_local_date_time_broken() {

        // given
        val holder = new JavaOffsetDateTimeHolder().butBroken();

        // when
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                        .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_local_date() {

        // given
        val holder = new JavaLocalDateHolder();
        Assertions.assertThat(holder).extracting(JavaLocalDateHolder::getSomeLocalDate).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(JavaLocalDateHolder::getSomeLocalDate).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_local_date_broken() {

        // given
        val holder = new JavaLocalDateHolder().butBroken();

        // when
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                        .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_local_date_time() {

        // given
        val holder = new JavaLocalDateTimeHolder();
        Assertions.assertThat(holder).extracting(JavaLocalDateTimeHolder::getSomeLocalDateTime).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(JavaLocalDateTimeHolder::getSomeLocalDateTime).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_local_date_time_broken() {

        // given
        val holder = new JavaLocalDateTimeHolder().butBroken();

        // when
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                        .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }

    @Test
    public void exercise_local_time() {

        // given
        val holder = new JavaLocalTimeHolder();
        Assertions.assertThat(holder).extracting(JavaLocalTimeHolder::getLocalTime).isNull();

        // when
        PojoTester.create()
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(JavaLocalTimeHolder::getLocalTime).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_local_time_broken() {

        // given
        val holder = new JavaLocalTimeHolder().butBroken();

        // when
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                        .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }


}
