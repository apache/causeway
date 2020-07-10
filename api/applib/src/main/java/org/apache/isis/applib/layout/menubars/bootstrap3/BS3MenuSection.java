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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.component.ServiceActionLayoutDataOwner;
import org.apache.isis.applib.layout.menubars.MenuSection;


/**
 * Corresponds to a domain service that contributes its serviceActions under a particular {@link BS3MenuBar}.
 */
@XmlType(
        name = "section"
        , propOrder = {
                "named",
                "serviceActions"
        }
        )
public class BS3MenuSection implements MenuSection, Serializable, ServiceActionLayoutDataOwner {

    private static final long serialVersionUID = 1L;

    public BS3MenuSection() {
    }

    private String named;

    @Override
    @XmlElement(required = false)
    public String getNamed() {
        return named;
    }

    public void setNamed(String named) {
        this.named = named;
    }
    
    private List<ServiceActionLayoutData> serviceActions = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "serviceAction", required = true)
    public List<ServiceActionLayoutData> getServiceActions() {
        return serviceActions;
    }

    @Override
    public void setServiceActions(List<ServiceActionLayoutData> actionLayoutDatas) {
        this.serviceActions = actionLayoutDatas;
    }


}
