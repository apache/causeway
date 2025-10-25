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
package org.apache.causeway.applib.layout.menubars;

import java.io.Serializable;
import java.util.LinkedHashMap;

import jakarta.xml.bind.annotation.XmlTransient;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;

/**
 * @since 1.x {@index}
 */
@XmlTransient // ignore this class
public abstract class MenuBarsAbstract implements MenuBars, Serializable {

    private static final long serialVersionUID = 1L;

    @XmlTransient
    private String tnsAndSchemaLocation;

    @Override
    @Programmatic
    @XmlTransient
    public String getTnsAndSchemaLocation() {
        return tnsAndSchemaLocation;
    }

    @Override
    @Programmatic
    public void setTnsAndSchemaLocation(final String tnsAndSchemaLocation) {
        this.tnsAndSchemaLocation = tnsAndSchemaLocation;
    }

    @Override
    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, ServiceActionLayoutData> getAllServiceActionsByObjectTypeAndId() {

        var serviceActionsByObjectTypeAndId =
                new LinkedHashMap<String, ServiceActionLayoutData>();

        visit(serviceActionLayoutData -> {
            serviceActionsByObjectTypeAndId
            .put(serviceActionLayoutData.getLogicalTypeNameAndId(), serviceActionLayoutData);
        });

        return serviceActionsByObjectTypeAndId;
    }

}
