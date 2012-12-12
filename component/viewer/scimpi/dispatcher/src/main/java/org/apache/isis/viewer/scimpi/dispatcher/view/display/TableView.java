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
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.PageWriter;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedFieldsBlock;
import org.apache.isis.viewer.scimpi.dispatcher.view.field.LinkedObject;
import org.apache.isis.viewer.scimpi.dispatcher.view.simple.RemoveElement;

public class TableView extends AbstractTableView {

   static final class SimpleTableBuilder implements TableContentWriter {
        private final String parent;
        private final boolean includeHeader;
        private final boolean includeFooter;
        private final String title;
        private final String[] headers;
        private final List<ObjectAssociation> fields;
        private final boolean showTitle;
        private final boolean showIcons;
        private final boolean showSelectOption;
        private final boolean showDeleteOption;
        private final boolean showEditOption;
        private final String fieldName;
        private final LinkedObject[] linkedFields;
        private final LinkedObject linkRow;
        private final int noColumns;

        SimpleTableBuilder(
                final String parent,
                final boolean includeHeader,
                final boolean includeFooter,
                final String title,
                final int noColumns,
                final String[] headers,
                final List<ObjectAssociation> fields,
                final boolean showTitle,
                final boolean showIcons,
                final boolean showSelectOption,
                final boolean showDeleteOption,
                final boolean showEditOption,
                final String fieldName,
                final LinkedObject[] linkedFields,
                final LinkedObject linkRow) {
            this.parent = parent;
            this.includeHeader = includeHeader;
            this.includeFooter = includeFooter;
            this.title = title;
            this.showTitle = showTitle;
            this.noColumns = noColumns < 1 ? fields.size() : noColumns;
            this.headers = headers;
            this.fields = fields;
            this.showIcons = showIcons;
            this.showSelectOption = showSelectOption;
            this.showDeleteOption = showDeleteOption;
            this.showEditOption = showEditOption;
            this.fieldName = fieldName;
            this.linkedFields = linkedFields;
            this.linkRow = linkRow;
        }

        @Override
        public void writeFooters(final PageWriter writer) {
            if (includeFooter) {
                writer.appendHtml("<tfoot>");
                columnHeaders(writer, headers);
                writer.appendHtml("</tfoot>");
            }
        }

        @Override
        public void writeCaption(PageWriter writer) {
            if (title != null) {
                writer.appendHtml("<caption>");
                writer.appendHtml(title);
                writer.appendHtml("</caption>");
            }
        }
        
        @Override
        public void writeHeaders(final PageWriter writer) {
            if (includeHeader) {
                writer.appendHtml("<thead>");
                columnHeaders(writer, headers);
                writer.appendHtml("</thead>");
            }
        }

        private void columnHeaders(final PageWriter writer, final String[] headers) {
            writer.appendHtml("<tr class=\"column-headers\">");
            if (showTitle) {
                writer.appendHtml("<th></th>");
            }
            final String[] columnHeaders = headers;
            for (final String columnHeader : columnHeaders) {
                if (columnHeader != null) {
                    writer.appendHtml("<th>");
                    writer.appendAsHtmlEncoded(columnHeader);
                    writer.appendHtml("</th>");
                }
            }
            writer.appendHtml("<th class=\"controls\"></th>");
            writer.appendHtml("</tr>");
        }

        public void tidyUp() {
       //     request.popBlockContent();
            
        //    Is it the block that is left over, or is the collection form not being closed?
        }
        
        @Override
        public void writeElement(final Request request, final RequestContext context, final ObjectAdapter element) {
            final String rowId = context.mapObject(element, Scope.INTERACTION);
            final String scope = linkRow == null ? "" : "&amp;" + SCOPE + "=" + linkRow.getScope();
            String result = "";
            result = context.encodedInteractionParameters();

            if (noColumns == 0) {
                request.appendHtml("<td>");
                if (linkRow != null) {
                    request.appendHtml("<td><a href=\"" + linkRow.getForwardView() + "?" + linkRow.getVariable() + "=" + rowId + result + scope + "\">");
                    request.appendAsHtmlEncoded(element.titleString());
                    request.appendHtml("</a>");
                } else {
                    request.appendAsHtmlEncoded(element.titleString());
                }
                request.appendHtml("</td>");

            } else {
                if (showTitle) {
                    request.appendHtml("<td>");
                    request.appendAsHtmlEncoded(element.titleString());
                    request.appendHtml("</td>");
                }

                for (int i = 0; i < noColumns; i++) {
                    if (fields.get(i).isOneToManyAssociation()) {
                        continue;
                    }
                    request.appendHtml("<td>");
                    final ObjectAdapter field = fields.get(i).get(element);
                    if (field != null) {
                        if (showIcons && !fields.get(i).getSpecification().containsFacet(ParseableFacet.class)) {
                            request.appendHtml("<img class=\"" + "small-icon" + "\" src=\"" + request.getContext().imagePath(field) + "\" alt=\"" + fields.get(i).getSpecification().getShortIdentifier() + "\"/>");
                        }
                        if (linkRow != null) {
                            request.appendHtml("<a href=\"" + linkRow.getForwardView() + "?" + linkRow.getVariable() + "=" + rowId + result + scope + "\">");
                        } else if (linkedFields[i] != null) {
                            final ObjectAdapter fieldObject = fields.get(i).get(element);
                            final String id = context.mapObject(fieldObject, Scope.INTERACTION);
                            request.appendHtml("<a href=\"" + linkedFields[i].getForwardView() + "?" + linkedFields[i].getVariable() + "=" + id + "\">");
                            context.mapObject(fieldObject, RequestContext.scope(linkedFields[i].getScope()));

                        }
                        try {
                            request.appendAsHtmlEncoded(field.titleString());
                        } catch (final ObjectNotFoundException e) {
                            request.appendAsHtmlEncoded(e.getMessage());
                        }
                        if (linkRow != null || linkedFields[i] != null) {
                            request.appendHtml("</a>");
                        }
                    }
                    request.appendHtml("</td>");

                }
            }
            request.appendHtml("<td class=\"controls\">");
            if (showSelectOption) {
                request.appendHtml("<a class=\"button element-select\" href=\"" + "_generic." + Dispatcher.EXTENSION + "?" + RequestContext.RESULT + "=" + rowId + result + scope + "\">view</a>");
            }
            if (showEditOption) {
                request.appendHtml(" <a class=\"button element-edit\" href=\"" + "_generic_edit." + Dispatcher.EXTENSION + "?" + RequestContext.RESULT + "=" + rowId + result + scope + "\">edit</a>");
            }

            if (showDeleteOption && parent != null) {
                String view = request.getViewPath();
                view = context.fullFilePath(view == null ? context.getResourceFile() : view);
                RemoveElement.write(request, context.getMappedObject(parent), fieldName, element, null, view, view, "delete", "action in-line element-delete confirm");
            }

            request.appendHtml("</td>");

        }
    }

