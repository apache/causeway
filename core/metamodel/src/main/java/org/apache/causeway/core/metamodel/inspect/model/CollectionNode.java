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
package org.apache.causeway.core.metamodel.inspect.model;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class CollectionNode extends MemberNode {

    @Programmatic
    private final OneToManyAssociation collection;

    @Override
    public String title() {
        return collection.getId();
    }

    @Override
    public void putDetails(Details details) {
        details.put("Id", collection.getId());
        details.put("Friendly Name", collection.getCanonicalFriendlyName());
        details.put("Mixed In", "" + isMixedIn());
        details.put("Element Type", collection.getElementType().logicalTypeName());
    }
    
    @Override
    protected ObjectMember member() {
        return collection;
    }

}

