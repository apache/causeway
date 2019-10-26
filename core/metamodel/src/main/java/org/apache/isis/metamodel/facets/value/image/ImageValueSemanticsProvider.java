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

package org.apache.isis.metamodel.facets.value.image;

import org.apache.isis.applib.value.Image;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.spec.ManagedObject;


public class ImageValueSemanticsProvider extends ImageValueSemanticsProviderAbstract<Image> {

    public ImageValueSemanticsProvider(final FacetHolder holder) {
        super(holder, Image.class);
    }

    @Override
    public int getHeight(final ManagedObject object) {
        return image(object).getHeight();
    }

    private Image image(final ManagedObject object) {
        return (Image) object.getPojo();
    }

    @Override
    public java.awt.Image getImage(final ManagedObject object) {
        return createImage(image(object).getImage());
    }

    @Override
    protected int[][] getPixels(final Object object) {
        return ((Image) object).getImage();
    }

    public Class<?> getValueClass() {
        return Image.class;
    }

    @Override
    public int getWidth(final ManagedObject object) {
        return image(object).getWidth();
    }

    @Override
    protected Image setPixels(final int[][] pixels) {
        return new Image(pixels);
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

    @Override
    public ManagedObject createValue(final java.awt.Image image) {
        return getObjectAdapterProvider().adapterFor(new Image(grabPixels(image)));
    }

}
