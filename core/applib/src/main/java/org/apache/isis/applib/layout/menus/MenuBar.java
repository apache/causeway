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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainServiceLayout;

/**
 * Describes the collection of domain services into menubars, broadly corresponding to the aggregation of information of {@link org.apache.isis.applib.annotation.DomainServiceLayout} that have the same value of {@link DomainServiceLayout#named()}.
 */
@XmlType(
        name = "menuBar"
        , propOrder = {
            "menus"
        }
)
public class MenuBar implements Serializable {

    private static final long serialVersionUID = 1L;

    public MenuBar() {
    }


    private List<Menu> menus = Lists.newArrayList();

    // no wrapper
    @XmlElement(name = "menu", required = false)
    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }


}
