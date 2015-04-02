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

package org.apache.isis.viewer.scimpi.dispatcher.view.value;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class Type extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        final RequestContext context = request.getContext();
        final String showPlural = request.getOptionalProperty(PLURAL);
        final String id = request.getOptionalProperty(OBJECT);
        final String objectId = id != null ? id : (String) context.getVariable(RequestContext.RESULT);

        ObjectAdapter object = context.getMappedObjectOrResult(objectId);
        final String field = request.getOptionalProperty(FIELD);
        if (field != null) {
            final ObjectAssociation objectField = object.getSpecification().getAssociation(field);
            object = objectField.get(object);
        }
        request.appendDebug(" for " + object);

        final ObjectSpecification specification = object.getSpecification();
        final String name = showPlural != null ? specification.getPluralName() : specification.getSingularName();

        request.appendAsHtmlEncoded(name);
    }

    @Override
    public String getName() {
        return "type";
    }

}
