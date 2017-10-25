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
package org.apache.isis.applib.layout.menus;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainServiceLayout;

/**
 * Describes the collection of domain services into menubars, broadly corresponding to the aggregation of information of {@link DomainServiceLayout} that have the same value of {@link DomainServiceLayout#named()}.
 */
@XmlType(
        name = "menu"
        , propOrder = {
            "named",
            "sections"
        }
)
public class Menu implements Serializable, HasNamed, HasCssClassFa {

    private static final long serialVersionUID = 1L;

    public Menu() {
    }

    public Menu(String named) {
        this.named = named;
    }


    private String named;

    @Override
    @XmlElement(required = true)
    public String getNamed() {
        return named;
    }

    @Override
    public void setNamed(String named) {
        this.named = named;
    }



    private String cssClassFa;

    @Override
    @XmlAttribute(required = false)
    public String getCssClassFa() {
        return cssClassFa;
    }

    @Override
    public void setCssClassFa(final String cssClassFa) {
        this.cssClassFa = cssClassFa;
    }



    private List<MenuSection> sections = Lists.newArrayList();

    // no wrapper
    @XmlElement(name = "section", required = false)
    public List<MenuSection> getSections() {
        return sections;
    }

    public void setSections(List<MenuSection> menuSections) {
        this.sections = sections;
    }

}
