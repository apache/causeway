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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.datanucleus.enhancement.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.metamodel.DomainMember;
import org.apache.isis.applib.services.metamodel.MetaModelService3;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class MetaModelServiceDefault implements MetaModelService3 {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(MetaModelServiceDefault.class);


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

    @Override
    public void rebuild(final Class<?> domainType) {
        specificationLookup.invalidateCache(domainType);
        gridService.remove(domainType);
        final ObjectSpecification objectSpecification = specificationLookup.loadSpecification(domainType);
        // ensure the spec is fully rebuilt
        objectSpecification.getObjectActions(Contributed.INCLUDED);
        objectSpecification.getAssociations(Contributed.INCLUDED);
    }

    // //////////////////////////////////////



    @Programmatic
    public List<DomainMember> export() {

        final Collection<ObjectSpecification> specifications = specificationLookup.allSpecifications();

        final List<DomainMember> rows = Lists.newArrayList();
        for (final ObjectSpecification spec : specifications) {
            if (exclude(spec)) {
                continue;
            }
            final List<ObjectAssociation> properties = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES);
            for (final ObjectAssociation property : properties) {
                final OneToOneAssociation otoa = (OneToOneAssociation) property;
                if (exclude(otoa)) {
                    continue;
                }
                rows.add(new DomainMemberDefault(spec, otoa));
            }
            final List<ObjectAssociation> associations = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.COLLECTIONS);
            for (final ObjectAssociation collection : associations) {
                final OneToManyAssociation otma = (OneToManyAssociation) collection;
                if (exclude(otma)) {
                    continue;
                }
                rows.add(new DomainMemberDefault(spec, otma));
            }
            final List<ObjectAction> actions = spec.getObjectActions(Contributed.INCLUDED);
            for (final ObjectAction action : actions) {
                if (exclude(action)) {
                    continue;
                }
                rows.add(new DomainMemberDefault(spec, action));
            }
        }

        Collections.sort(rows);

        return rows;
    }



    protected boolean exclude(final OneToOneAssociation property) {
        return false;
    }

    protected boolean exclude(final OneToManyAssociation collection) {
        return false;
    }

    protected boolean exclude(final ObjectAction action) {
        return false;
    }

    protected boolean exclude(final ObjectSpecification spec) {
        return isBuiltIn(spec) || spec.isAbstract();
    }

    protected boolean isBuiltIn(final ObjectSpecification spec) {
        final String className = spec.getFullIdentifier();
        return className.startsWith("java") || className.startsWith("org.joda");
    }



    // //////////////////////////////////////

    @Override
    public Sort sortOf(final Class<?> domainType) {
        return sortOf(domainType, Mode.STRICT);
    }


    @Override
    public Sort sortOf(
            final Class<?> domainType, final Mode mode) {
        if(domainType == null) {
            return null;
        }
        final ObjectSpecification objectSpec = specificationLookup.loadSpecification(domainType);
        if(objectSpec.isService()) {
            return Sort.DOMAIN_SERVICE;
        }
        if(objectSpec.isViewModel()) {
            return Sort.VIEW_MODEL;
        }
        if(objectSpec.isValue()) {
            return Sort.VALUE;
        }
        if(objectSpec.isMixin()) {
            return Sort.VALUE;
        }
        if(objectSpec.isParentedOrFreeCollection()) {
            return Sort.COLLECTION;
        }
        final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
        if(Persistable.class.isAssignableFrom(correspondingClass)) {
            return Sort.JDO_ENTITY;
        }
        if(mode == Mode.RELAXED) {
            return Sort.UNKNOWN;
        }
        throw new IllegalArgumentException(String.format(
                "Unable to determine what sort of domain object is '%s'", objectSpec.getFullIdentifier()));
    }

    @Override
    public Sort sortOf(final Bookmark bookmark) {
        return sortOf(bookmark, Mode.STRICT);
    }

    @Override
    public Sort sortOf(final Bookmark bookmark, final Mode mode) {
        if(bookmark == null) {
            return null;
        }
        final Class<?> domainType = this.fromObjectType(bookmark.getObjectType());
        return sortOf(domainType, mode);
    }



    @javax.inject.Inject
    SpecificationLoader specificationLookup;

    @javax.inject.Inject
    GridService gridService;

}
