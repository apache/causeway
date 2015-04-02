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

package org.apache.isis.viewer.scimpi.dispatcher.view.display;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.facets.value.date.DateValueFacet;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class GetField extends AbstractElementProcessor {

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with 
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly) 
    // for any other value for Where
    private final Where where = Where.ANYWHERE;

    @Override
    public void process(final Request request) {
        final String id = request.getOptionalProperty(OBJECT);
        final String fieldName = request.getRequiredProperty(FIELD);
        final ObjectAdapter object = request.getContext().getMappedObjectOrResult(id);
        if (object == null) {
            throw new ScimpiException("No object to get field for: " + fieldName + " - " + id);
        }
        final ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
        if (field == null) {
            throw new ScimpiException("No field " + fieldName + " in " + object.getSpecification().getFullIdentifier());
        }
        final AuthenticationSession session = IsisContext.getAuthenticationSession();
        if (field.isVisible(session, object, where).isVetoed()) {
            throw new ForbiddenException(field, ForbiddenException.VISIBLE);
        }

        String pattern = request.getOptionalProperty("decimal-format");
        Format format = null;
        if (pattern != null) {
            format = new DecimalFormat(pattern);
        }
        pattern = request.getOptionalProperty("date-format");
        if (pattern != null) {
            format = new SimpleDateFormat(pattern);
        }

        final String name = request.getOptionalProperty(RESULT_NAME, fieldName);
        final String scopeName = request.getOptionalProperty(SCOPE);
        final Scope scope = RequestContext.scope(scopeName, Scope.REQUEST);

        process(request, object, field, format, name, scope);
    }

    protected void process(final Request request, final ObjectAdapter object, final ObjectAssociation field, final Format format, final String name, final Scope scope) {
        final ObjectAdapter fieldReference = field.get(object);
        if (format != null && fieldReference.isValue()) {
            final DateValueFacet facet = fieldReference.getSpecification().getFacet(DateValueFacet.class);
            final Date date = facet.dateValue(fieldReference);
            final String value = format.format(date);
            request.appendDebug("    " + object + " -> " + value);
            request.getContext().addVariable(name, Request.getEncoder().encoder(value), scope);
        } else {
            final String source = fieldReference == null ? "" : request.getContext().mapObject(fieldReference, scope);
            request.appendDebug("    " + object + " -> " + source);
            request.getContext().addVariable(name, source, scope);
        }
    }

    @Override
    public String getName() {
        return "get-field";
    }

}
