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

package org.apache.isis.viewer.scimpi.dispatcher.view.debug;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractObjectProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.HelpLink;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;

public class DebugCollectionView extends AbstractObjectProcessor {

    @Override
    public void process(final Request request, final ObjectAdapter object) {
        final String cls = request.getOptionalProperty(CLASS, "form");
        final String classString = " class=\"" + cls + "\"";
        String title = request.getOptionalProperty(FORM_TITLE);
        final String oddRowClass = request.getOptionalProperty(ODD_ROW_CLASS);
        final String evenRowClass = request.getOptionalProperty(EVEN_ROW_CLASS);
        final boolean showIcons = request.isRequested(SHOW_ICON, true);

    }

    private void write(final Request request, final ObjectAdapter object, final List<ObjectAssociation> fields,
        final LinkedObject[] linkFields, final String classString, final String title, final String oddRowClass,
        final String evenRowClass, final boolean showIcons) {
        request.appendHtml("<div" + classString + ">");
        if (title != null) {
            request.appendHtml("<div class=\"title\">");
            request.appendAsHtmlEncoded(title);
            request.appendHtml("</div>");
            HelpLink.append(request, object.getSpecification().getDescription(), object.getSpecification().getHelp());
        }
     /*   
        final List<ObjectAssociation> fields =
                tag.includedFields(object.getSpecification().getAssociations(
                    ObjectAssociationFilters.STATICALLY_VISIBLE_ASSOCIATIONS));
            final LinkedObject[] linkFields = tag.linkedFields(fields);

            String linkAllView = request.getOptionalProperty(LINK);
            if (linkAllView != null) {
                linkAllView = request.getContext().fullUriPath(linkAllView);
                for (int i = 0; i < linkFields.length; i++) {
                    final boolean isObject = fields.get(i).isOneToOneAssociation();
                    final boolean isNotParseable =
                        !fields.get(i).getSpecification().containsFacet(ParseableFacet.class);
                    linkFields[i] = isObject && isNotParseable ? new LinkedObject(linkAllView) : null;
                }
            }

        
        
        int row = 1;
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            if (ignoreField(field)) {
                continue;
            }
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) {
                continue;
            }

            final String description =
                field.getDescription().equals("") ? "" : "title=\"" + field.getDescription() + "\"";
            String cls;
            if (row++ % 2 == 1) {
                cls = " class=\"field " + (oddRowClass == null ? ODD_ROW_CLASS : oddRowClass) + "\"";
            } else {
                cls = " class=\"field " + (evenRowClass == null ? EVEN_ROW_CLASS : evenRowClass) + "\"";
            }
            request.appendHtml("<div " + cls + description + "><span class=\"label\">");
            request.appendAsHtmlEncoded(field.getName());
            request.appendHtml(":</span>");
            final LinkedObject linkedObject = linkFields[i];
            addField(request, object, field, linkedObject, showIcons);
            HelpLink.append(request, field.getDescription(), field.getHelp());
            request.appendHtml("</div>");
        }
        */
        request.appendHtml("</div>");
        
    }


    @Override
    public String getName() {
        return "debug-collection";
    }

}
