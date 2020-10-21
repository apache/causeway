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

package org.apache.isis.core.metamodel.facets.value.imageawt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.apache.isis.commons.internal.image._Images;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.image.ImageValueSemanticsProviderAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public class JavaAwtImageValueSemanticsProvider 
extends ImageValueSemanticsProviderAbstract<BufferedImage> {

    public JavaAwtImageValueSemanticsProvider(final FacetHolder holder) {
        super(holder, BufferedImage.class);
    }

    @Override
    public int getWidth(final ManagedObject object) {
        return unwrap(object).getWidth();
    }
    
    @Override
    public int getHeight(final ManagedObject object) {
        return unwrap(object).getHeight();
    }
    
    @Override
    public void render(ManagedObject object, Graphics2D graphics) {
        graphics.drawImage(unwrap(object), 0, 0, null);
    }

    @Override
    protected String doEncode(BufferedImage image) {
        return _Images.toBase64(image);
    }
    
    @Override
    protected BufferedImage doRestore(String base64ImageData) {
        return _Images.fromBase64(base64ImageData);
    }
    
    @Override
    public boolean isFallback() {
        return false;
    }

    @Override
    public String toString() {
        return "JavaAwtImageValueSemanticsProvider: ";
    }
    
    // -- HELPER
    
    private BufferedImage unwrap(final ManagedObject object) {
        return (BufferedImage) object.getPojo();
    }


}
