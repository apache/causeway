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
package org.apache.causeway.applib.layout.menubars.bootstrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.layout.menubars.Menu;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes the collection of domain services into menubars,
 * broadly corresponding to the aggregation of information of
 * {@link DomainServiceLayout} that have the same value of
 * {@link DomainServiceLayout#named()}.
 *
 * @since 1.x {@index}
 */
@XmlType(
    name = "menu", propOrder = {"named", "cssClassFa", "sections"})
@XmlAccessorType(XmlAccessType.FIELD)
@ToString(of = "named")
public class BSMenu implements Menu, Serializable {
    private static final long serialVersionUID = 1L;

    public BSMenu() {}
    public BSMenu(String named) {
        this.named = named;
    }

    @XmlElement(required = true)
    @Getter @Setter
    private String named;

    @XmlAttribute(required = false)
    @Getter @Setter
    private String cssClassFa;

    @XmlElement(name = "section", required = true)
    @Getter
    private final List<BSMenuSection> sections = new ArrayList<>();

    /**
     * Whether this menu should be used to hold any unreferenced actions.
     * <p>Any menubars layout must have precisely one menu that has this attribute set.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Boolean unreferencedActions;
    public boolean isUnreferencedActions() {
        return unreferencedActions!=null && unreferencedActions.booleanValue();
    }

}
