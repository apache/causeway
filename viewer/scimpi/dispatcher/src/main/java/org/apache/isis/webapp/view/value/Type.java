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


package org.apache.isis.webapp.view.value;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.processor.Request;


public class Type extends AbstractElementProcessor {

    public void process(Request request) {
        RequestContext context = request.getContext();
        String showPlural = request.getOptionalProperty(PLURAL);
        String id = request.getOptionalProperty(OBJECT);
        String objectId = id != null ? id : (String) context.getVariable(RequestContext.RESULT);
        
        ObjectAdapter object = (ObjectAdapter) context.getMappedObjectOrResult(objectId);
        String field = request.getOptionalProperty(FIELD);
        if (field != null) {
            ObjectAssociation objectField = object.getSpecification().getAssociation(field);
            object =  objectField.get(object);
        }
        
        ObjectSpecification specification = object.getSpecification();
        String name = showPlural != null ? specification.getPluralName() : specification.getSingularName();
        
        request.appendHtml(name);
    }

    public String getName() {
        return "type";
    }

}

