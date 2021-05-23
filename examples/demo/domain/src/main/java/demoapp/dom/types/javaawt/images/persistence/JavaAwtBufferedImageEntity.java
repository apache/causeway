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
package demoapp.dom.types.javaawt.images.persistence;

import java.awt.image.BufferedImage;

import org.apache.isis.applib.annotation.DomainObject;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom.types.javaawt.images.holder.JavaAwtBufferedImageHolder2;

@DomainObject(
        objectType = "demo.JavaAwtBufferedImageEntity" // shared permissions with concrete sub class
)
public abstract class JavaAwtBufferedImageEntity
implements
    HasAsciiDocDescription,
    JavaAwtBufferedImageHolder2,
    ValueHolder<java.awt.image.BufferedImage> {

    @Override
    public java.awt.image.BufferedImage value() {
        return getReadOnlyProperty();
    }

    // -- TODO R/W SUPPORT (actually implement in sub classes)

    @Override
    public BufferedImage getReadWriteProperty() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setReadWriteProperty(BufferedImage c) {
        // TODO Auto-generated method stub
    }

    @Override
    public BufferedImage getReadWriteOptionalProperty() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setReadWriteOptionalProperty(BufferedImage c) {
        // TODO Auto-generated method stub
    }

}
