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
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.PageWriter;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedFieldsBlock;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;
import org.apache.isis.viewer.scimpi.dispatcher.view.simple.RemoveElement;


public class TableView extends AbstractTableView {

    private static final class SimpleTableBuilder implements TableContentWriter {
        private final String parent;
        private final boolean includeHeading;
        private final boolean includeFooting;
        private final String title;
        private final String[] headers;
        private final List<ObjectAssociation> fields;
        private final boolean showSelectOption;
        private final boolean showDeleteOption;
        private final boolean showEditOption;
        private final String fieldName;
        private final LinkedObject[] linkedFields;
        private final LinkedObject linkRow;

        private SimpleTableBuilder(
                String parent,
                boolean includeHeading,
                boolean includeFooting,
                String title,
                String[] headers,
                List<ObjectAssociation> fields,
                boolean showSelectOption,
                boolean showDeleteOption,
                boolean showEditOption,
                String fieldName,
                LinkedObject[] linkedFields,
                LinkedObject linkRow) {
            this.parent = parent;
            this.includeHeading = includeHeading;
            this.includeFooting = includeFooting;
            this.title = title;
            this.headers = headers;
            this.fields = fields;
            this.showSelectOption = showSelectOption;
            this.showDeleteOption = showDeleteOption;
            this.showEditOption = showEditOption;
            this.fieldName = fieldName;
            this.linkedFields = linkedFields;
            this.linkRow = linkRow;
        }

        @Override
        public void writeFooters(PageWriter writer) {
            if (includeFooting) {
                writer.appendHtml("<tfooter>");
                headerRow(writer, headers);
                writer.appendHtml("</tfooter>");
            }
        }

        @Override
        public void writeHeaders(PageWriter writer) {
            if (includeHeading) {
                writer.appendHtml("<theader>");
                titleRow(writer);
                headerRow(writer, headers);
                writer.appendHtml("</theader>");
            }
        }

        private void titleRow(final PageWriter writer) {
            if (title != null) {
                writer.appendHtml("<tr colspan=\"" + fields.size() +"\">");
                writer.appendHtml("<th class=\"title\">" + title + "</th>");
                writer.appendHtml("</tr>");
            }
        }

        private void headerRow(final PageWriter writer, final String[] headers) {
            writer.appendHtml("<tr>");
            String[] columnHeaders = headers;
            for (int i = 0; i < columnHeaders.length; i++) {
                if (columnHeaders[i] != null) {
                    writer.appendHtml("<th>" + columnHeaders[i] + "</th>");
                }
            }
            writer.appendHtml("<th>" + "</th>");
            writer.appendHtml("</tr>");
        }

        @Override
        public void writeElement(Request request, RequestContext context, ObjectAdapter element) {
            String rowId = context.mapObject(element, Scope.INTERACTION);
            String scope = linkRow == null ? "" : "&amp;" + SCOPE + "=" + linkRow.getScope();
            String result = ""; // linkRow.getVariable().equals("_result") ? "" : "&amp;" +
                                // RequestContext.RESULT + "=" + context.mapObject(collection,
                                // Scope.REQUEST);
            result = context.encodedInteractionParameters();

            if (fields.size() == 0) {
                if (linkRow != null) {
                    request.appendHtml("<td><a href=\"" + linkRow.getForwardView() + "?" + linkRow.getVariable() + "="
                            + rowId + result + scope + "\">" + element.titleString() + "</a></td>");
                } else {
                    request.appendHtml("<td>" + element.titleString() + "</td>");
                }

            } else {
                for (int i = 0; i < fields.size(); i++) {
                    if (fields.get(i).isOneToManyAssociation()) {
                        continue;
                    }
                    request.appendHtml("<td>");
                    ObjectAdapter field = fields.get(i).get(element);
                    if (field != null) {
                        if (!fields.get(i).getSpecification().containsFacet(ParseableFacet.class)) {
                            request.appendHtml("<img class=\"" + "small-icon" + "\" src=\""
                                    + request.getContext().imagePath(field) + "\" alt=\""
                                    + fields.get(i).getSpecification().getShortIdentifier() + "\"/>");
                        }
                        if (linkRow != null) {
                            request.appendHtml("<a href=\"" + linkRow.getForwardView() + "?" + linkRow.getVariable() + "="
                                    + rowId + result + scope + "\">");
                        } else if (linkedFields[i] != null) {
                            ObjectAdapter fieldObject = fields.get(i).get(element);
                            String id = context.mapObject(fieldObject, Scope.INTERACTION);
                            request.appendHtml("<a href=\"" + linkedFields[i].getForwardView() + "?"
                                    + linkedFields[i].getVariable() + "=" + id + "\">");
                            context.mapObject(fieldObject, RequestContext.scope(linkedFields[i].getScope()));

                        }
                        request.appendHtml(field.titleString());
                        if (linkRow != null || linkedFields[i] != null) {
                            request.appendHtml("</a>");
                        }
                    }
                    request.appendHtml("</td>");

                }
            }
            request.appendHtml("<td>");
            if (showSelectOption) {
                request.appendHtml("<a class=\"element-select\" href=\"" + "_generic." + Dispatcher.EXTENSION + "?"
                        + RequestContext.RESULT + "=" + rowId + result + scope + "\">view</a>");
            }
            if (showEditOption) {
                request.appendHtml(" <a class=\"element-edit\" href=\"" + "_generic_edit." + Dispatcher.EXTENSION + "?"
                        + RequestContext.RESULT + "=" + rowId + result + scope + "\">edit</a>");
            }

            if (showDeleteOption && parent != null) {
                String view = request.getViewPath();
                view = context.fullFilePath(view == null ? context.getResourceFile() : view);
                RemoveElement.write(request, context.getMappedObject(parent), fieldName, element, null, view, view, "delete", "element-delete");
            }

            request.appendHtml("</td>");

        }
    }

