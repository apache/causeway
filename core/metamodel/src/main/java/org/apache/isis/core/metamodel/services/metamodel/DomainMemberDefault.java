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
import java.util.List;
import java.util.SortedSet;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import org.apache.isis.applib.services.metamodel.DomainMember;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.validate.CollectionValidateRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionChoicesFacet;
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
import org.apache.isis.core.metamodel.specloader.specimpl.ContributeeMember;
import org.apache.isis.core.metamodel.specloader.specimpl.ContributeeMember2;
import org.apache.isis.core.metamodel.specloader.specimpl.MixedInMember;
import org.apache.isis.core.metamodel.specloader.specimpl.MixedInMember2;

public class DomainMemberDefault implements DomainMember {

    private final ObjectSpecification spec;
    private final MemberType memberType;
    private final ObjectMember member;
    private ObjectAction action;

    DomainMemberDefault(ObjectSpecification spec, OneToOneAssociation property) {
        this.spec = spec;
        this.member = property;
        this.memberType = MemberType.PROPERTY;
    }

    DomainMemberDefault(ObjectSpecification spec, OneToManyAssociation collection) {
        this.spec = spec;
        this.member = collection;
        this.memberType = MemberType.COLLECTION;
    }

    DomainMemberDefault(ObjectSpecification spec, ObjectAction action) {
        this.spec = spec;
        this.member = this.action = action;
        this.memberType = MemberType.ACTION;
    }

    @Override public String getClassType() {
        boolean service = false;
        for(ObjectSpecification subspecs: spec.subclasses(Hierarchical.Depth.DIRECT)) {
            service = service || subspecs.isService();
        }
        return service || spec.isService() ?"2 Service":spec.isValue()?"3 Value":spec.isParentedOrFreeCollection()?"4 Collection":"1 Object";
    }
    @Override public String getClassName() {
        final String fullIdentifier = spec.getFullIdentifier();
        final int lastDot = fullIdentifier.lastIndexOf(".");
        return lastDot>0 && lastDot < fullIdentifier.length()-1
                ?fullIdentifier.substring(lastDot+1,fullIdentifier.length())
                        :fullIdentifier;
    }
    @Override public String getPackageName() {
        final String fullIdentifier = spec.getFullIdentifier();
        final int lastDot = fullIdentifier.lastIndexOf(".");
        return lastDot>0?fullIdentifier.substring(0,lastDot):fullIdentifier;
    }
    @Override public String getType() {
        return memberType.name().toLowerCase();
    }
    @Override public String getMemberName() {
        return member.getId();
    }
    @Override public String getNumParams() {
        return action!=null?""+action.getParameterCount():"";
    }

    @Override public boolean isContributed() {
        return member instanceof ContributeeMember;
    }

    @Override public String getContributedBy() {
        if(member instanceof ContributeeMember2) {
            final ObjectSpecification serviceContributedBy = ((ContributeeMember2) member).getServiceContributedBy();
            return serviceContributedBy.getCorrespondingClass().getSimpleName();
        }
        return "";
    }

    @Override public boolean isMixedIn() {
        return member instanceof MixedInMember;
    }

    @Override public String getMixin() {
        if(member instanceof MixedInMember2) {
            final MixedInMember2 mixedInMember = (MixedInMember2) this.member;

            final ObjectSpecification mixinType = mixedInMember.getMixinType();
            return mixinType.getCorrespondingClass().getSimpleName();
        }
        return "";
    }

