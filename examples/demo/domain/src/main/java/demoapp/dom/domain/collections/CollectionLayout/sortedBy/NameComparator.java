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
package demoapp.dom.domain.collections.CollectionLayout.sortedBy;

import demoapp.dom.domain.collections.CollectionLayout.sortedBy.child.CollectionLayoutSortedByChildVm;

import java.util.Comparator;

import org.apache.causeway.applib.util.ObjectContracts;

//tag::comparator[]
public class NameComparator implements Comparator<CollectionLayoutSortedByChildVm> {
    @Override
    public int compare(
            CollectionLayoutSortedByChildVm o1,
            CollectionLayoutSortedByChildVm o2
    ) {
        return ObjectContracts.contract(CollectionLayoutSortedByChildVm.class)  // <.>
                .thenUse("name", CollectionLayoutSortedByChildVm::getName)      // <.>
                .compare(o1, o2);
    }
}
//end::comparator[]