    @Override
    protected TableContentWriter createRowBuilder(
            final Request request,
            final RequestContext context,
            final String parent,
            final List<ObjectAssociation> allFields,
            final ObjectAdapter collection) {
        final String fieldName = request.getOptionalProperty(FIELD);
        final String title = request.getOptionalProperty(FORM_TITLE);
        return rowBuilder(request, context, title, parent, fieldName, allFields, showIconByDefault());
    }

    private static TableContentWriter rowBuilder(
            final Request request,
            final RequestContext context,
            final String title,
            final String object,
            final String fieldName,
            final List<ObjectAssociation> allFields,
            final boolean showIconByDefault) {
        final String linkRowView = request.getOptionalProperty(LINK_VIEW);
        final String linkObjectName = request.getOptionalProperty(ELEMENT_NAME, RequestContext.RESULT);
        final String linkObjectScope = request.getOptionalProperty(SCOPE, Scope.INTERACTION.toString());
        final LinkedObject linkRow = linkRowView == null ? null : new LinkedObject(linkObjectName, linkObjectScope, context.fullUriPath(linkRowView));
        final boolean includeHeader = request.isRequested(HEADER, true);
        final boolean includeFooter = request.isRequested(FOOTER, false);

        final boolean linkFields = request.isRequested("link-fields", true);
        final boolean showTitle = request.isRequested(SHOW_TITLE, false);
        final boolean showIcons = request.isRequested(SHOW_ICON, showIconByDefault);
        final boolean showSelectOption = request.isRequested(SHOW_SELECT, true);
        final boolean showEditOption = request.isRequested(SHOW_EDIT, true);
        final boolean showDeleteOption = request.isRequested(SHOW_DELETE, true);

        final String noColumnsString = request.getOptionalProperty("no-columns", "3");

        final LinkedFieldsBlock block = new LinkedFieldsBlock();
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

        int noColumns;
        if (noColumnsString.equalsIgnoreCase("all")) {
            noColumns = fields.size();
        } else {
            noColumns = Math.min(fields.size(), Integer.valueOf(noColumnsString));
        }

        final String headers[] = new String[noColumns];
        int h = 0;
        for (int i = 0; i < noColumns; i++) {
            if (fields.get(i).isOneToManyAssociation()) {
                continue;
            }
            headers[h++] = fields.get(i).getName();
        }

        request.popBlockContent();

        return new SimpleTableBuilder(object, includeHeader, includeFooter, title, noColumns, headers, fields, showTitle,
                showIcons, showSelectOption, showDeleteOption, showEditOption, fieldName, linkedFields, linkRow);
    }

    public static void write(
            final Request request,
            final String summary,
            final ObjectAdapter object,
            final ObjectAssociation field,
            final ObjectAdapter collection,
            final int noColumns,
            final List<ObjectAssociation> fields,
            final boolean linkAllFields,
            final boolean showIconByDefault,
            final String tableClass,
            final String[] rowClasses,
            LinkedObject linkedObject) {
        final LinkedObject[] linkedFields = new LinkedObject[fields.size()];
        if (linkAllFields) {
            for (int i = 0; i < linkedFields.length; i++) {
                if (fields.get(i).isOneToOneAssociation()) {
                    linkedFields[i] = linkedObject == null ? new LinkedObject("_generic.shtml") : linkedObject;  
                }
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
        
        final RequestContext context = request.getContext();
        final TableContentWriter rowBuilder = rowBuilder(request, context, null, context.mapObject(object, Scope.REQUEST), field.getIdentifier().getMemberName(), fields, 
                showIconByDefault);
        write(request, collection, summary, rowBuilder, null, null, null);
    }

    @Override
    public String getName() {
        return "table";
    }

}
