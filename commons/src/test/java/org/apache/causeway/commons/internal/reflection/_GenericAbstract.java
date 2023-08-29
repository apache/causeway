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

/**
 * Has sample actions 1..4.
 */
abstract class _GenericAbstract<T> {

    static _Expectations expectations() {
        return _Expectations.builder()
                .methodNameOrdinals("1,3")
                .methodCount(2)
                .syntheticCount(0)
                .bridgeCount(0)
                .build();
    }

    abstract void sampleAction1();
    abstract T sampleAction2(T x); // generic bound -> excluded

    void sampleAction3() { }
    T sampleAction4(final T x) { return x; } // generic bound -> excluded

}
