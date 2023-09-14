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
package org.apache.causeway.commons.internal.reflection;

import java.util.List;

import lombok.RequiredArgsConstructor;

class _Mixins {

    static class Task1 {

        static _Expectations expectations() {
            return _Expectations.builder()
                    .methodNameOrdinals("1:string,2")
                    .methodCount(2)
                    .syntheticCount(0)
                    .bridgeCount(0)
                    .build();
        }

        // inner, non static mixin
        class Mixin
        extends MixinAbstract {
            public List<String> sampleAction2() { return null; }
        }

        // inner, non static, abstract mixin base
        abstract class MixinAbstract {
            public Task1 sampleAction1(final String outcome) {
                return Task1.this;
            }
        }
    }

    static class Task2 {

        static _Expectations expectations() {
            return _Expectations.builder()
                    .methodNameOrdinals("1:string,2")
                    .methodCount(2)
                    .syntheticCount(0)
                    .bridgeCount(0)
                    .build();
        }

        // inner, static mixin
        static class Mixin
        extends MixinAbstract {
            public Mixin(final Task2 task) { super(task); }
            public List<String> sampleAction2() { return null; }
        }

        // inner, static, abstract mixin base
        @RequiredArgsConstructor
        abstract static class MixinAbstract {
            private final Task2 task;
            public Task2 sampleAction1(final String outcome) {
                return task;
            }
        }

    }

}
