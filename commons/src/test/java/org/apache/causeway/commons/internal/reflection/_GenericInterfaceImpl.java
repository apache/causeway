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
 * Has sample actions 1..6.
 */
class _GenericInterfaceImpl
implements _GenericInterface<String> {

    static _Expectations expectations() {
        return _Expectations.builder()
                .methodNameOrdinals("1,2:string,3,4pr:string,5,6:string")
                .methodCount(6)
                .syntheticCount(0)
                .bridgeCount(0)
                .build();
    }

    @Override public void sampleAction1() {}
    // appears twice, unusable variant has isSynthetic and isBridge
    @Override public String sampleAction2(final String x) { return x; } // isSynthetic and isBridge

    // sample actions 3..4 inherited without overriding

    void sampleAction5() { }
    String sampleAction6(final String x) { return x; }

}
