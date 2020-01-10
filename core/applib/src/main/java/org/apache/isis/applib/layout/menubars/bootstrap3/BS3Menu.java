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
package org.apache.isis.applib.layout.menubars.bootstrap3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.layout.menubars.Menu;

import lombok.ToString;

/**
 * Describes the collection of domain services into menubars, broadly corresponding to the aggregation of information of {@link DomainServiceLayout} that have the same value of {@link DomainServiceLayout#named()}.
 */
@XmlType(
        name = "menu"
        , propOrder = {
                "named",
                "cssClassFa",
                "sections"
        }
        )
@ToString(of = "named")
public class BS3Menu implements Menu, Serializable {

    private static final long serialVersionUID = 1L;

    public BS3Menu() {
    }

    public BS3Menu(String named) {
        this.named = named;
    }


    private String named;

    @Override
    @XmlElement(required = true)
    public String getNamed() {
        return named;
    }

    public void setNamed(String named) {
        this.named = named;
    }



    private String cssClassFa;

    @XmlAttribute(required = false)
    public String getCssClassFa() {
        return cssClassFa;
    }

    public void setCssClassFa(final String cssClassFa) {
        this.cssClassFa = cssClassFa;
    }



    private List<BS3MenuSection> sections = new ArrayList<>();

    // no wrapper
    @XmlElement(name = "section", required = true)
    public List<BS3MenuSection> getSections() {
        return sections;
    }

    private Boolean unreferencedActions;

    /**
     * Whether this menu should be used to hold any unreferenced actions.
     *
     * <p>
     *     Any menubars layout must have precisely one menu that has this attribute set.
     * </p>
     */
    @XmlAttribute(required = false)
    public Boolean isUnreferencedActions() {
        return unreferencedActions;
    }

    public void setUnreferencedActions(final Boolean unreferencedActions) {
        this.unreferencedActions = unreferencedActions;
    }

}
