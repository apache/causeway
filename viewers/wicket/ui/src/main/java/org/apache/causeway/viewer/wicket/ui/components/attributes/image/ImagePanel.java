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
package org.apache.causeway.viewer.wicket.ui.components.attributes.image;

import java.awt.image.BufferedImage;
import java.util.Optional;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.core.metamodel.valuesemantics.ImageValueSemantics;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

/**
 * Renders a thumb-nail of the underlying image and provides a show full size link on mouse-over.
 */
class ImagePanel
extends PanelAbstract<ManagedObject, UiAttributeWkt> {

    private static final long serialVersionUID = 1L;

    private static final String ID_THUMBNAIL = "thumbnail";
    private static final String ID_IMAGELINK = "imagelink";
    
    private static final CssResourceReference IMAGE_CSS =
        new CssResourceReference(ImagePanel.class, "ImagePanel.css");

    public ImagePanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel);
    }
    
    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(IMAGE_CSS));
    }
    
    @Override
    protected void onConfigure() {
        super.onConfigure();
        buildGui();
    }

    private void buildGui() {
        bufferedImage()
            .ifPresentOrElse(bufferedImage->{
                addOrReplace(Wkt.imageNonCaching(ID_THUMBNAIL, bufferedImage));
                addOrReplace(Wkt.link(ID_IMAGELINK, bufferedImage));
            }, ()->{
                WktComponents.permanentlyHide(this, ID_THUMBNAIL, ID_IMAGELINK);
            });
    }

    // -- HELPER
    
    private final UiAttributeWkt attributeModel() {
        return getModel();
    }

    private Optional<BufferedImage> bufferedImage() {
        var model = attributeModel();
        
        final ManagedObject adapter = model.getObject();
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) return Optional.empty();

        var typeSpec = model.getElementType();

        return Facets.valueStreamSemantics(typeSpec, ImageValueSemantics.class)
            .map(imageValueSemantics->imageValueSemantics.getImage(adapter).orElse(null))
            .filter(_NullSafe::isPresent)
            .findFirst();
    }
    
}
