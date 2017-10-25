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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Describes the collection of domain services into menubars, broadly corresponding to the aggregation of information within {@link org.apache.isis.applib.annotation.DomainServiceLayout}.
 */
@XmlRootElement(
        name = "menuBars"
)
@XmlType(
        name = "menuBars"
        , propOrder = {
            "primary",
            "secondary",
            "tertiary",
            "metadataError"
        }
)
public class MenuBars implements Serializable {

    private static final long serialVersionUID = 1L;

    public MenuBars() {
    }

    private MenuBar primary = new MenuBar();

    public MenuBar getPrimary() {
        return primary;
    }

    public void setPrimary(final MenuBar primary) {
        this.primary = primary;
    }

    private MenuBar secondary = new MenuBar();

    public MenuBar getSecondary() {
        return secondary;
    }

    public void setSecondary(final MenuBar secondary) {
        this.secondary = secondary;
    }

    private MenuBar tertiary = new MenuBar();

    public MenuBar getTertiary() {
        return tertiary;
    }

    public void setTertiary(final MenuBar tertiary) {
        this.tertiary = tertiary;
    }



    private String metadataError;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    public String getMetadataError() {
        return metadataError;
    }

    public void setMetadataError(final String metadataError) {
        this.metadataError = metadataError;
    }


}
