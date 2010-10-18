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

import java.util.Iterator;

import org.apache.isis.commons.exceptions.UnknownTypeException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.webapp.AbstractElementProcessor;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.context.RequestContext.Scope;
import org.apache.isis.webapp.processor.Request;
import org.apache.isis.webapp.util.MethodsUtils;
import org.apache.isis.webapp.view.action.ActionForm;
import org.apache.isis.webapp.view.action.CreateFormParameter;


public class Selector extends AbstractElementProcessor {

    public void process(Request request) {
        EditFieldBlock block = (EditFieldBlock) request.getBlockContent();
        String field = request.getRequiredProperty(FIELD);
        if (block.isVisible(field)) {
            processElement(request, block, field);
        }
        request.skipUntilClose();
    }

    private void processElement(Request request, EditFieldBlock block, String field) {
        String type = request.getOptionalProperty(TYPE, "dropdown");
        if (!request.isPropertySpecified(METHOD) && request.isPropertySpecified(COLLECTION)) {
            String id = request.getRequiredProperty(COLLECTION, Request.NO_VARIABLE_CHECKING);
            String selector = showSelectionList(request, id, type);
            block.replaceContent(field, selector);
        } else {
            String objectId = request.getOptionalProperty(OBJECT);
            String methodName = request.getRequiredProperty(METHOD);
            ObjectAdapter object = MethodsUtils.findObject(request.getContext(), objectId);
            ObjectAction action = MethodsUtils.findAction(object, methodName);
            if (action.getParameterCount() == 0) {
                ObjectAdapter collection = action.execute(object, new ObjectAdapter[0]);
                String selector = showSelectionList(request, collection, type);
                block.replaceContent(field, selector);
            } else {
                String id = "selector_options";
                String id2 = (String) request.getContext().getVariable(id);
                String selector = showSelectionList(request, id2, type);
    
                CreateFormParameter parameters = new CreateFormParameter();
                parameters.objectId = objectId;
                parameters.methodName = methodName;
                parameters.buttonTitle = request.getOptionalProperty(TITLE, "Search");
                parameters.legend = request.getOptionalProperty(LEGEND);
                parameters.className = request.getOptionalProperty(CLASS, "selector");
                parameters.id = request.getOptionalProperty(ID);
    
                parameters.resultName = id;
                parameters.forwardResultTo = request.getContext().getResourceFile();
                parameters.forwardVoidTo = "error";
                parameters.forwardErrorTo = parameters.forwardResultTo;
                parameters.scope = Scope.REQUEST.name();
                request.pushNewBuffer();
                ActionForm.createForm(request, parameters);
                block.replaceContent(field, selector);
    
                request.appendHtml(request.popBuffer());
            }
        }
    }

    private String showSelectionList(Request request, String collectionId, String type) {
        if (collectionId != null && !collectionId.equals("")) {
            ObjectAdapter collection = request.getContext().getMappedObjectOrResult(collectionId);
            return showSelectionList(request, collection, type);
       } else {
            return null;
        }
    }
        
    private String showSelectionList(Request request,  ObjectAdapter collection, String type) {
            String field = request.getRequiredProperty(FIELD);
            CollectionFacet facet = (CollectionFacet) collection.getSpecification().getFacet(CollectionFacet.class);
            
            if (facet.size(collection) == 1) {
                return onlyItem(request, field, collection, facet);
            } else if (type.equals("radio")) {
                return radioButtonList(request, field, collection, facet);
            } else if (type.equals("list")) {
                String size = request.getOptionalProperty("size", "5");
                return dropdownList(request, field, collection, size, facet);
            } else if (type.equals("dropdown")) {
                return dropdownList(request, field, collection, null, facet);
            } else {
                throw new UnknownTypeException(type);
            }
    }

    private String onlyItem(Request request, String field, ObjectAdapter collection, CollectionFacet facet) {
        RequestContext context = request.getContext();
        Iterator<ObjectAdapter> iterator = facet.iterator(collection);
        StringBuffer buffer = new StringBuffer();
        ObjectAdapter element = iterator.next();
        String elementId = context.mapObject(element, Scope.INTERACTION);
        buffer.append("<img class=\"small-icon\" src=\"" + request.getContext().imagePath(element)
                + "\" alt=\"" + element.getSpecification().getShortName() + "\"/>" + element.titleString() + "\n");
        buffer.append("<input type=\"hidden\" name=\"" + field + "\" value=\"" + elementId + "\"" + "/>\n");
        return buffer.toString();
    }

    private String radioButtonList(Request request, String field, ObjectAdapter collection, CollectionFacet facet) {
        RequestContext context = request.getContext();
        Iterator<ObjectAdapter> iterator = facet.iterator(collection);
        StringBuffer buffer = new StringBuffer();
        while (iterator.hasNext()) {
            ObjectAdapter element = iterator.next();
            String elementId = context.mapObject(element, Scope.INTERACTION);
            String title = element.titleString();
            String checked = "";
            buffer.append("<input type=\"radio\" name=\"" + field + "\" value=\"" + elementId + "\"" + checked + ">" + title
                    + "</input><br/>\n");
        }

        return buffer.toString();
    }

    private String dropdownList(Request request, String field, ObjectAdapter collection, String size, CollectionFacet facet) {
        RequestContext context = request.getContext();
        Iterator<ObjectAdapter> iterator = facet.iterator(collection);
        StringBuffer buffer = new StringBuffer();
        size = size == null ? "" : " size =\"" + size + "\"";
        buffer.append("<select name=\"" + field + "\"" + size + " >\n");
        while (iterator.hasNext()) {
            ObjectAdapter element = iterator.next();
            String elementId = context.mapObject(element, Scope.INTERACTION);
            String title = element.titleString();
            String checked = "";
            buffer.append("  <option value=\"" + elementId + "\"" + checked + ">" + title + "</option>\n");
        }
        buffer.append("</select>\n");
        return buffer.toString();
    }

    public String getName() {
        return "selector";
    }

}

