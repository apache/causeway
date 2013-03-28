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
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.image.ImageLookup;
import org.apache.isis.viewer.html.request.Request;

public class CollectionIcon implements Component {

    private final ObjectAdapter collection;
    private final PathBuilder pathBuilder;
    private final String id;
    private final String description;

    public CollectionIcon(final PathBuilder pathBuilder, final ObjectAdapter element, final String description, final String id) {
        this.pathBuilder = pathBuilder;
        this.collection = element;
        this.description = description;
        this.id = id;
    }

    @Override
    public void write(final PrintWriter writer) {
        final TypeOfFacet facet = collection.getSpecification().getFacet(TypeOfFacet.class);
        final Class<?> elementType = facet.value();
        final ObjectSpecification elementSpecification = IsisContext.getSpecificationLoader().loadSpecification(elementType);

        writer.print("<div class=\"item\">");
        writer.print("<a href=\"");
        writer.print(pathTo(Request.COLLECTION_COMMAND) + "?id=");
        writer.print(id);
        writer.print("\"");
        if (description != null) {
            writer.print(" title=\"");
            writer.print(description);
            writer.print("\"");
        }
        writer.print("><img src=\"");
        writer.print(ImageLookup.image(elementSpecification));
        writer.print("\" alt=\"");
        final String singularName = elementSpecification.getSingularName();
        writer.print(singularName);
        writer.print(" collection\" />");
        writer.print(collection.titleString());
        writer.print("</a>");

        writer.println("</div>");

    }

    protected String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }

}
