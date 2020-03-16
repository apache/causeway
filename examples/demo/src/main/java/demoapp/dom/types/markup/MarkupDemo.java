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
package demoapp.dom.types.markup;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.resources._Resources;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.types.clob.ClobDemo;
import demoapp.utils.DemoStub;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType="demo.Markup", editing=Editing.ENABLED)
@Log4j2
public class MarkupDemo extends DemoStub {

    @Override
    public void initDefaults() {

        log.info("MarkupDemo::initDefaults");

        try {
            val htmlSource = _Strings.read(_Resources.load(ClobDemo.class, "markup.html"), StandardCharsets.UTF_8);
            markup = new Markup(htmlSource);
        } catch (Exception e) {
            log.error("failed to create Markup from file resource", e);
        }
        
    }

    // -- EDITABLE

    @Property
    @XmlElement @Getter @Setter private Markup markup;
    
    
}
