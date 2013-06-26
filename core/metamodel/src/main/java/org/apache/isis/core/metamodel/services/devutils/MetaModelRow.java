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
package org.apache.isis.core.metamodel.services.devutils;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.actions.validate.ActionValidationFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateAddToFacet;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateRemoveFromFacet;
import org.apache.isis.core.progmodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacet;

public class MetaModelRow implements Comparable<MetaModelRow>{

    enum MemberType {
        PROPERTY {
            @Override
            String getChoices(MetaModelRow metaModelRow) {
                return interpretRowAndFacet(metaModelRow, PropertyChoicesFacet.class);
            }
            @Override
            String getAutoComplete(MetaModelRow metaModelRow) {
                return interpretRowAndFacet(metaModelRow, PropertyAutoCompleteFacet.class);
            }
            @Override
            String getDefault(MetaModelRow metaModelRow) {
                return interpretRowAndFacet(metaModelRow, PropertyDefaultFacet.class);
            }
            @Override
            String getValidate(MetaModelRow metaModelRow) {
                return interpretRowAndFacet(metaModelRow, PropertyValidateFacet.class);
            }
        },
        COLLECTION {
            @Override
            String getChoices(MetaModelRow metaModelRow) {
                return "";
            }
            @Override
            String getAutoComplete(MetaModelRow metaModelRow) {
                return "";
            }
            @Override
            String getDefault(MetaModelRow metaModelRow) {
                return "";
            }
            @Override
            String getValidate(MetaModelRow metaModelRow) {
                final List<String> interpretations = Lists.newArrayList();
                addIfNotEmpty(interpretRowAndFacet(metaModelRow, CollectionValidateAddToFacet.class), interpretations);
                addIfNotEmpty(interpretRowAndFacet(metaModelRow, CollectionValidateRemoveFromFacet.class), interpretations);
                return !interpretations.isEmpty()? Joiner.on(";").join(interpretations) : "";
            }
        },
        ACTION {
            @Override
            String getChoices(MetaModelRow metaModelRow) {
                final List<ObjectActionParameter> parameters = metaModelRow.action.getParameters();
                final List<String> interpretations = Lists.newArrayList();
                for (ObjectActionParameter param : parameters) {
                    final ActionParameterChoicesFacet facet = param.getFacet(ActionParameterChoicesFacet.class);
                    addIfNotEmpty(interpretFacet(facet), interpretations);
                }
                return !interpretations.isEmpty()? Joiner.on(";").join(interpretations) : interpretRowAndFacet(metaModelRow, ActionChoicesFacet.class);
            }
            @Override
            String getAutoComplete(MetaModelRow metaModelRow) {
                final List<ObjectActionParameter> parameters = metaModelRow.action.getParameters();
                final List<String> interpretations = Lists.newArrayList();
                for (ObjectActionParameter param : parameters) {
                    final ActionParameterAutoCompleteFacet facet = param.getFacet(ActionParameterAutoCompleteFacet.class);
                    addIfNotEmpty(interpretFacet(facet), interpretations);
                }
                return !interpretations.isEmpty()? Joiner.on(";").join(interpretations) : "";
            }
            @Override
            String getDefault(MetaModelRow metaModelRow) {
                final List<ObjectActionParameter> parameters = metaModelRow.action.getParameters();
                final List<String> interpretations = Lists.newArrayList();
                for (ObjectActionParameter param : parameters) {
                    final ActionParameterDefaultsFacet facet = param.getFacet(ActionParameterDefaultsFacet.class);
                    addIfNotEmpty(interpretFacet(facet), interpretations);
                }
                return !interpretations.isEmpty()? Joiner.on(";").join(interpretations) : interpretRowAndFacet(metaModelRow, ActionDefaultsFacet.class);
            }
            @Override
            String getValidate(MetaModelRow metaModelRow) {
                return interpretRowAndFacet(metaModelRow, ActionValidationFacet.class);
            }
        };

        private static String interpretRowAndFacet(MetaModelRow metaModelRow, Class<? extends Facet> facetClass) {
            final Facet facet = metaModelRow.member.getFacet(facetClass);
            return interpretFacet(facet);
        }
        
        private static void addIfNotEmpty(final String str, final List<String> list) {
            if(!Strings.isNullOrEmpty(str)) {
                list.add(str);
            }
        }

        abstract String getChoices(MetaModelRow metaModelRow);
        abstract String getAutoComplete(MetaModelRow metaModelRow);
        abstract String getDefault(MetaModelRow metaModelRow);
        abstract String getValidate(MetaModelRow metaModelRow);
    }
    
    private final ObjectSpecification spec;
    private final MemberType memberType;
    private final ObjectMember member;
    private ObjectAction action;
    
    MetaModelRow(ObjectSpecification spec, OneToOneAssociation property) {
        this.spec = spec;
        this.member = property;
        this.memberType = MemberType.PROPERTY;
    }

    MetaModelRow(ObjectSpecification spec, OneToManyAssociation collection) {
        this.spec = spec;
        this.member = collection;
        this.memberType = MemberType.COLLECTION;
    }
    
    MetaModelRow(ObjectSpecification spec, ObjectAction action) {
        this.spec = spec;
        this.member = this.action = action;
        this.memberType = MemberType.ACTION;
    }

    public String getClassType() {
        boolean service = false;
        for(ObjectSpecification subspecs: spec.subclasses()) {
            service = service || subspecs.isService();
        }
        return service || spec.isService() ?"2 Service":spec.isValue()?"3 Value":spec.isParentedOrFreeCollection()?"4 Collection":"1 Object";
    }
    public String getClassName() {
        return spec.getFullIdentifier();
    }
    public String getType() {
        return memberType.name().toLowerCase();
    }
    public String getMemberName() {
        return member.getId();
    }
    public String getNumParams() {
        return action!=null?""+action.getParameterCount():"";
    }
    String getHidden() {
        return interpret(HiddenFacet.class);
    }
    String getDisabled() {
        return interpret(DisabledFacet.class);
    }
    public String getChoices() {
        return memberType.getChoices(this);
    }
    public String getAutoComplete() {
        return memberType.getAutoComplete(this);
    }
    String getDefault() {
        return memberType.getDefault(this);
    }
    String getValidate() {
        return memberType.getValidate(this);
    }

    static Object header() {
        return "classType,className,memberType,memberName,numParams,hidden,disabled,choices,autoComplete,default,validate";
    }
    
    String asTextCsv() {
        return Joiner.on(",").join(
                getClassType(),
                getClassName(),
                getType(),
                getMemberName(),
                getNumParams(),
                getHidden(),
                getDisabled(),
                getChoices(),
                getAutoComplete(),
                getDefault(),
                getValidate());
    }
    
    private String interpret(final Class<? extends Facet> cls) {
        return interpretFacet(member.getFacet(cls));
    }

    private static String interpretFacet(final Facet facet) {
        if (facet == null) {
            return "";
        }
        if (facet instanceof ImperativeFacet) {
            ImperativeFacet imperativeFacet = (ImperativeFacet) facet;
            return imperativeFacet.getMethods().get(0).getName();
        } else {
            return "decl.";
        }
    }

    @Override
    public int compareTo(MetaModelRow o) {
        return ObjectContracts.compare(this, o, "classType,className,type desc,memberName");
    }

}