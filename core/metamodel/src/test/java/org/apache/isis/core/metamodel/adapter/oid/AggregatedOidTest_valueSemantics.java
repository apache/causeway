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

import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.unittestsupport.value.ValueTypeContractTestAbstract;

public class AggregatedOidTest_valueSemantics extends ValueTypeContractTestAbstract<AggregatedOid> {

    private final RootOidDefault parent = new RootOidDefault(ObjectSpecId.of("CUS"), "123", State.PERSISTENT);
    private final RootOidDefault otherParent = new RootOidDefault(ObjectSpecId.of("CUS"), "124", State.PERSISTENT);
    private final AggregatedOid yetAnotherParent = new AggregatedOid(ObjectSpecId.of("NME"), parent, "789");
    
    @Override
    protected List<AggregatedOid> getObjectsWithSameValue() {
        return Arrays.asList(
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "456") 
                );
    }

    @Override
    protected List<AggregatedOid> getObjectsWithDifferentValue() {
        return Arrays.asList(
                new AggregatedOid(ObjectSpecId.of("NME"), otherParent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NMX"), parent, "456"), 
                new AggregatedOid(ObjectSpecId.of("NME"), parent, "457"), 
                new AggregatedOid(ObjectSpecId.of("NME"), yetAnotherParent, "456"));
    }

}
