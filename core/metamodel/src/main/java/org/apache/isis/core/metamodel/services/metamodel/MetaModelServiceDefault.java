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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifest2;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandDtoProcessor;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.metamodel.DomainMember;
import org.apache.isis.applib.services.metamodel.DomainModel;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.JdoMetamodelUtil;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.core.metamodel.services.appmanifest.AppManifestProvider;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
    )
public class MetaModelServiceDefault implements MetaModelService {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(MetaModelServiceDefault.class);

    private MetaModelExporter metaModelExporter;

    @PostConstruct
    @Programmatic
    public void init() {
        metaModelExporter = new MetaModelExporter(specificationLookup);
    }

    @Override
    @Programmatic
    public Class<?> fromObjectType(final String objectType) {
        if(objectType == null) {
            return null;
        }
        final ObjectSpecId objectSpecId = ObjectSpecId.of(objectType);
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
        specificationLookup.loadSpecification(domainType);
    }

    // //////////////////////////////////////



    @Override
    @Programmatic
    public DomainModel getDomainModel() {

        final Collection<ObjectSpecification> specifications = specificationLookup.allSpecifications();

        final List<DomainMember> rows = _Lists.newArrayList();
        for (final ObjectSpecification spec : specifications) {
            if (exclude(spec)) {
                continue;
            }
            
            {
                final Stream<ObjectAssociation> properties = 
                        spec.streamAssociations(Contributed.EXCLUDED)
                        .filter(ObjectAssociation.Predicates.PROPERTIES);
                
                properties
                .map(property->(OneToOneAssociation) property)
                .filter(otoa->!exclude(otoa))
                .forEach(otoa->rows.add(new DomainMemberDefault(spec, otoa)));
            }
            
            {
                final Stream<ObjectAssociation> associations = 
                        spec.streamAssociations(Contributed.EXCLUDED)
                        .filter(ObjectAssociation.Predicates.COLLECTIONS);
            
                associations
                .map(collection->(OneToManyAssociation) collection)
                .filter(otma->!exclude(otma))
                .forEach(otma->rows.add(new DomainMemberDefault(spec, otma)));
            }
                
            {
                final Stream<ObjectAction> actions = 
                        spec.streamObjectActions(Contributed.INCLUDED);
                
                actions
                .filter(action->!exclude(action))
                .forEach(action->rows.add(new DomainMemberDefault(spec, action)));
            }
        }

        Collections.sort(rows);

        return new DomainModelDefault(rows);
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
        return isBuiltIn(spec) || spec.isAbstract() || spec.isMixin();
    }

    protected boolean isBuiltIn(final ObjectSpecification spec) {
        final String className = spec.getFullIdentifier();
        return className.startsWith("java") || className.startsWith("org.joda");
    }

    // //////////////////////////////////////

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
        if(JdoMetamodelUtil.isPersistenceEnhanced(correspondingClass)) {
            return Sort.JDO_ENTITY;
        }
        if(mode == Mode.RELAXED) {
            return Sort.UNKNOWN;
        }
        throw new IllegalArgumentException(String.format(
                "Unable to determine what sort of domain object this is: '%s'. Originating domainType: '%s'",
                objectSpec.getFullIdentifier(),
                domainType.getName()
                ));
    }

    @Override
    public Sort sortOf(final Bookmark bookmark, final Mode mode) {
        if(bookmark == null) {
            return null;
        }

        final Class<?> domainType;
        switch (mode) {
        case RELAXED:
            try {
                domainType = this.fromObjectType(bookmark.getObjectType());
            } catch (Exception e) {
                return Sort.UNKNOWN;
            }
            break;

        case STRICT:
            // fall through to...
        default:
            domainType = this.fromObjectType(bookmark.getObjectType());
            break;
        }
        return sortOf(domainType, mode);
    }

    @Override
    public CommandDtoProcessor commandDtoProcessorFor(final String memberIdentifier) {
        final ApplicationFeatureId featureId = ApplicationFeatureId
                .newFeature(ApplicationFeatureType.MEMBER, memberIdentifier);

        final ObjectSpecId objectSpecId = featureId.getObjectSpecId();
        if(objectSpecId == null) {
            return null;
        }

        final ObjectSpecification spec = specificationLookup.lookupBySpecId(objectSpecId);
        if(spec == null) {
            return null;
        }
        final ObjectMember objectMemberIfAny = spec.getMember(featureId.getMemberName());
        if (objectMemberIfAny == null) {
            return null;
        }
        final CommandFacet commandFacet = objectMemberIfAny.getFacet(CommandFacet.class);
        if(commandFacet == null) {
            return null;
        }
        return commandFacet.getProcessor();
    }

    @Override
    public AppManifest2 getAppManifest2() {
        AppManifest appManifest = getAppManifest();
        return appManifest instanceof AppManifest2 ? (AppManifest2) appManifest : null;
    }

    @Override
    public AppManifest getAppManifest() {
        return appManifestProvider.getAppManifest();
    }

    @javax.inject.Inject
    SpecificationLoader specificationLookup;

    @javax.inject.Inject
    GridService gridService;

    @javax.inject.Inject
    AppManifestProvider appManifestProvider;

    @Override
    public MetamodelDto exportMetaModel(final Config config) {
        return metaModelExporter.exportMetaModel(config);
    }


}
