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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.command.CommandDtoProcessor;
import org.apache.isis.applib.services.metamodel.MetaModelService6;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.schema.metamodel.v1.Action;
import org.apache.isis.schema.metamodel.v1.Collection;
import org.apache.isis.schema.metamodel.v1.DomainClass;
import org.apache.isis.schema.metamodel.v1.FacetAttr;
import org.apache.isis.schema.metamodel.v1.Member;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;
import org.apache.isis.schema.metamodel.v1.Param;
import org.apache.isis.schema.metamodel.v1.Property;
import org.apache.isis.schema.metamodel.v1.ScalarParam;
import org.apache.isis.schema.metamodel.v1.VectorParam;

class MetaModelExporter {

    @SuppressWarnings("unused")
    private final static Logger LOG = LoggerFactory.getLogger(MetaModelExporter.class);

    SpecificationLoader specificationLookup;

    public MetaModelExporter(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }

    /**
     * The metamodel is populated in two phases, first to create a {@link DomainClass} for each ObjectSpecification,
     * and then to populate the members of those domain class types.
     *
     * <p>
     *     This is because the members (and action parameters) all reference the {@link DomainClass}s, so these need
     *     to exist first.
     * </p>
     */
    MetamodelDto exportMetaModel(final MetaModelService6.Flags flags) {
        MetamodelDto metamodelDto = new MetamodelDto();

        // phase 1: create a domainClassType for each ObjectSpecification
        // these are also added into a map for lookups in phase 2
        Map<ObjectSpecification, DomainClass> domainClassByObjectSpec = Maps.newHashMap();
        for (final ObjectSpecification specification : specificationLookup.allSpecifications()) {
            DomainClass domainClassType = asXsdType(specification, flags);
            metamodelDto.getDomainClass().add(domainClassType);
            domainClassByObjectSpec.put(specification, domainClassType);
        }

        // phase 2: now flesh out the domain class types, passing the map for lookups of the domainClassTypes that
        // correspond to each object members types.
        for (final ObjectSpecification specification : specificationLookup.allSpecifications()) {
            addMembersTo(specification, domainClassByObjectSpec, flags);
        }

        sortDomainClasses(metamodelDto.getDomainClass());

        return metamodelDto;
    }

    private DomainClass asXsdType(
            final ObjectSpecification specification,
            final MetaModelService6.Flags flags) {

        final DomainClass domainClass = new DomainClass();
        domainClass.setId(specification.getFullIdentifier());
        domainClass.setFacets(new org.apache.isis.schema.metamodel.v1.FacetHolder.Facets());
        domainClass.setProperties(new DomainClass.Properties());
        domainClass.setCollections(new DomainClass.Collections());
        domainClass.setActions(new DomainClass.Actions());

        return domainClass;
    }


    private void addMembersTo(
            final ObjectSpecification specification,
            final Map<ObjectSpecification, DomainClass> domainClassByObjectSpec,
            final MetaModelService6.Flags flags) {

        DomainClass domainClass = domainClassByObjectSpec.get(specification);

        addFacets(specification, domainClass.getFacets(), flags);
        addMembers(specification, domainClassByObjectSpec, flags);
    }


    private void addMembers(
            final ObjectSpecification specification,
            final Map<ObjectSpecification, DomainClass> domainClassByObjectSpec,
            final MetaModelService6.Flags flags) {

        final DomainClass domainClass = domainClassByObjectSpec.get(specification);

        final List<ObjectAssociation> oneToOneAssociations =
                specification.getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.PROPERTIES);
        final List<Property> properties = domainClass.getProperties().getProp();
        for (final ObjectAssociation association : oneToOneAssociations) {
            OneToOneAssociation otoa = (OneToOneAssociation) association;
            properties.add(asXsdType(otoa, domainClassByObjectSpec, flags));
        }
        sortMembers(properties);

