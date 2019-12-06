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
package org.apache.isis.metamodel.services.metamodel;

import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandDtoProcessor;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.metamodel.DomainMember;
import org.apache.isis.applib.services.metamodel.DomainModel;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.ioc.BeanSort;
import org.apache.isis.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureType;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

@Service
@Named("isisMetaModel.MetaModelServiceDefault")
@Log4j2
public class MetaModelServiceDefault implements MetaModelService {

    private MetaModelExporter metaModelExporter;

    @PostConstruct
    public void init() {
        metaModelExporter = new MetaModelExporter(specificationLoader);
    }

    @Override
    public Class<?> fromObjectType(final String objectType) {
        if(objectType == null) {
            return null;
        }
        final ObjectSpecId objectSpecId = ObjectSpecId.of(objectType);
        final ObjectSpecification objectSpecification = specificationLoader.lookupBySpecIdElseLoad(objectSpecId);
        return objectSpecification != null? objectSpecification.getCorrespondingClass(): null;
    }

    @Override
    public String toObjectType(final Class<?> domainType) {
        if(domainType == null) {
            return null;
        }
        final ObjectSpecification objectSpecification = specificationLoader.loadSpecification(domainType);
        final ObjectSpecIdFacet objectSpecIdFacet = objectSpecification.getFacet(ObjectSpecIdFacet.class);
        final ObjectSpecId objectSpecId = objectSpecIdFacet.value();
        return objectSpecId.asString();
    }

    @Override
    public void rebuild(final Class<?> domainType) {

        gridService.remove(domainType);
        specificationLoader.reloadSpecification(domainType);
    }

    // //////////////////////////////////////



    @Override
    public DomainModel getDomainModel() {

        final Collection<ObjectSpecification> specifications = specificationLoader.snapshotSpecifications();

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
    public BeanSort sortOf(
            final Class<?> domainType, final Mode mode) {
        if(domainType == null) {
            return null;
        }
        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(domainType);
        if(objectSpec.isManagedBean()) {
            return BeanSort.MANAGED_BEAN;
        }
        if(objectSpec.isViewModel()) {
            return BeanSort.VIEW_MODEL;
        }
        if(objectSpec.isValue()) {
            return BeanSort.VALUE;
        }
        if(objectSpec.isMixin()) {
            return BeanSort.MIXIN;
        }
        if(objectSpec.isParentedOrFreeCollection()) {
            return BeanSort.COLLECTION;
        }
        if(objectSpec.isEntity()) {
            return BeanSort.ENTITY;
        }
        if(mode == Mode.RELAXED) {
            return BeanSort.UNKNOWN;
        }
        throw new IllegalArgumentException(String.format(
                "Unable to determine what sort of domain object this is: '%s'. Originating domainType: '%s'",
                objectSpec.getFullIdentifier(),
                domainType.getName()
                ));
    }

    @Override
    public BeanSort sortOf(final Bookmark bookmark, final Mode mode) {
        if(bookmark == null) {
            return null;
        }

        final Class<?> domainType;
        switch (mode) {
        case RELAXED:
            try {
                domainType = this.fromObjectType(bookmark.getObjectType());
            } catch (Exception e) {
                return BeanSort.UNKNOWN;
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

        final ObjectSpecification spec = specificationLoader.lookupBySpecIdElseLoad(objectSpecId);
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
    public MetamodelDto exportMetaModel(final Config config) {
        return metaModelExporter.exportMetaModel(config);
    }

    @Inject ServiceRegistry serviceRegistry;

    //-------------
    //TODO[2112] workaround circular dependency issues when bootstrapping ...
    @Inject SpecificationLoader specificationLoader;
    @Inject GridService gridService;
    //
    //    private _Lazy<SpecificationLoader> specificationLoader = _Lazy.threadSafe(()->
    //        serviceRegistry.lookupServiceElseFail(SpecificationLoader.class));
    //    //
    //    private _Lazy<GridService> gridService = _Lazy.threadSafe(()->
    //        serviceRegistry.lookupServiceElseFail(GridService.class));
    //    //
    //    private _Lazy<MetaModelExporter> metaModelExporter = _Lazy.threadSafe(()->
    //        new MetaModelExporter(specificationLoader.get()));
    //-------------

}
