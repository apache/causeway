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
package org.apache.isis.viewer.wicket.ui.components.scalars.image;

import java.awt.Graphics2D;

import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;

import org.apache.isis.core.metamodel.facets.value.image.ImageValueFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.NonNull;
import lombok.val;

//@Log4j2
final class JavaAwtImageDynamicResource extends RenderedDynamicImageResource {
    
    private static final long serialVersionUID = 1L;
    
    private ScalarModel model;
    
    public static JavaAwtImageDynamicResource of(@NonNull ScalarModel model) {
        val imageValueFacet = model.getTypeOfSpecification().getFacet(ImageValueFacet.class);
        val adapter = model.getObject();
        return new JavaAwtImageDynamicResource(
                model, 
                imageValueFacet.getWidth(adapter), 
                imageValueFacet.getHeight(adapter));
    }

    private JavaAwtImageDynamicResource(ScalarModel model, int width, int height) {
        super(width, height);
        this.model = model;
    }

    @Override
    protected boolean render(final Graphics2D graphics, final Attributes attributes) {
        val imageValueFacet = model.getTypeOfSpecification().getFacet(ImageValueFacet.class);
        val adapter = model.getObject();
        imageValueFacet.render(adapter, graphics);
        return true;
    }
    
}
