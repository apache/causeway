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
import java.util.List;

import org.apache.isis.core.internaltestsupport.contract.ValueTypeContractTestAbstract;

public class RootOidDefaultTest_valueSemantics_whenPersistent 
extends ValueTypeContractTestAbstract<Oid> {

    @Override
    protected List<Oid> getObjectsWithSameValue() {
        return Arrays.asList(
                Oid.root(LogicalTypeTestFactory.cus(), "123"),
                Oid.root(LogicalTypeTestFactory.cus(), "123"),
                Oid.root(LogicalTypeTestFactory.cus(), "123"));
    }

    @Override
    protected List<Oid> getObjectsWithDifferentValue() {
        return Arrays.asList(
                //Oid.Factory.of(ObjectSpecId.of("CUS"), "123"),
                Oid.root(LogicalTypeTestFactory.cus(), "124"),
                Oid.root(LogicalTypeTestFactory.cux(), "123"));
    }

}
