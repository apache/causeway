/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.layout;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.v1_0.ActionHolder;
import org.apache.isis.applib.layout.v1_0.ActionLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.CollectionLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.Column;
import org.apache.isis.applib.layout.v1_0.ObjectLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.PropertyGroup;
import org.apache.isis.applib.layout.v1_0.PropertyLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.Tab;
import org.apache.isis.applib.layout.v1_0.TabGroup;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.ObjectLayoutMetadataService;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryAware;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionPositionFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.BookmarkPolicyFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFaFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.DescribedAsFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.HiddenFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.NamedFacetForActionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.CssClassFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DescribedAsFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.HiddenFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.NamedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.PagedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.SortedByFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetXml;
import org.apache.isis.core.metamodel.facets.object.layoutmetadata.ObjectLayoutMetadataFacet;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.DescribedAsFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.NamedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyXml;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@DomainService(nature = NatureOfService.DOMAIN)
public class ObjectLayoutMetadataServiceDefault
        implements ObjectLayoutMetadataService, DeploymentCategoryAware , SpecificationLoaderAware {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectLayoutMetadataServiceDefault.class);

    private final Set<Class<?>> blacklisted = Sets.newConcurrentHashSet();

    @Programmatic
    public ObjectLayoutMetadata fromXml(Class<?> domainClass) {

        if(blacklisted.contains(domainClass)) {
            return null;
        }

        final String xml;
        final String resourceName = domainClass.getSimpleName() + ".layout.xml";
        try {
            xml = resourceContentOf(domainClass, resourceName);
        } catch (IOException | IllegalArgumentException ex) {

            blacklisted.add(domainClass);
            final String message = String .format(
                    "Failed to locate file %s (relative to %s.class); ex: %s)",
                    resourceName, domainClass.getName(), ex.getMessage());

            LOG.debug(message);
            return null;
        }

        try {
            final ObjectLayoutMetadata metadata = jaxbService.fromXml(ObjectLayoutMetadata.class, xml);
            return metadata;
        } catch(Exception ex) {

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            LOG.warn(message);

            return null;
        }
    }

    private static String resourceContentOf(final Class<?> cls, final String resourceName) throws IOException {
        final URL url = Resources.getResource(cls, resourceName);
        return Resources.toString(url, Charset.defaultCharset());
    }


    @Override
    public ObjectLayoutMetadata normalize(final ObjectLayoutMetadata objectLayoutMetadata, final Class<?> domainClass) {
        // caching (of whether validated) is enabled only in production.
        return objectLayoutMetadata.isNormalized() && deploymentCategory.isProduction()
                ? objectLayoutMetadata
                : deriveAndOverwrite(objectLayoutMetadata, domainClass);
    }

    private ObjectLayoutMetadata deriveAndOverwrite(final ObjectLayoutMetadata objectLayoutMetadata, final Class<?> domainClass) {
        synchronized (objectLayoutMetadata) {
            final ObjectSpecification objectSpec = specificationLookup.loadSpecification(domainClass);
            doDeriveAndOverwrite(objectLayoutMetadata, objectSpec);
            objectLayoutMetadata.setNormalized(true);
        }
        return objectLayoutMetadata;
    }

    private void doDeriveAndOverwrite(final ObjectLayoutMetadata metadata, final ObjectSpecification objectSpec) {

        final Map<String, OneToOneAssociation> oneToOneAssociationById =
                ObjectMember.Util.mapById(getOneToOneAssociations(objectSpec));
        final Map<String, OneToManyAssociation> oneToManyAssociationById =
                ObjectMember.Util.mapById(getOneToManyAssociations(objectSpec));
        final Map<String, ObjectAction> objectActionById =
                ObjectMember.Util.mapById(objectSpec.getObjectActions(Contributed.INCLUDED));

        derive(metadata, oneToOneAssociationById, oneToManyAssociationById, objectActionById);
        overwrite(metadata, oneToOneAssociationById, oneToManyAssociationById, objectActionById);
    }

    /**
     * Ensures that all object members (properties, collections and actions) are in the metadata.
     *
     * <p>
     *     If they are missing then they will be added to default tabs (created on the fly if need be).
     * </p>
     */
    private static void derive(
            final ObjectLayoutMetadata metadata,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById) {

        final LinkedHashMap<String, PropertyLayoutMetadata> propertyIds = metadata.getAllPropertiesById();
        final LinkedHashMap<String, CollectionLayoutMetadata> collectionIds = metadata.getAllCollectionsById();
        final LinkedHashMap<String, ActionLayoutMetadata> actionIds = metadata.getAllActionsById();

        final AtomicReference<PropertyGroup> defaultPropertyGroupRef = new AtomicReference<>();
        final AtomicReference<Column> firstColumnRef = new AtomicReference<>();
        final AtomicReference<TabGroup> lastTabGroupRef = new AtomicReference<>();

        // capture the first column, and also
        // capture the first property group (if any) with the default name ('General')
        metadata.visit(new ObjectLayoutMetadata.VisitorAdapter() {
            @Override
            public void visit(final Column column) {
                firstColumnRef.compareAndSet(null, column);
            }
            @Override
            public void visit(final PropertyGroup propertyGroup) {
                if(MemberGroupLayoutFacet.DEFAULT_GROUP.equals(propertyGroup.getName())) {
                    defaultPropertyGroupRef.compareAndSet(null, propertyGroup);
                }
            }
            @Override
            public void visit(final TabGroup tabGroup) {
                lastTabGroupRef.set(tabGroup);
            }
        });

        // any missing properties will be added to the (first) 'General' property group found
        // if there is no default ('General') property group
        // then one will be added to the first Column of the first Tab.
        final List<String>[] propertyIdTuple = surplusAndMissing(propertyIds.keySet(), oneToOneAssociationById.keySet());
        final List<String> surplusPropertyIds = propertyIdTuple[0];
        final List<String> missingPropertyIds = propertyIdTuple[1];

        for (String surplusPropertyId : surplusPropertyIds) {
            propertyIds.get(surplusPropertyId).setMetadataError("No such property");
        }

        if(!missingPropertyIds.isEmpty()) {
            // ensure that there is a property group to use
            boolean wasSet = defaultPropertyGroupRef.compareAndSet(null, new PropertyGroup(MemberGroupLayoutFacet.DEFAULT_GROUP));
            final PropertyGroup defaultPropertyGroup = defaultPropertyGroupRef.get();
            if(wasSet) {
                firstColumnRef.get().getPropertyGroups().add(defaultPropertyGroup);
            }
            for (final String propertyId : missingPropertyIds) {
                defaultPropertyGroup.getProperties().add(new PropertyLayoutMetadata(propertyId));
            }
        }


        // any missing collections will be added as tabs to the last TabGroup.
        // If there is only a single tab group then a new TabGroup will be added first
        final List<String>[] collectionIdTuple = surplusAndMissing(collectionIds.keySet(), oneToManyAssociationById.keySet());
        final List<String> surplusCollectionIds = collectionIdTuple[0];
        final List<String> missingCollectionIds = collectionIdTuple[1];

        for (String surplusCollectionId : surplusCollectionIds) {
            collectionIds.get(surplusCollectionId).setMetadataError("No such collection");
        }

        if(!missingCollectionIds.isEmpty()) {
            while(metadata.getTabGroups().size() < 2) {
                final TabGroup tabGroup = new TabGroup();
                metadata.getTabGroups().add(tabGroup);
                lastTabGroupRef.set(tabGroup);
            }
            final TabGroup lastTabGroup = lastTabGroupRef.get();
            for (final String collectionId : missingCollectionIds) {
                final Tab tab = new Tab();
                lastTabGroup.getTabs().add(tab);
                Column left = new Column(12);
                tab.setLeft(left);
                final CollectionLayoutMetadata layoutMetadata = new CollectionLayoutMetadata(collectionId);
                layoutMetadata.setDefaultView("table");
                left.getCollections().add(layoutMetadata);
            }
        }

        // any missing actions will be added as domain object actions (in the header)
        final List<String>[] actionIdTuple = surplusAndMissing(actionIds.keySet(), objectActionById.keySet());
        final List<String> surplusActionIds = actionIdTuple[0];
        final List<String> missingActionIds = actionIdTuple[1];

        for (String surplusActionId : surplusActionIds) {
            actionIds.get(surplusActionId).setMetadataError("No such action");
        }

        if(!missingActionIds.isEmpty()) {
            for (String actionId : missingActionIds) {
                metadata.getActions().add(new ActionLayoutMetadata(actionId));
            }
        }
    }

    /**
     * Returns a 2-element array (a tuple) of [first-second, second-first]
     */
    static <T> List<T>[] surplusAndMissing(final java.util.Collection<T> first, final java.util.Collection<T> second){
        final List<T> firstNotSecond = Lists.newArrayList(first);
        firstNotSecond.removeAll(second);
        final List<T> secondNotFirst = Lists.newArrayList(second);
        secondNotFirst.removeAll(first);
        return new List[]{ firstNotSecond, secondNotFirst };
    }

    private void overwrite(
            final ObjectLayoutMetadata metadata,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById) {

        metadata.visit(new ObjectLayoutMetadata.VisitorAdapter() {
            private final Map<String, int[]> propertySequenceByGroup = Maps.newHashMap();
            private int collectionSequence = 1;
            private int actionDomainObjectSequence = 1;
            private int actionPropertyGroupSequence = 1;
            private int actionPropertySequence = 1;
            private int actionCollectionSequence = 1;

            @Override
            public void visit(final ActionLayoutMetadata actionLayoutMetadata) {
                final ActionHolder actionHolder = actionLayoutMetadata.getOwner();
                final ObjectAction objectAction = objectActionById.get(actionLayoutMetadata.getId());
                if(objectAction == null) {
                    return;
                }

                final String memberOrderName;
                final int memberOrderSequence;
                if(actionHolder instanceof PropertyGroup) {
                    final PropertyGroup propertyGroup = (PropertyGroup) actionHolder;
                    final List<PropertyLayoutMetadata> properties = propertyGroup.getProperties();
                    final PropertyLayoutMetadata propertyLayoutMetadata = properties.get(0); // any will do
                    memberOrderName = propertyLayoutMetadata.getId();
                    memberOrderSequence = actionPropertyGroupSequence++;
                } else if(actionHolder instanceof PropertyLayoutMetadata) {
                    final PropertyLayoutMetadata propertyLayoutMetadata = (PropertyLayoutMetadata) actionHolder;
                    memberOrderName = propertyLayoutMetadata.getId();
                    memberOrderSequence = actionPropertySequence++;
                } else if(actionHolder instanceof CollectionLayoutMetadata) {
                    final CollectionLayoutMetadata collectionLayoutMetadata = (CollectionLayoutMetadata) actionHolder;
                    memberOrderName = collectionLayoutMetadata.getId();
                    memberOrderSequence = actionCollectionSequence++;
                } else {
                    // DomainObject
                    memberOrderName = null;
                    memberOrderSequence = actionDomainObjectSequence++;
                }
                FacetUtil.addFacet(
                        new MemberOrderFacetXml(memberOrderName, ""+memberOrderSequence, translationService, objectAction));


                if(actionHolder instanceof PropertyGroup) {
                    if(actionLayoutMetadata.getPosition() == null ||
                            actionLayoutMetadata.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.BELOW ||
                            actionLayoutMetadata.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.RIGHT) {
                        actionLayoutMetadata.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.PANEL);
                    }
                } else if(actionHolder instanceof PropertyLayoutMetadata) {
                    if(actionLayoutMetadata.getPosition() == null ||
                            actionLayoutMetadata.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL_DROPDOWN ||
                            actionLayoutMetadata.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL) {
                        actionLayoutMetadata.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.BELOW);
                    }
                } else {
                    // doesn't do anything for DomainObject or Collection
                    actionLayoutMetadata.setPosition(null);
                }

                FacetUtil.addFacet(ActionPositionFacetForActionXml.create(actionLayoutMetadata, objectAction));
                FacetUtil.addFacet(BookmarkPolicyFacetForActionXml.create(actionLayoutMetadata, objectAction));
                FacetUtil.addFacet(CssClassFacetForActionXml.create(actionLayoutMetadata, objectAction));
                FacetUtil.addFacet(CssClassFaFacetForActionXml.create(actionLayoutMetadata, objectAction));
                FacetUtil.addFacet(DescribedAsFacetForActionXml.create(actionLayoutMetadata, objectAction));
                FacetUtil.addFacet(HiddenFacetForActionLayoutXml.create(actionLayoutMetadata, objectAction));
                FacetUtil.addFacet(NamedFacetForActionXml.create(actionLayoutMetadata, objectAction));
            }

            @Override
            public void visit(final PropertyLayoutMetadata propertyLayoutMetadata) {
                final OneToOneAssociation oneToOneAssociation = oneToOneAssociationById.get(propertyLayoutMetadata.getId());
                if(oneToOneAssociation == null) {
                    return;
                }

                FacetUtil.addFacet(CssClassFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));
                FacetUtil.addFacet(DescribedAsFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));
                FacetUtil.addFacet(HiddenFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));
                FacetUtil.addFacet(LabelAtFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));
                FacetUtil.addFacet(MultiLineFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));
                FacetUtil.addFacet(NamedFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));
                FacetUtil.addFacet(RenderedAdjustedFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));
                FacetUtil.addFacet(TypicalLengthFacetForPropertyXml.create(propertyLayoutMetadata, oneToOneAssociation));

                // @MemberOrder#name based on owning property group, @MemberOrder#sequence monotonically increasing
                final PropertyGroup propertyGroup = propertyLayoutMetadata.getOwner();
                final String groupName = propertyGroup.getName();
                final String sequence = nextInSequenceFor(groupName, propertySequenceByGroup);
                FacetUtil.addFacet(
                        new MemberOrderFacetXml(groupName, sequence, translationService, oneToOneAssociation));
            }

            @Override
            public void visit(final CollectionLayoutMetadata collectionLayoutMetadata) {
                final OneToManyAssociation oneToManyAssociation = oneToManyAssociationById.get(collectionLayoutMetadata.getId());
                if(oneToManyAssociation == null) {
                    return;
                }

                FacetUtil.addFacet(CssClassFacetForCollectionXml.create(collectionLayoutMetadata, oneToManyAssociation));
                FacetUtil.addFacet(DefaultViewFacetForCollectionXml.create(collectionLayoutMetadata, oneToManyAssociation));
                FacetUtil.addFacet(DescribedAsFacetForCollectionXml.create(collectionLayoutMetadata, oneToManyAssociation));
                FacetUtil.addFacet(HiddenFacetForCollectionXml.create(collectionLayoutMetadata, oneToManyAssociation));
                FacetUtil.addFacet(NamedFacetForCollectionXml.create(collectionLayoutMetadata, oneToManyAssociation));
                FacetUtil.addFacet(PagedFacetForCollectionXml.create(collectionLayoutMetadata, oneToManyAssociation));
                FacetUtil.addFacet(SortedByFacetForCollectionXml.create(collectionLayoutMetadata, oneToManyAssociation));

                // @MemberOrder#name based on the collection's id (so that each has a single "member group")
                final String groupName = collectionLayoutMetadata.getId();
                final String sequence = "" + collectionSequence++;
                FacetUtil.addFacet(
                        new MemberOrderFacetXml(groupName, sequence, translationService, oneToManyAssociation));

                // if there is only a single column and no other contents, then copy the collection Id onto the tab'
                final Column column = collectionLayoutMetadata.getOwner();
                final Tab tab = column.getOwner();
                if(tab.getContents().size() == 1) {
                    final String collectionName = oneToManyAssociation.getName();
                    tab.setName(collectionName);
                }
            }

            private String nextInSequenceFor(
                    final String key, final Map<String, int[]> seqByKey) {
                synchronized (seqByKey) {
                    int[] holder = seqByKey.get(key);
                    if(holder == null) {
                        holder = new int[]{0};
                        seqByKey.put(key, holder);
                    }
                    holder[0]++;
                    return ""+holder[0];
                }
            }
        });
    }

    private static List<OneToOneAssociation> getOneToOneAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec
                .getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.PROPERTIES);
        return associations;
    }
    private static List<OneToManyAssociation> getOneToManyAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec
                .getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.COLLECTIONS);
        return associations;
    }


    @Override
    public ObjectLayoutMetadata toMetadata(final Object domainObject) {
        return toMetadata(domainObject.getClass());
    }

    @Override
    public ObjectLayoutMetadata toMetadata(final Class<?> domainClass) {
        final ObjectSpecification objectSpec = specificationLookup.loadSpecification(domainClass);
        final ObjectLayoutMetadataFacet facet = objectSpec.getFacet(ObjectLayoutMetadataFacet.class);
        return facet != null? facet.getMetadata(): null;
    }



    //region > injected dependencies
    private DeploymentCategory deploymentCategory;

    @Override
    public void setDeploymentCategory(final DeploymentCategory deploymentCategory) {
        this.deploymentCategory = deploymentCategory;
    }

    private SpecificationLoader specificationLookup;

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }


    @Inject
    JaxbService jaxbService;

    @Inject
    TranslationService translationService;

    //endregion

}
