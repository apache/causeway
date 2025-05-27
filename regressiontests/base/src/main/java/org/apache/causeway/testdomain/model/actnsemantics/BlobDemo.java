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
package org.apache.causeway.testdomain.model.actnsemantics;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.resources._Resources;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@XmlRootElement(name = "BlobDemo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("regressiontests.BlobDemo")
@DomainObject(nature=Nature.VIEW_MODEL)
@Slf4j
public class BlobDemo {

    public void initDefaults() {

        try {
            var bytes = _Bytes.of(_Resources.load(BlobDemo.class, "causeway-logo-605x449.png"));
            logo = Blob.of("causeway-logo-605x449.png", CommonMimeType.PNG, bytes);
        } catch (Exception e) {
            log.error("failed to create Blob from image resource", e);
        }

    }

    // -- EDITABLE

    @Property(editing=Editing.ENABLED)
    @PropertyLayout
    @XmlElement @XmlJavaTypeAdapter(Blob.JaxbToStringAdapter.class)
    @Getter @Setter private Blob logo;

}
