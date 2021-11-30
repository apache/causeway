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
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.metamodel.Config;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.metamodel.v2.Action;
import org.apache.isis.schema.metamodel.v2.Collection;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.schema.metamodel.v2.FacetAttr;
import org.apache.isis.schema.metamodel.v2.Member;
import org.apache.isis.schema.metamodel.v2.MetamodelDto;
import org.apache.isis.schema.metamodel.v2.Param;
import org.apache.isis.schema.metamodel.v2.Property;
import org.apache.isis.schema.metamodel.v2.ScalarParam;
import org.apache.isis.schema.metamodel.v2.VectorParam;

import lombok.val;

class MetaModelExporter {

    SpecificationLoader specificationLookup;

    public MetaModelExporter(final SpecificationLoader specificationLoader) {
        this.specificationLookup = specificationLoader;
    }

    /**
     * The metamodel is populated in two phases, first to create a {@link DomainClassDto} for each ObjectSpecification,
     * and then to populate the members of those domain class types.
     *
     * <p>
     *     This is because the members (and action parameters) all reference the {@link DomainClassDto}s, so these need
     *     to exist first.
     * </p>
     */
    MetamodelDto exportMetaModel(final Config config) {
        MetamodelDto metamodelDto = new MetamodelDto();

        // phase 1: create a domainClassType for each ObjectSpecification
        // these are added into a map for lookups in phase 2
        final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec = _Maps.newHashMap();
        for (final ObjectSpecification specification : specificationLookup.snapshotSpecifications()) {
            DomainClassDto domainClassType = asXsdType(specification);
            domainClassByObjectSpec.put(specification, domainClassType);
        }

        // phase 2: now flesh out the domain class types, passing the map for lookups of the domainClassTypes that
        // correspond to each object members types.
        //
        // we do this in phases, just in case we discover new types along the way as we introspect the members.
        final List<ObjectSpecification> processed = _Lists.newArrayList();
        List<ObjectSpecification> toProcess =
                remaining(domainClassByObjectSpec.keySet(), processed);
        while(!toProcess.isEmpty()) {
            for (final ObjectSpecification specification : toProcess) {
                addFacetsAndMembersTo(specification, domainClassByObjectSpec, config);
            }
            processed.addAll(toProcess);
            toProcess = remaining(domainClassByObjectSpec.keySet(), processed);
        }

        // phase 2.5: check no duplicates
        final Map<String, ObjectSpecification> objectSpecificationByDomainClassId = _Maps.newHashMap();
        final List<String> buf = _Lists.newArrayList();
        for (final Map.Entry<ObjectSpecification, DomainClassDto> entry : domainClassByObjectSpec.entrySet()) {
            final ObjectSpecification objectSpecification = entry.getKey();
            final DomainClassDto domainClassDto = entry.getValue();
            final String id = domainClassDto.getId();
            final ObjectSpecification existing = objectSpecificationByDomainClassId.get(id);
            if(existing != null) {
                if(!existing.getCorrespondingClass().isEnum()) {
                    buf.add(String.format("%s mapped to %s and %s", id, existing, objectSpecification));
                }
            } else {
                objectSpecificationByDomainClassId.put(id, objectSpecification);
            }
        }
        if(buf.size() > 0) {
            throw new IllegalStateException(String.join("\n", buf));
        }

        // phase 3: now copy all domain classes into the metamodel
        for (final ObjectSpecification objectSpecification : _Lists.newArrayList(domainClassByObjectSpec.keySet())) {
            if(shouldIgnore(config, objectSpecification)) {
                continue;
            }
            metamodelDto.getDomainClassDto().add(domainClassByObjectSpec.get(objectSpecification));
        }

        sortDomainClasses(metamodelDto.getDomainClassDto());

        return metamodelDto;
    }

    private boolean shouldIgnore(final Config config, final ObjectSpecification specification) {
        return notInNamespacePrefixes(specification, config) ||
                config.isIgnoreMixins() && specification.isMixin() ||
                config.isIgnoreInterfaces() && specification.getCorrespondingClass().isInterface() ||
                config.isIgnoreAbstractClasses() && Modifier.isAbstract(specification.getCorrespondingClass().getModifiers()) ||
                config.isIgnoreBuiltInValueTypes() && isValueType(specification);
    }

    private static <T> List<T> remaining(final java.util.Collection<T> processed, final java.util.Collection<T> other) {
        final List<T> x = _Lists.newArrayList(processed);
        x.removeAll(other);
        return x;
    }

    private boolean notInNamespacePrefixes(
            final ObjectSpecification specification, final Config config) {
        return !inNamespacePrefixes(specification, config);
    }

