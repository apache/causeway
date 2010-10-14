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


package org.apache.isis.metamodel.value;

import java.awt.Image;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;


public class JavaAwtImageValueSemanticsProvider extends ImageValueSemanticsProviderAbstract {

    public JavaAwtImageValueSemanticsProvider(
            final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(holder, Image.class, configuration, specificationLoader, runtimeContext);
    }

	public int getHeight(final ObjectAdapter object) {
        return image(object).getHeight(null);
    }

    private Image image(final ObjectAdapter object) {
        return (Image) object.getObject();
    }

    public Image getImage(final ObjectAdapter object) {
        return image(object);
    }

    @Override
    protected int[][] getPixels(final Object object) {
        return grabPixels((Image) object);
    }

    public Class<?> getValueClass() {
        return Image.class;
    }

    public int getWidth(final ObjectAdapter object) {
        return image(object).getWidth(null);
    }

    @Override
    protected Object setPixels(final int[][] pixels) {
        final Image image = createImage(pixels);
        return getRuntimeContext().adapterFor(image);
    }


    public boolean isNoop() {
        return false;
    }

    @Override
    public String toString() {
        return "JavaAwtImageValueSemanticsProvider: ";
    }

    public ObjectAdapter createValue(final Image image) {
        return getRuntimeContext().adapterFor(image);
   }
    
}

