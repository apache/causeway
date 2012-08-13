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
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.PageWriter;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request.RepeatMarker;

public class TableBuilder extends AbstractTableView {

    @Override
    protected TableContentWriter createRowBuilder(final Request request, final RequestContext context, final String parent, final List<ObjectAssociation> allFields, final ObjectAdapter collection) {

        final String title = request.getOptionalProperty(TABLE_TITLE);
        final String variable = request.getOptionalProperty(ELEMENT_NAME, ELEMENT);
        final String headerClass = request.getOptionalProperty("head-" + CLASS);

        final TableBlock block = new TableBlock();
        block.setCollection(collection);
        block.setElementName(variable);
        request.setBlockContent(block);
        request.pushNewBuffer();
        request.processUtilCloseTag();
        final String headers = request.popBuffer();       
        return new TableContentWriter() {

            @Override
            public void writeFooters(final PageWriter writer) {
            }

            public void tidyUp() {
                request.popBlockContent();
            }
            
            @Override
            public void writeCaption(PageWriter writer) {
                if (title != null) {
                    writer.appendHtml("<caption>");
                    writer.appendHtml(title);
                    writer.appendHtml("</thead>");
                }
            }
            
            @Override
            public void writeHeaders(final PageWriter writer) {
                final String headerSegment = headerClass == null ? "" : (" class=\"" + headerClass + "\"");
                writer.appendHtml("<thead" + headerSegment + ">");
                writer.appendHtml(headers);
                writer.appendHtml("</thead>");
            }

            @Override
            public void writeElement(final Request request, final RequestContext context, final ObjectAdapter element) {
                context.addVariable(variable, context.mapObject(element, Scope.REQUEST), Scope.REQUEST);
                final RepeatMarker end = request.createMarker();
                final RepeatMarker marker = block.getMarker();
                marker.repeat();
                request.processUtilCloseTag();
                end.repeat();
            }
        };
    }

    @Override
    public String getName() {
        return "table-builder";
    }

}
