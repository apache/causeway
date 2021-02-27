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

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.primitives._Ints;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;

import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PageParameterUtil {
    
    public static ActionModel actionModelFor(IsisAppCommonContext commonContext, PageParameters pageParameters) {
        val entityModel = newEntityModelFrom(commonContext, pageParameters);
        val actionMemento = newActionMementoFrom(commonContext, pageParameters);
        val actionModel = ActionModel.of(entityModel, actionMemento);
        val specLoader = commonContext.getSpecificationLoader();
        setArgumentsIfPossible(specLoader, actionModel, pageParameters);
        setContextArgumentIfPossible(specLoader, actionModel, pageParameters);
        return actionModel;
    }
    
    // -- FACTORY METHODS FOR PAGE PARAMETERS

    /**
     * Factory method for creating {@link PageParameters} to represent an
     * object.
     */
    public static PageParameters createPageParametersForObject(ManagedObject adapter) {

        val pageParameters = PageParametersUtils.newPageParameters();
        val isEntity = ManagedObjects.isIdentifiable(adapter);

        if (isEntity) {
            ManagedObjects.stringify(adapter)
            .ifPresent(oidStr->PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr));
        } else {
            // don't do anything; instead the page should be redirected back to
            // an EntityPage so that the underlying EntityModel that contains
            // the memento for the transient ObjectAdapter can be accessed.
        }
        return pageParameters;
    }
    
    public static PageParameters createPageParametersForAction(
            ManagedObject adapter, 
            ObjectAction objectAction,
            Can<ManagedObject> paramValues) {
        
        val pageParameters = createPageParameters(adapter, objectAction);
        
        // capture argument values
        for(val argumentAdapter: paramValues) {
            val encodedArg = encodeArg(argumentAdapter);
            PageParameterNames.ACTION_ARGS.addStringTo(pageParameters, encodedArg);
        }
        
        return pageParameters;
    }
    
    // -- HELPERS
    
    private static PageParameters createPageParameters(ManagedObject adapter, ObjectAction objectAction) {

        val pageParameters = PageParametersUtils.newPageParameters();

        ManagedObjects.stringify(adapter)
        .ifPresent(oidStr->
            PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr));

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

    private static Optional<ParamNumAndOidString> parseParamContext(PageParameters pageParameters) {
        final String paramContext = PageParameterNames.ACTION_PARAM_CONTEXT.getStringFrom(pageParameters);
        if (paramContext == null) {
            return Optional.empty();
        }
        return parseParamContext(paramContext);
    }

    private static ActionMemento newActionMementoFrom(
            IsisAppCommonContext commonContext,
            PageParameters pageParameters) {

        final ObjectSpecId owningSpec = ObjectSpecId.of(PageParameterNames.ACTION_OWNING_SPEC.getStringFrom(pageParameters));
        final ActionType actionType = PageParameterNames.ACTION_TYPE.getEnumFrom(pageParameters, ActionType.class);
        final String actionNameParms = PageParameterNames.ACTION_ID.getStringFrom(pageParameters);
        return new ActionMemento(owningSpec, actionType, actionNameParms, commonContext.getSpecificationLoader());
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
            return identifier.getMemberNameAndParameterClassNamesIdentityString();
        }
        // fallback (used for action sets)
        return objectAction.getId();
    }
    
    private static EntityModel newEntityModelFrom(
            IsisAppCommonContext commonContext,
            PageParameters pageParameters) {

        val rootOid = oidFor(pageParameters);
        val memento = commonContext.mementoFor(rootOid);
        return EntityModel.ofMemento(commonContext, memento);
    }

    private static RootOid oidFor(final PageParameters pageParameters) {
        final String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        return Oid.unmarshaller().unmarshal(oidStr, RootOid.class);
    }

    private static final String NULL_ARG = "$nullArg$";
    private String encodeArg(ManagedObject adapter) {
        if(adapter == null) {
            return NULL_ARG;
        }

        final ObjectSpecification objSpec = adapter.getSpecification();
        if(objSpec.isEncodeable()) {
            final EncodableFacet encodeable = objSpec.getFacet(EncodableFacet.class);
            return encodeable.toEncodedString(adapter);
        }

        return ManagedObjects.stringify(adapter).orElse(null);
    }
    
    private ManagedObject decodeArg(
            final SpecificationLoader specificationLoader,
            final ObjectSpecification objSpec, 
            final String encoded) {
        if(NULL_ARG.equals(encoded)) {
            return null;
        }

        if(objSpec.isEncodeable()) {
            final EncodableFacet encodeable = objSpec.getFacet(EncodableFacet.class);
            return encodeable.fromEncodedString(encoded);
        }

        try {
            val rootOid = RootOid.deStringEncoded(encoded);
            return rootOid.loadObject(specificationLoader);
        } catch (final Exception e) {
            return null;
        }
    }
    
    private static void setArgumentsIfPossible(
            final SpecificationLoader specLoader, 
            final ActionModel actionModel,
            final PageParameters pageParameters) {

        final List<String> argsAsEncodedOidStrings = PageParameterNames.ACTION_ARGS.getListFrom(pageParameters);

        val action = actionModel.getMetaModel();
        val parameters = action.getParameters();

        for (int paramNum = 0; paramNum < argsAsEncodedOidStrings.size(); paramNum++) {
            val oidStrEncoded = argsAsEncodedOidStrings.get(paramNum);
            parameters.get(paramNum)
            .ifPresent(param->decodeAndSetArgument(specLoader, actionModel, param, oidStrEncoded));
        }
    }
    
    private static boolean setContextArgumentIfPossible(
            final SpecificationLoader specLoader, 
            final ActionModel actionModel, 
            final PageParameters pageParameters) {
        
        val paramNumAndOidString = parseParamContext(pageParameters)
                .orElse(null);
        if(paramNumAndOidString==null) {
            return false;
        }
        
        val action = actionModel.getMetaModel();
        val actionParamIfAny = action.getParameters().get(paramNumAndOidString.getParamNum());
        if(!actionParamIfAny.isPresent()) {
            return false;
        }
        val actionParam = actionParamIfAny.get();

        val oidStrEncoded = paramNumAndOidString.getOidString();
        decodeAndSetArgument(specLoader, actionModel, actionParam, oidStrEncoded);
        return true;
    }
    
    private static void decodeAndSetArgument(
            final SpecificationLoader specLoader, 
            final ActionModel actionModel, 
            final ObjectActionParameter actionParam, 
            final String oidStrEncoded) {
        val paramValue = decodeArg(specLoader, actionParam.getSpecification(), oidStrEncoded);
        actionModel.setParameterValue(actionParam, paramValue);
    }
    
}
