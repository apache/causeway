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
package org.apache.causeway.testdomain.model.good;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableCollection;

import lombok.Getter;
import lombok.Setter;

/**
 * @see <a href="https://issues.apache.org/jira/browse/CAUSEWAY-2499">CAUSEWAY-2499</a>
 */
@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperElementTypeVm
extends ProperElementTypeVmAbstract {

    @Collection
    @Getter @Setter private List<ElementTypeInterface> interfaceColl;

    @Collection
    @Getter @Setter private List<ElementTypeAbstract> abstractColl;

    @Collection
    @Getter @Setter private List<ElementTypeConcrete> concreteColl;

    @Collection
    @Getter @Setter private List<? extends ElementTypeInterface> interfaceColl2;

    @Collection
    @Getter @Setter private List<? extends ElementTypeAbstract> abstractColl2;

    @Collection
    @Getter @Setter private List<? extends ElementTypeConcrete> concreteColl2;

    // specialization over Set<ElementTypeInterface> in super
    @Override
    public SortedSet<ElementTypeInterface> getSetOfInterfaceType() {
        return Collections.emptySortedSet();
    }

    // specialization over Set<? extends ElementTypeConcrete> in super
    @Override
    public SortedSet<? extends ElementTypeConcrete> getSetOfConcreteType() {
        return Collections.emptySortedSet();
    }

    // specialization over ImmutableCollection<ElementTypeInterface> in super
    @Override
    public Can<ElementTypeInterface> getImmutableOfInterfaceType() {
        return Can.empty();
    }

    // specialization over ImmutableCollection<? extends ElementTypeConcrete> in super
    @Override
    public Can<? extends ElementTypeConcrete> getImmutableOfConcreteType() {
        return Can.empty();
    }

    @Override
    public void act(final ImmutableCollection<ElementTypeInterface> coll) {
    }


}