    @Override public String getHidden() {
        return interpret(HiddenFacet.class);
    }
    @Override public String getDisabled() {
        return interpret(DisabledFacet.class);
    }
    @Override public String getChoices() {
        switch (memberType) {
        case PROPERTY:
            return interpretRowAndFacet(PropertyChoicesFacet.class);
        case COLLECTION:
            return "";
        default:
            final List<ObjectActionParameter> parameters = this.action.getParameters();
            final SortedSet<String> interpretations = Sets.newTreeSet();
            for (ObjectActionParameter param : parameters) {
                final ActionParameterChoicesFacet facet = param.getFacet(ActionParameterChoicesFacet.class);
                addIfNotEmpty(interpretFacet(facet), interpretations);
            }
            return !interpretations.isEmpty() ?
                    Joiner.on(";").join(interpretations) :
                        interpretRowAndFacet(ActionChoicesFacet.class);
        }
    }
    @Override public String getAutoComplete() {
        if(memberType == MemberType.PROPERTY) {
            return interpretRowAndFacet(PropertyAutoCompleteFacet.class);
        } else if(memberType == MemberType.COLLECTION) {
            return "";
        } else {
            final List<ObjectActionParameter> parameters = this.action.getParameters();
            final SortedSet<String> interpretations = Sets.newTreeSet();
            for (ObjectActionParameter param : parameters) {
                final ActionParameterAutoCompleteFacet facet = param.getFacet(ActionParameterAutoCompleteFacet.class);
                addIfNotEmpty(interpretFacet(facet), interpretations);
            }
            return !interpretations.isEmpty()? Joiner.on(";").join(interpretations) : "";
        }
    }
    @Override public String getDefault() {
        if(memberType == MemberType.PROPERTY) {
            return interpretRowAndFacet(PropertyDefaultFacet.class);
        } else if(memberType == MemberType.COLLECTION) {
            return "";
        } else {
            final List<ObjectActionParameter> parameters = this.action.getParameters();
            final SortedSet<String> interpretations = Sets.newTreeSet();
            for (ObjectActionParameter param : parameters) {
                final ActionParameterDefaultsFacet facet = param.getFacet(ActionParameterDefaultsFacet.class);
                addIfNotEmpty(interpretFacet(facet), interpretations);
            }
            return !interpretations.isEmpty()? Joiner.on(";").join(interpretations) : interpretRowAndFacet(ActionDefaultsFacet.class);
        }
    }
    @Override public String getValidate() {
        if(memberType == MemberType.PROPERTY) {
            return interpretRowAndFacet(PropertyValidateFacet.class);
        } else if(memberType == MemberType.COLLECTION) {
            final SortedSet<String> interpretations = Sets.newTreeSet();
            addIfNotEmpty(interpretRowAndFacet(CollectionValidateAddToFacet.class), interpretations);
            addIfNotEmpty(interpretRowAndFacet(CollectionValidateRemoveFromFacet.class), interpretations);
            return !interpretations.isEmpty()? Joiner.on(";").join(interpretations) : "";
        } else {
            return interpretRowAndFacet(ActionValidationFacet.class);
        }
    }


    private String interpretRowAndFacet(Class<? extends Facet> facetClass) {
        final Facet facet = member.getFacet(facetClass);
        return interpretFacet(facet);
    }

    private static void addIfNotEmpty(final String str, final SortedSet<String> set) {
        if(!Strings.isNullOrEmpty(str)) {
            set.add(str);
        }
    }

    private String interpret(final Class<? extends Facet> cls) {
        return interpretFacet(member.getFacet(cls));
    }

    private static String interpretFacet(final Facet facet) {
        if (facet == null || facet.isNoop()) {
            return "";
        }
        if (facet instanceof ImperativeFacet) {
            ImperativeFacet imperativeFacet = (ImperativeFacet) facet;
            return imperativeFacet.getMethods().get(0).getName();
        }
        final String name = facet.getClass().getSimpleName();
        if (ignore(name)) {
            return "";
        }
        final String abbr = StringExtensions.toAbbreviation(name);
        return abbr.length()>0 ? abbr : name;
    }

    protected static boolean ignore(final String name) {
        return Arrays.asList("PropertyValidateFacetDefault","PropertyDefaultFacetDerivedFromDefaultedFacet").contains(name);
    }

    @Override
    public int compareTo(DomainMember o) {
        // legacy of ObjectContracts.compare(this, o, "classType,className,type desc,memberName");
        return Comparator.comparing(DomainMember::getClassType)
                .thenComparing(DomainMember::getClassName)
                .thenComparing(DomainMember::getType, Comparator.reverseOrder()) // desc
                .thenComparing(DomainMember::getMemberName)
                .compare(this, o);
    }
}