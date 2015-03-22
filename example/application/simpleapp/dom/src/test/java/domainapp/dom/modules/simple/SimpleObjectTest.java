/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package domainapp.dom.modules.simple;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class SimpleObjectTest {

    SimpleObject simpleObject;

    @Before
    public void setUp() throws Exception {
        simpleObject = new SimpleObject();
    }

    public static class Name extends SimpleObjectTest {

        @Test
        public void happyCase() throws Exception {
            // given
            String name = "Foobar";
            assertThat(simpleObject.getName(), is(nullValue()));

            // when
            simpleObject.setName(name);

            // then
            assertThat(simpleObject.getName(), is(name));
        }
    }

}
