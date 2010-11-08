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


package org.apache.isis.metamodel.facets.object.immutable;

import org.apache.isis.applib.marker.AlwaysImmutable;
import org.apache.isis.applib.marker.ImmutableOncePersisted;
import org.apache.isis.applib.marker.ImmutableUntilPersisted;
import org.apache.isis.applib.marker.NeverImmutable;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.When;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;


public class ImmutableMarkerInterfacesFacetFactory extends FacetFactoryAbstract {

    public ImmutableMarkerInterfacesFacetFactory() {
        super(ObjectFeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        When when = null;
        if (AlwaysImmutable.class.isAssignableFrom(cls)) {
            when = When.ALWAYS;
        } else if (ImmutableOncePersisted.class.isAssignableFrom(cls)) {
            when = When.ONCE_PERSISTED;
        } else if (ImmutableUntilPersisted.class.isAssignableFrom(cls)) {
            when = When.UNTIL_PERSISTED;
        } else if (NeverImmutable.class.isAssignableFrom(cls)) {
            when = When.NEVER;
        }
        return FacetUtil.addFacet(create(when, holder));
    }

    private ImmutableFacet create(final When when, final FacetHolder holder) {
        return when == null ? null : new ImmutableFacetMarkerInterface(when, holder);
    }

}
