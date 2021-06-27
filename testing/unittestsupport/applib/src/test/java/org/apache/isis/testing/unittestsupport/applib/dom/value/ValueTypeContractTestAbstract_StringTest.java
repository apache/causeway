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
package org.apache.isis.testing.unittestsupport.applib.dom.value;

import java.util.Arrays;
import java.util.List;

public class ValueTypeContractTestAbstract_StringTest extends ValueTypeContractTestAbstract<String> {

    @Override
    protected List<String> getObjectsWithSameValue() {
        return Arrays.asList(new String("1"), new String("1"));
    }

    @Override
    protected List<String> getObjectsWithDifferentValue() {
        return Arrays.asList(new String("1 "), new String(" 1"), new String("2"));
    }

}
