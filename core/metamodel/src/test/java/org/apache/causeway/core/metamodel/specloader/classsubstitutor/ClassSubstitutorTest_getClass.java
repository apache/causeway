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
package org.apache.causeway.core.metamodel.specloader.classsubstitutor;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;

class ClassSubstitutorTest_getClass {

    private ClassSubstitutor classSubstitutor;
    private ClassSubstitutorRegistry classSubstitutorReg;

    @BeforeEach
    public void setUp() throws Exception {
        classSubstitutor = new ClassSubstitutorDefault();
        classSubstitutorReg = new ClassSubstitutorRegistry(List.of(classSubstitutor));
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
        var input = SomeDomainObject.class;
        var replacement = classSubstitutorReg.getSubstitution(input).apply(input);
        assertEquals(input, replacement);
    }

    @Test
    public void someEnum() throws Exception {
        var input = SomeDomainObject.SomeEnum.class;
        var replacement = classSubstitutorReg.getSubstitution(input).apply(input);
        assertEquals(input, replacement);
    }

    @Test
    public void someAnonymousSubtypeOfEnum() throws Exception {
        var input = SomeDomainObject.SomeEnum.Foo.getClass();
        var replacement = classSubstitutorReg.getSubstitution(input).apply(input);
        assertEquals(SomeDomainObject.SomeEnum.class, replacement);
    }

}
