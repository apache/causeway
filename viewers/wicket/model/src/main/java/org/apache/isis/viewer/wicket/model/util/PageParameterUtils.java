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
package org.apache.isis.viewer.wicket.model.util;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.INamedParameters.NamedPair;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * A helper class for dealing with PageParameters
 */
@UtilityClass
public class PageParameterUtils {

    /**
     * The name of the special request parameter that controls whether the page header/navigation bar
     * should be shown or not
     */
    public static final String ISIS_NO_HEADER_PARAMETER_NAME = "isis.no.header";

    /**
     * The name of the special request parameter that controls whether the page footer
     * should be shown or not
     */
    public static final String ISIS_NO_FOOTER_PARAMETER_NAME = "isis.no.footer";

    /**
     * Creates a new instance of PageParameters that preserves some special request parameters
     * which should propagate in all links created by Isis
     *
     * @return a new PageParameters instance
     */
    public static PageParameters newPageParameters() {
        val newPageParameters = new PageParameters();
        val requestCycle = RequestCycle.get();

        if (requestCycle != null) {
            Optional.ofNullable(PageRequestHandlerTracker.getFirstHandler(requestCycle))
            .map(IPageRequestHandler::getPageParameters)
            .ifPresent(currentPageParameters->{
                final StringValue noHeader = currentPageParameters.get(ISIS_NO_HEADER_PARAMETER_NAME);
                if (!noHeader.isNull()) {
                    newPageParameters.set(ISIS_NO_HEADER_PARAMETER_NAME, noHeader.toString());
                }
                final StringValue noFooter = currentPageParameters.get(ISIS_NO_FOOTER_PARAMETER_NAME);
                if (!noFooter.isNull()) {
                    newPageParameters.set(ISIS_NO_FOOTER_PARAMETER_NAME, noFooter.toString());
                }

            });

        }
        return newPageParameters;
    }

    public static Stream<NamedPair> streamCurrentRequestParameters() {
        return Optional.ofNullable(RequestCycle.get()).stream()
        .map(RequestCycle::getRequest)
        .map(Request::getRequestParameters)
        .flatMap(params->
            params.getParameterNames().stream()
            .map(key->new NamedPair(key, params.getParameterValue(key).toString()))
        );
    }

    // -- FACTORY METHODS FOR PAGE PARAMETERS

    public static PageParameters createPageParametersForBookmark(final Bookmark bookmark) {
        val pageParameters = PageParameterUtils.newPageParameters();
        PageParameterNames.OBJECT_OID.addStringTo(pageParameters, bookmark.stringify());
        return pageParameters;
    }

    /**
     * Factory method for creating {@link PageParameters} to represent an
     * object.
     */
    public static PageParameters createPageParametersForObject(final ManagedObject adapter) {

        val pageParameters = PageParameterUtils.newPageParameters();
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
            final ManagedObject adapter,
            final ObjectAction objectAction,
            final Can<ManagedObject> paramValues) {

        val pageParameters = createPageParameters(adapter, objectAction);

        // capture argument values
        for(val argumentAdapter: paramValues) {
            val encodedArg = encodeArg(argumentAdapter);
            PageParameterNames.ACTION_ARGS.addStringTo(pageParameters, encodedArg);
        }

        return pageParameters;
    }

    public static Optional<Bookmark> toBookmark(final PageParameters pageParameters) {
        val oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        return _Strings.isEmpty(oidStr)
                ? Optional.empty()
                : Optional.of(Bookmark.parseElseFail(oidStr));
    }

    // -- HELPERS

    private static PageParameters createPageParameters(final ManagedObject adapter, final ObjectAction objectAction) {

        val pageParameters = PageParameterUtils.newPageParameters();

        ManagedObjects.stringify(adapter)
        .ifPresent(oidStr->
            PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr));

        val actionScope = objectAction.getScope();
        PageParameterNames.ACTION_TYPE.addEnumTo(pageParameters, actionScope);

        val actionOnTypeSpec = objectAction.getDeclaringType();
        if (actionOnTypeSpec != null) {
            PageParameterNames.ACTION_OWNING_SPEC.addStringTo(pageParameters, actionOnTypeSpec.getFullIdentifier());
        }

        val actionId = determineActionId(objectAction);
        PageParameterNames.ACTION_ID.addStringTo(pageParameters, actionId);

        return pageParameters;
    }

    private static String determineActionId(final ObjectAction objectAction) {
        final Identifier identifier = objectAction.getFeatureIdentifier();
        if (identifier != null) {
            return identifier.getMemberNameAndParameterClassNamesIdentityString();
        }
        // fallback (used for action sets)
        return objectAction.getId();
    }

    private static final String NULL_ARG = "$nullArg$";
    private String encodeArg(final ManagedObject adapter) {
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

    private @Nullable ManagedObject decodeArg(
            final @NonNull MetaModelContext mmc,
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
            return Bookmark.parseUrlEncoded(encoded)
                    .flatMap(mmc::loadObject)
                    .orElse(null);
        } catch (final Exception e) {
            return null;
        }
    }

}
