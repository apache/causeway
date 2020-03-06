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
package org.apache.isis.subdomains.base.applib.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.subdomains.base.applib.valuetypes.LocalDateInterval;

public class TitleBuilderTest {

    public class TestObject {
        public String title(){
            return "Parent [PAR]";
        }
    }

    @Test
    public void testToString() throws Exception {
        assertThat(TitleBuilder.start().withName("Name").withReference("REF").toString())
                .isEqualTo("Name [REF]");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName("Name").withReference("REF").toString())
                .isEqualTo("Parent [PAR] > Name [REF]");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName("REF").withReference("REF").toString())
                .isEqualTo("Parent [PAR] > REF");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName("Name1").withName("Name2").withReference("REF").toString())
                .isEqualTo("Parent [PAR] > Name1 Name2 [REF]");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName(LocalDateInterval.parseString("2014-01-01/2015-01-01")).toString())
                .isEqualTo("Parent [PAR] > 2014-01-01/2015-01-01");

    }
}