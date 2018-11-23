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
package org.apache.isis.applib.services.exceprecog.jdo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.apache.isis.commons.internal.collections._Maps;
import org.junit.Before;
import org.junit.Test;

public class ExceptionRecognizerCompositeForJdoObjectStoreTest {

    private boolean[] called;
    private ExceptionRecognizerCompositeForJdoObjectStore recog;

    @Before
    public void setUp() throws Exception {
        
        called = new boolean[1];
        recog = new ExceptionRecognizerCompositeForJdoObjectStore() {
            @Override protected void addChildren() {
                called[0] = true;
            }
        };
    }

    @Test
    public void whenDisabledFlagNotSet() throws Exception {
        // when
        recog.init();

        // then
        assertThat(called[0], is(true));
    }

  //FIXME[2039]    
//    @Test
//    public void whenDisabledFlagSetToTrue() throws Exception {
//        // when
//        recog.init(_Maps.unmodifiable(ExceptionRecognizerCompositeForJdoObjectStore.KEY_DISABLE, "true"));
//
//        // then
//        assertThat(called[0], is(false));
//    }
//
//    @Test
//    public void whenDisabledFlagSetToFalse() throws Exception {
//        // when
//    	recog.init(_Maps.unmodifiable(ExceptionRecognizerCompositeForJdoObjectStore.KEY_DISABLE, "false"));
//
//        // then
//        assertThat(called[0], is(true));
//    }

}