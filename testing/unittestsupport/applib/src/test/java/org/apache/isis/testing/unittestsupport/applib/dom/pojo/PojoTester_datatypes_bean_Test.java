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
import org.opentest4j.AssertionFailedError;

import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ExampleBean;
import org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders.ExampleBeanHolder;

import lombok.val;

public class PojoTester_datatypes_bean_Test {

    @Test
    public void exercise_bean() {

        // given
        val holder = new ExampleBeanHolder();
        Assertions.assertThat(holder).extracting(ExampleBeanHolder::getExampleBean).isNull();

        // when
        PojoTester.create()
                .usingData(ExampleBean.class, ExampleBean.class)
                .exercise(holder);

        // then
        Assertions.assertThat(holder).extracting(ExampleBeanHolder::getExampleBean).isNotNull();
        Assertions.assertThat(holder.getCounter()).isGreaterThan(0);
    }

    @Test
    public void exercise_bean_broken() {

        // given
        val holder = new ExampleBeanHolder().butBroken();

        // when
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(holder);
        }).isInstanceOf(AssertionFailedError.class);
    }


}
