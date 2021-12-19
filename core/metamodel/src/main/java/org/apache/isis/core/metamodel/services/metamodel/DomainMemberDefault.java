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

import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.services.metamodel.DomainMember;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.validating.PropertyValidateFacet;
import org.apache.isis.core.metamodel.services.devutils.MemberType;
import org.apache.isis.core.metamodel.spec.Hierarchical;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.MixedInMember;

import lombok.val;

@XmlRootElement(name = "domain-member")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DomainMemberDefault implements DomainMember {

    private final ObjectSpecification spec;
    private final MemberType memberType;
    private final ObjectMember member;
    private ObjectAction action;

    // to support JAX-B marshaling
    DomainMemberDefault(){
        throw _Exceptions.unexpectedCodeReach();
    }

    DomainMemberDefault(final ObjectSpecification spec, final OneToOneAssociation property) {
        this.spec = spec;
        this.member = property;
        this.memberType = MemberType.PROPERTY;
    }

    DomainMemberDefault(final ObjectSpecification spec, final OneToManyAssociation collection) {
        this.spec = spec;
        this.member = collection;
        this.memberType = MemberType.COLLECTION;
    }

    DomainMemberDefault(final ObjectSpecification spec, final ObjectAction action) {
        this.spec = spec;
        this.member = this.action = action;
        this.memberType = MemberType.ACTION;
    }

    @XmlElement @Override
    public String getClassType() {

        val isService = Stream.concat(
                    Stream.of(spec),
                    spec.subclasses(Hierarchical.Depth.DIRECT).stream())
                .anyMatch(ObjectSpecification::isManagedBean);

        return isService
                    ? "2 Service"
                    : spec.isValue()
                            ? "3 Value"
                            : spec.isNonScalar()
                                    ? "4 Collection"
                                    : "1 Object";
    }


    @XmlElement @Override
    public String getLogicalTypeName() {
        return spec.getLogicalTypeName();
    }

    @Override
    public String getNamespace() {
        return spec.getLogicalType().getNamespace();
    }

    @XmlElement @Override
    public String getClassName() {
        final String fullIdentifier = spec.getFullIdentifier();
        final int lastDot = fullIdentifier.lastIndexOf(".");
        return lastDot>0 && lastDot < fullIdentifier.length()-1
                ?fullIdentifier.substring(lastDot+1,fullIdentifier.length())
                        :fullIdentifier;
    }

    @XmlElement @Override
    public String getPackageName() {
        final String fullIdentifier = spec.getFullIdentifier();
        final int lastDot = fullIdentifier.lastIndexOf(".");
        return lastDot>0?fullIdentifier.substring(0,lastDot):fullIdentifier;
    }

    @XmlElement @Override
    public String getType() {
        return memberType.name().toLowerCase();
    }

    @XmlElement @Override
    public String getMemberName() {
        return member.getId();
    }

    @XmlElement @Override
    public String getNumParams() {
        return action!=null?""+action.getParameterCount():"";
    }

    @XmlElement @Override
    public boolean isMixedIn() {
        return member instanceof MixedInMember;
    }

    @XmlElement @Override
    public String getMixin() {
        if(member instanceof MixedInMember) {
            final MixedInMember mixedInMember = (MixedInMember) this.member;

            final ObjectSpecification mixinType = mixedInMember.getMixinType();
            return mixinType.getCorrespondingClass().getSimpleName();
        }
        return "";
    }

    @XmlElement @Override
    public String getHidden() {
        return interpret(HiddenFacet.class);
    }

    @XmlElement @Override
    public String getDisabled() {
        return interpret(DisabledFacet.class);
    }

    @XmlElement @Override
    public String getChoices() {
        switch (memberType) {
        case PROPERTY:
            return interpretRowAndFacet(PropertyChoicesFacet.class);
        case COLLECTION:
            return "";
        case ACTION:
        default:
            val parameters = this.action.getParameters();
            final SortedSet<String> interpretations = _Sets.newTreeSet();
            for (ObjectActionParameter param : parameters) {
                final ActionParameterChoicesFacet facet = param.getFacet(ActionParameterChoicesFacet.class);
                addIfNotEmpty(interpretFacet(facet), interpretations);
            }
            return interpretations.isEmpty()
                    ? ""
                    : interpretations.stream().collect(Collectors.joining(";"));
        }
    }

    @XmlElement @Override
    public String getAutoComplete() {
        if(memberType == MemberType.PROPERTY) {
            return interpretRowAndFacet(PropertyAutoCompleteFacet.class);
        } else if(memberType == MemberType.COLLECTION) {
            return "";
        } else {
            val parameters = this.action.getParameters();
            final SortedSet<String> interpretations = _Sets.newTreeSet();
            for (ObjectActionParameter param : parameters) {
                final ActionParameterAutoCompleteFacet facet = param.getFacet(ActionParameterAutoCompleteFacet.class);
                addIfNotEmpty(interpretFacet(facet), interpretations);
            }
            return interpretations.stream().collect(Collectors.joining(";"));
        }
    }

    @XmlElement @Override
    public String getDefault() {
        if(memberType == MemberType.PROPERTY) {
            return interpretRowAndFacet(PropertyDefaultFacet.class);
        } else if(memberType == MemberType.COLLECTION) {
            return "";
        } else {
            val parameters = this.action.getParameters();
            final SortedSet<String> interpretations = _Sets.newTreeSet();
            for (ObjectActionParameter param : parameters) {
                final ActionParameterDefaultsFacet facet = param.getFacet(ActionParameterDefaultsFacet.class);
                addIfNotEmpty(interpretFacet(facet), interpretations);
            }
            return !interpretations.isEmpty()
                    ? String.join(";", interpretations)
                    : "";
        }
    }

    @XmlElement @Override
    public String getValidate() {
        if(memberType == MemberType.PROPERTY) {
            return interpretRowAndFacet(PropertyValidateFacet.class);
        } else if(memberType == MemberType.COLLECTION) {
            return String.join(";", _Sets.newTreeSet());
        } else {
            return interpretRowAndFacet(ActionValidationFacet.class);
        }
    }

    // -- COMPARATOR

    @Override
    public int compareTo(final DomainMember o) {
        return comparator.compare(this, o);
    }

    // -- HELPER

    private String interpretRowAndFacet(final Class<? extends Facet> facetClass) {
        final Facet facet = member.getFacet(facetClass);
        return interpretFacet(facet);
    }

    private static void addIfNotEmpty(final String str, final SortedSet<String> set) {
        if(!_Strings.isNullOrEmpty(str)) {
            set.add(str);
        }
    }

    private String interpret(final Class<? extends Facet> cls) {
        return interpretFacet(member.getFacet(cls));
    }

    private static String interpretFacet(final Facet facet) {
        if (facet == null
                || facet.getPrecedence().isFallback()) {
            return "";
        }
        if (facet instanceof ImperativeFacet) {
            ImperativeFacet imperativeFacet = (ImperativeFacet) facet;
            return imperativeFacet.getMethods().getFirstOrFail().getName();
        }
        final String name = facet.getClass().getSimpleName();
        if (ignore(name)) {
            return "";
        }
        //[ahuber] not sure why abbreviated, so I disabled abbreviation
        //        final String abbr = StringExtensions.toAbbreviation(name);
        //        return abbr.length()>0 ? abbr : name;

        return name;
    }

    protected static boolean ignore(final String name) {
        return Arrays.asList("PropertyValidateFacetDefault","PropertyDefaultFacetDerivedFromDefaultedFacet")
                .contains(name);
    }

    private static final Comparator<DomainMember> comparator =
            Comparator.comparing(DomainMember::getClassType)
            .thenComparing(DomainMember::getClassName)
            .thenComparing(DomainMember::getType, Comparator.reverseOrder()) // desc
            .thenComparing(DomainMember::getMemberName);


}
