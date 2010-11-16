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
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public class ElementType extends AbstractElementProcessor {

    public void process(Request request) {
        ObjectAdapter collection;
        String field = request.getOptionalProperty(FIELD);
        RequestContext context = request.getContext();
        if (field != null) {
            String id = request.getRequiredProperty(OBJECT);
            ObjectAdapter object = (ObjectAdapter) context.getMappedObjectOrResult(id);
            ObjectAssociation objectField = object.getSpecification().getAssociation(field);
            if (!objectField.isOneToManyAssociation()) {
                throw new ScimpiException("Field " + objectField.getId() + " is not a collection");
            }
            collection =  objectField.get(object);
        } else {
            String id = request.getOptionalProperty(COLLECTION);
            collection =  context.getMappedObjectOrResult(id);
        }
        
        TypeOfFacet facet =  (TypeOfFacet) collection.getTypeOfFacet();
        String name = facet.valueSpec().getSingularName();
        
        request.appendHtml(name);
    }

    public String getName() {
        return "element-type";
    }

}

