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

package org.apache.isis.core.metamodel.facets.value.image;

import java.awt.image.BufferedImage;

import org.apache.isis.applib.value.Image;
import org.apache.isis.commons.internal.image._Images;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;


public class ImageValueSemanticsProvider 
extends ImageValueSemanticsProviderAbstract<Image> {

    public ImageValueSemanticsProvider(final FacetHolder holder) {
        super(holder, Image.class);
    }

    @Override
    public int getHeight(final ManagedObject object) {
        return unwrap(object).getHeight();
    }

    @Override
    public int getWidth(final ManagedObject object) {
        return unwrap(object).getWidth();
    }
    
    @Override
    public BufferedImage getImage(final ManagedObject object) {
        return _Images.fromPixels(unwrap(object).getImage());
    }
    
    @Override
    protected String doEncode(Image image) {
        return _Images.toBase64(_Images.fromPixels(image.getImage()));
    }
    
    @Override
    protected Image doRestore(String base64ImageData) {
        return new Image(_Images.toPixels(_Images.fromBase64(base64ImageData)));
    }
    
    @Override
    public ManagedObject createValue(BufferedImage image) {
        return getObjectManager().adapt(new Image(_Images.toPixels(image)));
    }

    @Override
    public Facet getUnderlyingFacet() {
        return null;
    }

    /**
     * Not required because {@link #alwaysReplace()} is <tt>false</tt>.
     */
    @Override
    public void setUnderlyingFacet(final Facet underlyingFacet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean alwaysReplace() {
        return false;
    }

    @Override
    public boolean isFallback() {
        return false;
    }

    @Override
    public String toString() {
        return "ImageValueSemanticsProvider: ";
    }
    
    // -- HELPER

    private Image unwrap(final ManagedObject object) {
        return (Image) object.getPojo();
    }
    

}
