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

package org.apache.isis.core.metamodel.specloader.classsubstitutor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;

import lombok.val;

public class ClassSubstitutorTest_getClass {

    private ClassSubstitutor classSubstitutor;
    private ClassSubstitutorRegistry classSubstitutorReg;

    @Before
    public void setUp() throws Exception {
        classSubstitutor = new ClassSubstitutorDefault();
        classSubstitutorReg = new ClassSubstitutorRegistry(_Lists.of(classSubstitutor));
    }

    public static class SomeDomainObject {

        public static enum SomeEnum {
            Foo {
                @Override
                public void x() {
                }
            };
            public abstract void x();
        }
    }

    @Test
    public void regularDomainObject() throws Exception {
        val input = SomeDomainObject.class;
        val replacement = classSubstitutorReg.getSubstitution(input).apply(input);
        assertEquals(input, replacement);
    }

    @Test
    public void someEnum() throws Exception {
        val input = SomeDomainObject.SomeEnum.class;
        val replacement = classSubstitutorReg.getSubstitution(input).apply(input);
        assertEquals(input, replacement);
    }

    @Test
    public void someAnonymousSubtypeOfEnum() throws Exception {
        val input = SomeDomainObject.SomeEnum.Foo.getClass();
        val replacement = classSubstitutorReg.getSubstitution(input).apply(input);
        assertEquals(SomeDomainObject.SomeEnum.class, replacement);
    }

}
