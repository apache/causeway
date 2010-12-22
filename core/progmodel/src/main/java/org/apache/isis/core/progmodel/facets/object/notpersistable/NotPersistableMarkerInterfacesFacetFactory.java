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


package org.apache.isis.core.progmodel.facets.object.notpersistable;

import java.lang.reflect.Method;

import org.apache.isis.applib.marker.NonPersistable;
import org.apache.isis.applib.marker.ProgramPersistable;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.object.notpersistable.InitiatedBy;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.metamodel.feature.FeatureType;


public class NotPersistableMarkerInterfacesFacetFactory extends FacetFactoryAbstract {

    public NotPersistableMarkerInterfacesFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    public boolean recognizes(final Method method) {
        return false;
    }

    @Override
    public boolean process(final Class<?> type, final MethodRemover methodRemover, final FacetHolder holder) {
        InitiatedBy initiatedBy = null;
        if (ProgramPersistable.class.isAssignableFrom(type)) {
            initiatedBy = InitiatedBy.USER;
        } else if (NonPersistable.class.isAssignableFrom(type)) {
            initiatedBy = InitiatedBy.USER_OR_PROGRAM;
        }
        return FacetUtil.addFacet(create(initiatedBy, holder));
    }

    private NotPersistableFacet create(final InitiatedBy initiatedBy, final FacetHolder holder) {
        return initiatedBy != null ? new NotPersistableFacetMarkerInterface(initiatedBy, holder) : null;
    }

}
