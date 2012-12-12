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

package org.apache.isis.viewer.scimpi.dispatcher.view.action;

import java.util.List;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.BlockContent;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class ActionContent implements BlockContent {
    private final ObjectAction action;
    private final String[] parameters;
    private int next;

    public ActionContent(final ObjectAction action) {
        this.action = action;
        this.parameters = new String[action.getParameterCount()];
    }

    public void setParameter(final String field, final String value) {
        int index;
        if (field == null) {
            index = next++;
        } else {
            index = Integer.valueOf(field).intValue() - 1;
        }
        if (index < 0 || index >= parameters.length) {
            throw new ScimpiException("Parameter numbers should be between 1 and " + parameters.length + ": " + index);
        }
        parameters[index] = value;
    }

    public ObjectAdapter[] getParameters(final Request request) {
        final ObjectAdapter[] params = new ObjectAdapter[parameters.length];
        final List<ObjectActionParameter> pars = action.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final ObjectSpecification typ = pars.get(i).getSpecification();
            if (typ.getFacet(ParseableFacet.class) != null) {
                final ParseableFacet facet = typ.getFacet(ParseableFacet.class);
                Localization localization = IsisContext.getLocalization(); 
                params[i] = facet.parseTextEntry(null, parameters[i], localization);            
            } else {
                params[i] = request.getContext().getMappedObject(parameters[i]);
            }
        }
        return params;
    }

    public String[] getParameters() {
        return parameters;
    }
}