    private boolean inNamespacePrefixes(
            final ObjectSpecification specification,
            final Config config) {

        val namespacePrefixes = config.getNamespacePrefixes();
        if(namespacePrefixes.isEmpty()) {
            return false; // export none
        }

        if(config.isNamespacePrefixAny()) {
            return true; // export all
        }

        val logicalTypeName = specification.getLogicalTypeName();
        for (val prefix : namespacePrefixes) {
            if(logicalTypeName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private DomainClassDto asXsdType(
            final ObjectSpecification specification) {

        final DomainClassDto domainClass = new DomainClassDto();

        domainClass.setId(specification.getFullIdentifier());

        if(specification.isManagedBean()) {
            domainClass.setService(true);
        }

        return domainClass;
    }

    private void addFacetsAndMembersTo(
            final ObjectSpecification specification,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {

        final DomainClassDto domainClass = lookupDomainClass(specification, domainClassByObjectSpec, config);
        if(domainClass.getFacets() == null) {
            domainClass.setFacets(new org.apache.isis.schema.metamodel.v2.FacetHolder.Facets());
        }
        addFacets(specification, domainClass.getFacets(), config);

        if(specification.isValueOrIsParented() || isEnum(specification)) {
            return;
        }

        if (specification.isManagedBean()) {
            if(DomainServiceFacet.getNatureOfService(specification).isPresent()) {
                addActions(specification, domainClassByObjectSpec, config);
            }
        } else {
            addProperties(specification, domainClassByObjectSpec, config);
            addCollections(specification, domainClassByObjectSpec, config);
            addActions(specification, domainClassByObjectSpec, config);
        }
    }

    private boolean isEnum(final ObjectSpecification specification) {
        return specification.getCorrespondingClass().isEnum();
    }

    private void addProperties(
            final ObjectSpecification specification,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {
        final DomainClassDto domainClass = lookupDomainClass(specification, domainClassByObjectSpec, config);

        if(domainClass.getProperties() == null) {
            domainClass.setProperties(new DomainClassDto.Properties());
        }

        final List<Property> properties = domainClass.getProperties().getProp();
        specification.streamProperties(MixedIn.INCLUDED)
        .map(otoa -> asXsdType(otoa, domainClassByObjectSpec, config))
        .forEach(properties::add);

        sortMembers(properties);
    }

    private void addCollections(
            final ObjectSpecification specification,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {
        final DomainClassDto domainClass = lookupDomainClass(specification, domainClassByObjectSpec, config);

        if(domainClass.getCollections() == null) {
            domainClass.setCollections(new DomainClassDto.Collections());
        }

        final List<Collection> collections = domainClass.getCollections().getColl();
        specification.streamCollections(MixedIn.INCLUDED)
        .map(otma -> asXsdType(otma, domainClassByObjectSpec, config))
        .forEach(collections::add);

        sortMembers(collections);
    }

    private void addActions(
            final ObjectSpecification specification,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {
        final DomainClassDto domainClass = lookupDomainClass(specification, domainClassByObjectSpec, config);

        if(domainClass.getActions() == null) {
            domainClass.setActions(new DomainClassDto.Actions());
        }

        final List<Action> actions = domainClass.getActions().getAct();
        specification.streamAnyActions(MixedIn.INCLUDED)
        .map(action->asXsdType(action, domainClassByObjectSpec, config))
        .forEach(actions::add);

        sortMembers(actions);
    }

    private Property asXsdType(
            final OneToOneAssociation otoa,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {

        Property propertyType = new Property();
        propertyType.setId(otoa.getId());
        propertyType.setFacets(new org.apache.isis.schema.metamodel.v2.FacetHolder.Facets());
        final ObjectSpecification specification = otoa.getElementType();
        final DomainClassDto value = lookupDomainClass(specification, domainClassByObjectSpec, config);
        propertyType.setType(value);

        addFacets(otoa, propertyType.getFacets(), config);
        return propertyType;
    }

    private Collection asXsdType(
            final OneToManyAssociation otoa,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {
        Collection collectionType = new Collection();
        collectionType.setId(otoa.getId());
        collectionType.setFacets(new org.apache.isis.schema.metamodel.v2.FacetHolder.Facets());
        final ObjectSpecification specification = otoa.getElementType();
        final DomainClassDto value = lookupDomainClass(specification, domainClassByObjectSpec, config);
        collectionType.setType(value);

        addFacets(otoa, collectionType.getFacets(), config);
        return collectionType;
    }

    private Action asXsdType(
            final ObjectAction oa,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {
        Action actionType = new Action();
        actionType.setId(oa.getId());
        actionType.setFacets(new org.apache.isis.schema.metamodel.v2.FacetHolder.Facets());
        actionType.setParams(new Action.Params());

        final ObjectSpecification specification = oa.getReturnType();
        final DomainClassDto value = lookupDomainClass(specification, domainClassByObjectSpec, config);
        actionType.setReturnType(value);

        addFacets(oa, actionType.getFacets(), config);

        val parameters = oa.getParameters();
        final List<Param> params = actionType.getParams().getParam();
        for (final ObjectActionParameter parameter : parameters) {
            params.add(asXsdType(parameter, domainClassByObjectSpec, config));
        }
        return actionType;
    }

    private DomainClassDto lookupDomainClass(
            final ObjectSpecification specification,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {
        DomainClassDto value = domainClassByObjectSpec.get(specification);
        if(value == null) {
            final DomainClassDto domainClass = asXsdType(specification);
            domainClassByObjectSpec.put(specification, domainClass);
            value = domainClass;
        }
        return value;
    }

    private Param asXsdType(
            final ObjectActionParameter parameter,
            final Map<ObjectSpecification, DomainClassDto> domainClassByObjectSpec,
            final Config config) {

        Param parameterType = parameter instanceof OneToOneActionParameter
                ? new ScalarParam()
                        : new VectorParam();
                parameterType.setId(parameter.getId());
                parameterType.setFacets(new org.apache.isis.schema.metamodel.v2.FacetHolder.Facets());

                final ObjectSpecification specification = parameter.getElementType();
                final DomainClassDto value = lookupDomainClass(specification, domainClassByObjectSpec, config);
                parameterType.setType(value);

                addFacets(parameter, parameterType.getFacets(), config);
                return parameterType;
    }

    private void addFacets(
            final FacetHolder facetHolder,
            final org.apache.isis.schema.metamodel.v2.FacetHolder.Facets facets,
            final Config config) {

        final List<org.apache.isis.schema.metamodel.v2.Facet> facetList = facets.getFacet();
        facetHolder.streamFacets()
        .filter(facet -> !facet.getPrecedence().isFallback() || !config.isIgnoreNoop())
        .map(facet -> asXsdType(facet, config))
        .forEach(facetList::add);

        sortFacets(facetList);
    }

    private org.apache.isis.schema.metamodel.v2.Facet asXsdType(
            final Facet facet,
            final Config config) {
        final org.apache.isis.schema.metamodel.v2.Facet facetType = new org.apache.isis.schema.metamodel.v2.Facet();
        facetType.setId(facet.facetType().getCanonicalName());
        facetType.setFqcn(facet.getClass().getCanonicalName());

        addFacetAttributes(facet, facetType, config);

        return facetType;
    }

    private void addFacetAttributes(
            final Facet facet,
            final org.apache.isis.schema.metamodel.v2.Facet facetType,
            final Config config) {

        Map<String, Object> attributeMap = _Maps.newTreeMap();
        facet.visitAttributes(attributeMap::put);

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
            final org.apache.isis.schema.metamodel.v2.Facet facetType,
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

    private static void sortDomainClasses(final List<DomainClassDto> specifications) {
        Collections.sort(specifications, new Comparator<DomainClassDto>() {
            @Override
            public int compare(final DomainClassDto o1, final DomainClassDto o2) {
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

    private void sortFacets(final List<org.apache.isis.schema.metamodel.v2.Facet> facets) {
        Collections.sort(facets, new Comparator<org.apache.isis.schema.metamodel.v2.Facet>() {
            @Override public int compare(final org.apache.isis.schema.metamodel.v2.Facet o1,
                    final org.apache.isis.schema.metamodel.v2.Facet o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }

    private boolean isValueType(final ObjectSpecification specification) {
        return specification.getBeanSort().isValue();
    }

    private String asStr(final Object attributeObj) {
        String str;
        if(attributeObj instanceof Method) {
            str = asStr((Method) attributeObj);
        } else if(attributeObj instanceof String) {
            str = asStr((String) attributeObj);
        } else if(attributeObj instanceof Enum) {
            str = asStr((Enum<?>) attributeObj);
        } else if(attributeObj instanceof Class) {
            str = asStr((Class<?>) attributeObj);
        } else if(attributeObj instanceof Specification) {
            str = asStr((Specification) attributeObj);
        } else if(attributeObj instanceof Facet) {
            str = asStr((Facet) attributeObj);
        } else if(attributeObj instanceof MetaModelExportSupport) {
            str = asStr((MetaModelExportSupport) attributeObj);
        } else if(attributeObj instanceof Pattern) {
            str = asStr((Pattern) attributeObj);
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
        return _Strings.emptyToNull(attributeObj);
    }

    private String asStr(final Specification attributeObj) {
        return attributeObj.getClass().getName();
    }

    private String asStr(final ObjectSpecification attributeObj) {
        return attributeObj.getFullIdentifier();
    }

    private String asStr(final MetaModelExportSupport attributeObj) {
        return attributeObj.toMetamodelString();
    }

    private String asStr(final CommandDtoProcessor attributeObj) {
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

    private String asStr(final Class<?> attributeObj) {
        return attributeObj.getCanonicalName();
    }

    private String asStr(final Enum<?> attributeObj) {
        return attributeObj.name();
    }

    private String asStr(final Method attributeObj) {
        return attributeObj.toGenericString();
    }

    private String asStr(final Object[] list) {
        if(list.length == 0) {
            return null; // skip
        }
        List<String> strings = _Lists.newArrayList();
        for (final Object o : list) {
            String s = asStr(o);
            strings.add(s);
        }
        return String.join(";", strings);
    }

    private String asStr(final List<?> list) {
        if(list.isEmpty()) {
            return null; // skip
        }
        List<String> strings = _Lists.newArrayList();
        for (final Object o : list) {
            String s = asStr(o);
            strings.add(s);
        }
        return String.join(";", strings);
    }


}
