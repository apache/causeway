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


package org.apache.isis.webapp.view.edit;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.propparam.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.propparam.typicallength.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.propparam.validate.maxlength.MaxLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.context.RequestContext.Scope;
import org.apache.isis.webapp.view.form.InputField;

public class FieldFactory {
    
    public static void initializeField(
        RequestContext context,
        ObjectAdapter object,
        ObjectFeature param,
        ObjectAdapter[] optionsForParameter,
        boolean isOptional,
        boolean includeUnusableFields,
        InputField field) {

        field.setLabel(param.getName());
        field.setDescription(param.getDescription());
        field.setRequired(!isOptional);
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
            } else if (spec.getFullName().endsWith(".Password")) {
                field.setType(InputField.PASSWORD);
            } else {
                field.setType(InputField.TEXT);
            }

        } else {
            field.setType(InputField.REFERENCE);
        }

        if (optionsForParameter != null) {
            int noOptions = optionsForParameter.length;
            String[] optionValues = new String[noOptions];
            String[] optionTitles = new String[noOptions];
            for (int j = 0; j < noOptions; j++) {
                optionValues[j] = getValue(context, optionsForParameter[j]);
                optionTitles[j] = optionsForParameter[j].titleString();
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
            return field.titleString();
        }
    }
}

