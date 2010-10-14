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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype;

import org.apache.isis.applib.value.Image;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.value.ImageValueSemanticsProviderAbstract;


public class ImageType extends AbstractImageType {

    private final RuntimeContext runtimeContext;
    
    public ImageType(final RuntimeContext runtimeContext) {
    	this.runtimeContext = runtimeContext;
    }

    @Override
    protected ImageValueSemanticsProviderAbstract getImageAdapter() {
        // return new ImageValueSemanticsProvider(runtimeContext);
        throw new NotYetImplementedException();
        // TODO update to work with new mechanism
    }

    // protected AbstractImageAdapter getImageAdapter() {
    // if (value == null) {
    // return new JavaAwtImageValueSemanticsProvider();
    // }
    // return new JavaAwtImageValueSemanticsProvider((Image) value);
    // return new JavaAwtImageValueSemanticsProvider();
    // }

    @Override
    public Class<Image> returnedClass() {
        return Image.class;
    }
}
