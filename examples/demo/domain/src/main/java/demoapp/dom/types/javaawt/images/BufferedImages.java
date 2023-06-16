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
package demoapp.dom.types.javaawt.images;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.types.Samples;
import demoapp.dom.types.javaawt.images.persistence.BufferedImageEntity;

@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.BufferedImages")
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
//@Log4j2
public class BufferedImages implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "java.awt.image.BufferedImage data type";
    }

    @Collection
    public List<? extends BufferedImageEntity> getEntities() {
        return entities.all();
    }

    @Inject
    @XmlTransient
    ValueHolderRepository<BufferedImage, ? extends BufferedImageEntity> entities;

    @Inject
    @XmlTransient
    Samples<BufferedImage> samples;

}
