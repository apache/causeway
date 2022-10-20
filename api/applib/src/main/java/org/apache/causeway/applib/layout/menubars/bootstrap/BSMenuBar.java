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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.layout.menubars.MenuBar;


/**
 * Describes the collection of domain services into menubars, broadly corresponding to the aggregation of information of {@link org.apache.causeway.applib.annotation.DomainServiceLayout} that have the same value of {@link DomainServiceLayout#named()}.
 *
 * @since 1.x {@index}
 */
@XmlType(
        name = "menuBar"
        , propOrder = {
                "menus"
        }
        )
public class BSMenuBar implements MenuBar, Serializable {

    private static final long serialVersionUID = 1L;

    public BSMenuBar() {
    }


    private List<BSMenu> menus = new ArrayList<>();

    // no wrapper
    @XmlElement(name = "menu", required = true)
    public List<BSMenu> getMenus() {
        return menus;
    }

    public void setMenus(List<BSMenu> menus) {
        this.menus = menus;
    }


}
