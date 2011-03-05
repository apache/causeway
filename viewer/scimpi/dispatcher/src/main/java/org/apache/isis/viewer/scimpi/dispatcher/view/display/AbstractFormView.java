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

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractObjectProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.HelpLink;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedFieldsBlock;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;


public abstract class AbstractFormView extends AbstractObjectProcessor {

    @Override
    public String checkFieldType(ObjectAssociation objectField) {
        return objectField.isOneToOneAssociation() ? null : "is not an object";
    }

    @Override
    public void process(Request request, ObjectAdapter object) {
        String cls = request.getOptionalProperty(CLASS, "form");
        String classString = " class=\"" + cls + "\"";
        String title = request.getOptionalProperty(FORM_TITLE);
        String oddRowClass = request.getOptionalProperty(ODD_ROW_CLASS);
        String evenRowClass = request.getOptionalProperty(EVEN_ROW_CLASS);
        boolean showIcons = request.isRequested(SHOW_ICON, true);

        LinkedFieldsBlock tag = new LinkedFieldsBlock();
        request.setBlockContent(tag);
        request.processUtilCloseTag();

        if (object != null) {
            List<ObjectAssociation> fields = tag.includedFields(object.getSpecification().getAssociations(ObjectAssociationFilters.STATICALLY_VISIBLE_ASSOCIATIONS));
            LinkedObject[] linkFields = tag.linkedFields(fields);
    
            String linkAllView = request.getOptionalProperty(LINK);
            if (linkAllView != null) {
                linkAllView = request.getContext().fullUriPath(linkAllView);
                for (int i = 0; i < linkFields.length; i++) {
                    boolean isObject = fields.get(i).isOneToOneAssociation();
                    boolean isNotParseable = !fields.get(i).getSpecification().containsFacet(ParseableFacet.class);
                    linkFields[i] = isObject && isNotParseable ? new LinkedObject(linkAllView) : null;
                }
            }
            
            if (title == null) {
                title = object.getSpecification().getSingularName();
            } else if( title.equals("")) {
                title = null;
            }
            

            write(request, object, fields, linkFields, classString, title, oddRowClass, evenRowClass, showIcons);
        }
        request.popBlockContent();
    }

    private void write(
            Request request,
            ObjectAdapter object,
            List<ObjectAssociation> fields,
            LinkedObject[] linkFields, String classString, String title, String oddRowClass, String evenRowClass, boolean showIcons) {
        request.appendHtml("<div" + classString + ">");
        if (title != null) {
            request.appendHtml("<div class=\"title\">" + title+ "</div>");
            HelpLink.append(request, object.getSpecification().getDescription(), object.getSpecification().getHelp());
        }
        int row = 1;
        for (int i = 0; i < fields.size(); i++) {
            ObjectAssociation field = fields.get(i);
            if (ignoreField(field)) {
                continue;
            }
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) {
                continue;
            }

            String description = field.getDescription().equals("") ? "" : "title=\"" + field.getDescription() + "\"";
            String cls;
            if (row++ % 2 == 1) {
                cls = " class=\"field " + (oddRowClass == null ? ODD_ROW_CLASS : oddRowClass) + "\"";
            } else {
                cls = " class=\"field " + (evenRowClass == null ? EVEN_ROW_CLASS : evenRowClass) + "\"";
            }
            request.appendHtml("<div " + cls + description + "><span class=\"label\">" + field.getName() + ":</span>");
            LinkedObject linkedObject = linkFields[i];
            addField(request, object, field, linkedObject, showIcons);
            HelpLink.append(request, field.getDescription(), field.getHelp());
            request.appendHtml("</div>");
        }
        request.appendHtml("</div>");
    }

    protected void addField(Request request, ObjectAdapter object, ObjectAssociation field, LinkedObject linkedObject, boolean showIcons) {
        FieldValue.write(request, object, field, linkedObject, "value", showIcons, 0);
    }

    protected boolean ignoreField(ObjectAssociation objectField) {
        return false;
    }

}

