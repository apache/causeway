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
package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.unittestsupport.value.ValueTypeContractTestAbstract;

public class VersionTest_valueSemantics extends ValueTypeContractTestAbstract<Version> {

    @Override
    protected List<Version> getObjectsWithSameValue() {
        return Arrays.asList(
                    Version.of(123L, null), 
                    Version.of(123L, "jimmy"), 
                    Version.of(123L, null, new Date().getTime())
                ); 
    }

    @Override
    protected List<Version> getObjectsWithDifferentValue() {
        return Arrays.asList(
                    Version.of(124L, null), 
                    Version.of(125L, null) 
                );
    }

}
