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

package org.apache.isis.viewer.html.component.html;

import java.io.PrintWriter;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.ComponentAbstract;
import org.apache.isis.viewer.html.image.ImageLookup;
import org.apache.isis.viewer.html.request.Request;

class CollectionLink extends ComponentAbstract {
    private final String objectId;
    private final String fieldId;
    private final ObjectSpecification specification;
    private final String title;
    private final String description;

    public CollectionLink(final PathBuilder pathBuilder, final ObjectAssociation field, final ObjectAdapter collection, final String description, final String objectId) {
        super(pathBuilder);
        this.description = description;
        this.objectId = objectId;
        fieldId = field.getId();
        title = collection.titleString();
        specification = field.getSpecification();
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.print("<span class=\"value\"");
        if (description != null) {
            writer.print(" title=\"");
            writer.print(description);
            writer.print("\"");
        }
        writer.print(">");

        writer.print("<a href=\"");
        writer.print(pathTo(Request.FIELD_COLLECTION_COMMAND) + "?id=");
        writer.print(objectId);
        writer.print("&amp;field=");
        writer.print(fieldId);
        writer.print("\"");
        writer.print("><img src=\"");
        writer.print(ImageLookup.image(specification));
        writer.print("\" alt=\"icon\">");
        // writer.print(elementType);
        writer.print(title);
        writer.print("</a>");
        writer.println("</span>");
    }

    protected String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }

}
