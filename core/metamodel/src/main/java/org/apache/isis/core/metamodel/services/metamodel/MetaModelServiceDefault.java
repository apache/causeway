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
package org.apache.isis.core.metamodel.services.metamodel;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class MetaModelServiceDefault implements MetaModelService, SpecificationLoaderAware {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(MetaModelServiceDefault.class);



    //region > fromObjectType, toObjectType
    @Programmatic
    public Class<?> fromObjectType(final String objectType) {
        if(objectType == null) {
            return null;
        }
        final ObjectSpecId objectSpecId = new ObjectSpecId(objectType);
        final ObjectSpecification objectSpecification = specificationLookup.lookupBySpecId(objectSpecId);
        return objectSpecification != null? objectSpecification.getCorrespondingClass(): null;
    }

    @Override
    public String toObjectType(final Class<?> domainType) {
        if(domainType == null) {
            return null;
        }
        final ObjectSpecification objectSpecification = specificationLookup.loadSpecification(domainType);
        final ObjectSpecIdFacet objectSpecIdFacet = objectSpecification.getFacet(ObjectSpecIdFacet.class);
        final ObjectSpecId objectSpecId = objectSpecIdFacet.value();
        return objectSpecId.asString();
    }
    //endregion

    //region > injected dependencies
    private SpecificationLoader specificationLookup;

    @Override
    public void setSpecificationLookup(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }
    //endregion

}