        final List<ObjectAssociation> oneToManyAssociations =
                specification.getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.COLLECTIONS);
        final List<Collection> collections = domainClass.getCollections().getColl();
        for (final ObjectAssociation association : oneToManyAssociations) {
            OneToManyAssociation otma = (OneToManyAssociation) association;
            collections.add(asXsdType(otma, domainClassByObjectSpec, flags));
        }
        sortMembers(collections);

        final List<ObjectAction> objectActions =
                specification.getObjectActions(Contributed.INCLUDED);
        final List<Action> actions = domainClass.getActions().getAct();
        for (final ObjectAction action : objectActions) {
            actions.add(asXsdType(action, domainClassByObjectSpec, flags));
        }
        sortMembers(actions);
    }

    private Property asXsdType(
            final OneToOneAssociation otoa,
            final Map<ObjectSpecification, DomainClass> domainClassByObjectSpec,
            final MetaModelService6.Flags flags) {

        Property propertyType = new Property();
        propertyType.setId(otoa.getId());
        propertyType.setFacets(new org.apache.isis.schema.metamodel.v1.FacetHolder.Facets());
        final ObjectSpecification specification = otoa.getSpecification();
        final DomainClass value = domainClassByObjectSpec.get(specification);
        if(value == null) {
            throw new IllegalStateException("Could not locate domainClass for: " + specification);
        }
        propertyType.setType(value);

        addFacets(otoa, propertyType.getFacets(), flags);
        return propertyType;
    }

    private Collection asXsdType(
            final OneToManyAssociation otoa,
            final Map<ObjectSpecification, DomainClass> domainClassByObjectSpec,
            final MetaModelService6.Flags flags) {
        Collection collectionType = new Collection();
        collectionType.setId(otoa.getId());
        collectionType.setFacets(new org.apache.isis.schema.metamodel.v1.FacetHolder.Facets());
        final ObjectSpecification specification = otoa.getSpecification();
        final DomainClass value = domainClassByObjectSpec.get(specification);
        if(value == null) {
            throw new IllegalStateException("Could not locate domainClass for: " + specification);
        }
        collectionType.setType(value);

        addFacets(otoa, collectionType.getFacets(), flags);
        return collectionType;
    }

    private Action asXsdType(
            final ObjectAction oa,
            final Map<ObjectSpecification, DomainClass> domainClassByObjectSpec,
            final MetaModelService6.Flags flags) {
        Action actionType = new Action();
        actionType.setId(oa.getId());
        actionType.setFacets(new org.apache.isis.schema.metamodel.v1.FacetHolder.Facets());
        actionType.setParams(new Action.Params());

        final ObjectSpecification specification = oa.getReturnType();
        final DomainClass value = domainClassByObjectSpec.get(specification);
        if(value == null) {
            throw new IllegalStateException("Could not locate domainClass for: " + specification);
        }
        actionType.setReturnType(value);

        addFacets(oa, actionType.getFacets(), flags);

        final List<ObjectActionParameter> parameters = oa.getParameters();
        final List<Param> params = actionType.getParams().getParam();
        for (final ObjectActionParameter parameter : parameters) {
            params.add(asXsdType(parameter, domainClassByObjectSpec, flags));
        }
        return actionType;
    }

    private Param asXsdType(
            final ObjectActionParameter parameter,
            final Map<ObjectSpecification, DomainClass> domainClassByObjectSpec,
            final MetaModelService6.Flags flags) {

        Param parameterType = parameter instanceof OneToOneActionParameter
                                    ? new ScalarParam()
                                    : new VectorParam();
        parameterType.setId(parameter.getId());
        parameterType.setFacets(new org.apache.isis.schema.metamodel.v1.FacetHolder.Facets());

        final ObjectSpecification specification = parameter.getSpecification();
        final DomainClass value = domainClassByObjectSpec.get(specification);
        if(value == null) {
            throw new IllegalStateException("Could not locate domainClass for: " + specification);
        }
        parameterType.setType(value);

        addFacets(parameter, parameterType.getFacets(), flags);
        return parameterType;
    }

    private void addFacets(
            final FacetHolder facetHolder,
            final org.apache.isis.schema.metamodel.v1.FacetHolder.Facets facets,
            final MetaModelService6.Flags flags) {

        final Class<? extends Facet>[] facetTypes = facetHolder.getFacetTypes();
        for (final Class<? extends Facet> facetType : facetTypes) {
            final Facet facet = facetHolder.getFacet(facetType);
            if(!facet.isNoop() || !flags.isIgnoreNoop()) {
                facets.getFacet().add(asXsdType(facet, flags));
            }
        }
        sortFacets(facets.getFacet());
    }

    private org.apache.isis.schema.metamodel.v1.Facet asXsdType(
            final Facet facet,
            final MetaModelService6.Flags flags) {
        final org.apache.isis.schema.metamodel.v1.Facet facetType = new org.apache.isis.schema.metamodel.v1.Facet();
        facetType.setId(facet.facetType().getCanonicalName());
        facetType.setFqcn(facet.getClass().getCanonicalName());

        addFacetAttributes(facet, facetType, flags);

        return facetType;
    }

    private void addFacetAttributes(
            final Facet facet,
            final org.apache.isis.schema.metamodel.v1.Facet facetType,
            final MetaModelService6.Flags flags) {

        Map<String, Object> attributeMap = Maps.newTreeMap();
        facet.appendAttributesTo(attributeMap);

        for (final String key : attributeMap.keySet()) {
            Object attributeObj = attributeMap.get(key);
            if(attributeObj == null) {
                continue;
            }

            String str = asStr(attributeObj);
            addAttribute(facetType,key, str);
        }

        sortFacetAttributes(facetType.getAttr());
    }

    private void addAttribute(
            final org.apache.isis.schema.metamodel.v1.Facet facetType,
            final String key, final String str) {
        if(str == null) {
            return;
        }
        FacetAttr attributeDto = new FacetAttr();
        attributeDto.setName(key);
        attributeDto.setValue(str);
        facetType.getAttr().add(attributeDto);
    }

    private void sortFacetAttributes(final List<FacetAttr> attributes) {
        Collections.sort(attributes, new Comparator<FacetAttr>() {
            @Override
            public int compare(final FacetAttr o1, final FacetAttr o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    private static void sortDomainClasses(final List<DomainClass> specifications) {
        Collections.sort(specifications, new Comparator<DomainClass>() {
            @Override
            public int compare(final DomainClass o1, final DomainClass o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    private void sortMembers(final List<? extends Member> members) {
        Collections.sort(members, new Comparator<Member>() {
            @Override public int compare(final Member o1, final Member o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    private void sortFacets(final List<org.apache.isis.schema.metamodel.v1.Facet> facets) {
        Collections.sort(facets, new Comparator<org.apache.isis.schema.metamodel.v1.Facet>() {
            @Override public int compare(final org.apache.isis.schema.metamodel.v1.Facet o1, final org.apache.isis.schema.metamodel.v1.Facet o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    private String asStr(final Object attributeObj) {
        String str;
        if(attributeObj instanceof Method) {
            str = asStr((Method) attributeObj);
        } else if(attributeObj instanceof String) {
            str = asStr((String) attributeObj);
        } else if(attributeObj instanceof Enum) {
            str = asStr((Enum) attributeObj);
        } else if(attributeObj instanceof Class) {
            str = asStr((Class) attributeObj);
        } else if(attributeObj instanceof Specification) {
            str = asStr((Specification) attributeObj);
        } else if(attributeObj instanceof Facet) {
            str = asStr((Facet) attributeObj);
        } else if(attributeObj instanceof JdoNamedQuery) {
            str = asStr((JdoNamedQuery) attributeObj);
        } else if(attributeObj instanceof Pattern) {
            str = asStr((Pattern) attributeObj);
        } else if(attributeObj instanceof PublishedObject.PayloadFactory) {
            str = asStr((PublishedAction.PayloadFactory) attributeObj);
        } else if(attributeObj instanceof PublishedAction.PayloadFactory) {
            str = asStr((PublishedAction.PayloadFactory) attributeObj);
        } else if(attributeObj instanceof CommandDtoProcessor) {
            str = asStr((CommandDtoProcessor) attributeObj);
        } else if(attributeObj instanceof ObjectSpecification) {
            str = asStr((ObjectSpecification) attributeObj);
        } else if(attributeObj instanceof ObjectMember) {
            str = asStr((ObjectMember) attributeObj);
        } else if(attributeObj instanceof List) {
            str = asStr((List<?>) attributeObj);
        } else if(attributeObj instanceof Object[]) {
            str = asStr((Object[]) attributeObj);
        } else  {
            str = "" + attributeObj;
        }
        return str;
    }

    private String asStr(final String attributeObj) {
        return Strings.emptyToNull(attributeObj);
    }

    private String asStr(final Specification attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final ObjectSpecification attributeObj) {
        return attributeObj.getFullIdentifier();
    }

    private String asStr(final JdoNamedQuery attributeObj) {
        return attributeObj.getName();
    }

    private String asStr(final CommandDtoProcessor attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final PublishedAction.PayloadFactory attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final PublishedObject.PayloadFactory attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final Pattern attributeObj) {
        return attributeObj.pattern();
    }

    private String asStr(final Facet attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final ObjectMember attributeObj) {
        return attributeObj.getId();
    }

    private String asStr(final Class attributeObj) {
        return attributeObj.getCanonicalName();
    }

    private String asStr(final Enum attributeObj) {
        return attributeObj.name();
    }

    private String asStr(final Method attributeObj) {
        return attributeObj.toGenericString();
    }

    private String asStr(final Object[] list) {
        if(list.length == 0) {
            return null; // skip
        }
        List<String> strings = Lists.newArrayList();
        for (final Object o : list) {
            String s = asStr(o);
            strings.add(s);
        }
        return Joiner.on(",").join(strings);
    }

    private String asStr(final List<?> list) {
        if(list.isEmpty()) {
            return null; // skip
        }
        List<String> strings = Lists.newArrayList();
        for (final Object o : list) {
            String s = asStr(o);
            strings.add(s);
        }
        return Joiner.on(",").join(strings);
    }



}
