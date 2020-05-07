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
package org.apache.isis.viewer.wicket.model.models;

import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.internal.primitives._Ints;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;

import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass


class PageParameterUtil {
    
    /**
     * Factory method for creating {@link PageParameters}.
     */
    public static PageParameters createPageParameters(ManagedObject adapter, ObjectAction objectAction) {

        val pageParameters = PageParametersUtils.newPageParameters();

        ManagedObject.stringify(adapter)
        .ifPresent(oidStr->
        PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr)
                );

        val actionType = objectAction.getType();
        PageParameterNames.ACTION_TYPE.addEnumTo(pageParameters, actionType);

        val actionOnTypeSpec = objectAction.getOnType();
        if (actionOnTypeSpec != null) {
            PageParameterNames.ACTION_OWNING_SPEC.addStringTo(pageParameters, actionOnTypeSpec.getFullIdentifier());
        }

        val actionId = determineActionId(objectAction);
        PageParameterNames.ACTION_ID.addStringTo(pageParameters, actionId);

        return pageParameters;
    }

    @Value(staticConstructor = "of")
    public static class ParamNumAndOidString {
        int paramNum;
        String oidString;
    }

    public static Optional<ParamNumAndOidString> parseParamContext(PageParameters pageParameters) {
        final String paramContext = PageParameterNames.ACTION_PARAM_CONTEXT.getStringFrom(pageParameters);
        if (paramContext == null) {
            return Optional.empty();
        }
        return parseParamContext(paramContext);
    }
    
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("([^=]+)=(.+)");
    
    static Optional<ParamNumAndOidString> parseParamContext(final String paramContext) {
        val matcher = KEY_VALUE_PATTERN.matcher(paramContext);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        try {

            val intLiteral = matcher.group(1);
            val oidStr = matcher.group(2);

            val parseResult = _Ints.parseInt(intLiteral, 10);
            if(parseResult.isPresent()) {
                val paramNum = parseResult.getAsInt();
                return Optional.of(ParamNumAndOidString.of(paramNum, oidStr));
            }

        } catch (final Exception e) {
            // ignore and fall through
        }

        return Optional.empty();

    }
    
    private static String determineActionId(final ObjectAction objectAction) {
        final Identifier identifier = objectAction.getIdentifier();
        if (identifier != null) {
            return identifier.toNameParmsIdentityString();
        }
        // fallback (used for action sets)
        return objectAction.getId();
    }

    
}
