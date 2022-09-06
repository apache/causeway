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
package org.apache.isis.testdomain.model.good;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

import lombok.Getter;
import lombok.Setter;

/**
 * @see <a href="https://issues.apache.org/jira/browse/ISIS-2499">ISIS-2499</a>
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

    @Override
    public Iterable<ElementTypeInterface> getIterableOfInterfaceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<? extends ElementTypeConcrete> getIterableOfConcreteType() {
        // TODO Auto-generated method stub
        return null;
    }
  //FIXME add Can support
//    // specialization over Iterable<ElementTypeInterface> in super
//    @Override
//    public Can<ElementTypeInterface> getIterableOfInterfaceType() {
//        return Can.empty();
//    }
//
//    // specialization over Iterable<? extends ElementTypeConcrete> in super
//    @Override
//    public Can<? extends ElementTypeConcrete> getIterableOfConcreteType() {
//        return Can.empty();
//    }
//
//    @Override
//    void act(final ImmutableCollection<ElementTypeInterface> coll) {
//    }


}
