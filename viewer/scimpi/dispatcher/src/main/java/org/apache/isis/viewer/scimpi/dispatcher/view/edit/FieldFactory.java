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


package org.apache.isis.viewer.scimpi.dispatcher.view.edit;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.propparam.maxlength.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.typicallength.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;

public class FieldFactory {
    
    public static void initializeField(
        RequestContext context,
        ObjectAdapter object,
        ObjectFeature param,
        ObjectAdapter[] optionsForParameter,
        boolean isRequired,
        InputField field) {

        field.setLabel(param.getName());
        field.setDescription(param.getDescription());
        if (param instanceof ObjectMember) {
            field.setHelpReference(((ObjectMember) param).getHelp());
        } else {
            HelpFacet helpFacet = param.getFacet(HelpFacet.class);
            String value = helpFacet.value();
            field.setHelpReference(value);
        }
        field.setRequired(isRequired);
        field.setHidden(false);
        
        if (param.getSpecification().getFacet(ParseableFacet.class) != null) {
            final int maxLength = param.getFacet(MaxLengthFacet.class).value();
            field.setMaxLength(maxLength);

            TypicalLengthFacet typicalLengthFacet = param.getFacet(TypicalLengthFacet.class);
            if (typicalLengthFacet.isDerived() && maxLength > 0) {
                field.setWidth(maxLength);
            } else {
                field.setWidth(typicalLengthFacet.value());
            }

            MultiLineFacet multiLineFacet = param.getFacet(MultiLineFacet.class);
            field.setHeight(multiLineFacet.numberOfLines());
            field.setWrapped(!multiLineFacet.preventWrapping());

            // TODO figure out a better way to determine if boolean or a password
            ObjectSpecification spec = param.getSpecification();
            if (spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(boolean.class))
                    || spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(Boolean.class.getName()))) {
                field.setType(InputField.CHECKBOX);
            } else if (spec.getFullIdentifier().endsWith(".Password")) {
                field.setType(InputField.PASSWORD);
            } else {
                field.setType(InputField.TEXT); 
            }

        } else {
            field.setType(InputField.REFERENCE);
        }

        if (optionsForParameter != null) {
            int noOptions = optionsForParameter.length + (field.isRequired() ? 0 : 1);
            String[] optionValues = new String[noOptions];
            String[] optionTitles = new String[noOptions];
            if (!field.isRequired()) {
                optionValues[0] = "null";
                optionTitles[0] = "[not set]";
            }
            for (int j = 0; j < optionsForParameter.length; j++) {
                int i = j + (field.isRequired() ? 0 : 1);
                optionValues[i] = getValue(context, optionsForParameter[j]);
                optionTitles[i] = optionsForParameter[j].titleString();
            }
            field.setOptions(optionTitles, optionValues);
        }
    }

    private static String getValue(RequestContext context, ObjectAdapter field) {
        if (field == null) {
            return "";
        }
        if (field.getSpecification().getFacet(ParseableFacet.class) == null) {
            return context.mapObject(field, Scope.INTERACTION);
        } else {
            return field.getObject().toString();
        }
    }
}