    @Override
    protected TableContentWriter createRowBuilder(
            final Request request,
            RequestContext context,
            final String parent,
            final List<ObjectAssociation> allFields, ObjectAdapter collection) {
        final String fieldName = request.getOptionalProperty(FIELD);
        return rowBuilder(request, context, parent, fieldName, allFields);
    }

    private static TableContentWriter rowBuilder(
            final Request request,
            RequestContext context,
            final String object,
            String fieldName, 
            List<ObjectAssociation> allFields) {
        String linkRowView = request.getOptionalProperty(LINK);
        String linkObjectName = request.getOptionalProperty(ELEMENT_NAME, RequestContext.RESULT);
        String linkObjectScope = request.getOptionalProperty(SCOPE, Scope.INTERACTION.toString());
        final LinkedObject linkRow = linkRowView == null ? null : new LinkedObject(linkObjectName, linkObjectScope,
                context.fullUriPath(linkRowView));
        final boolean includeHeading = request.isRequested(HEADING, true);
        final boolean includeFooting = request.isRequested(FOOTING, false);
        final String title = request.getOptionalProperty(FORM_TITLE);
        
        boolean linkFields = request.isRequested("link-fields", true);
        final boolean showSelectOption = request.isRequested(SHOW_SELECT, true);
        final boolean showEditOption = request.isRequested(SHOW_EDIT, true);
        final boolean showDeleteOption = request.isRequested(SHOW_DELETE, true);

        LinkedFieldsBlock block = new LinkedFieldsBlock();
        request.setBlockContent(block);
        request.processUtilCloseTag();
        final List<ObjectAssociation> fields = block.includedFields(allFields);
        final LinkedObject[] linkedFields = block.linkedFields(fields);
        for (int i = 0; i < linkedFields.length; i++) {
            if (linkedFields[i] == null && linkFields && !fields.get(i).getSpecification().containsFacet(ParseableFacet.class)) {
                linkedFields[i] = new LinkedObject("_generic.shtml");
            }
            if (linkedFields[i] != null) {
                linkedFields[i].setForwardView(context.fullUriPath(linkedFields[i].getForwardView()));
            }
        }

        final String headers[] = new String[fields.size()];
        int h = 0;
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).isOneToManyAssociation()) {
                continue;
            }
            headers[h++] = fields.get(i).getName();
        }

        request.popBlockContent();
        
        return new SimpleTableBuilder(object, includeHeading, includeFooting, title, headers, fields, showSelectOption, showDeleteOption,
                showEditOption, fieldName, linkedFields, linkRow);
    }

    public static void write(
            Request request,
            String summary,
            ObjectAdapter object,
            ObjectAssociation field,
            ObjectAdapter collection,
            List<ObjectAssociation> fields,
            boolean linkAllFields) {
        boolean[] linkFields = new boolean[fields.size()];
        if (linkAllFields) {
            for (int i = 0; i < linkFields.length; i++) {
                linkFields[i] = fields.get(i).isOneToOneAssociation();
            }
        }
        RequestContext context = request.getContext();
        TableContentWriter rowBuilder = rowBuilder(request, context, context.mapObject(object, Scope.REQUEST), field.getId(), fields);
        write(request, collection, summary, rowBuilder, null);
    }

    @Override
    public String getName() {
        return "table";
    }

}

