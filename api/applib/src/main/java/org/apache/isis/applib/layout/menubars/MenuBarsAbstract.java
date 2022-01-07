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
package org.apache.isis.applib.layout.menubars;

import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.isis.applib.annotations.Programmatic;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;

import lombok.val;

/**
 * @since 1.x {@index}
 */
@XmlTransient // ignore this class
public abstract class MenuBarsAbstract implements MenuBars, Serializable {

    private static final long serialVersionUID = 1L;

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

        val serviceActionsByObjectTypeAndId =
                new LinkedHashMap<String, ServiceActionLayoutData>();

        visit(serviceActionLayoutData -> {
            serviceActionsByObjectTypeAndId
            .put(serviceActionLayoutData.getLogicalTypeNameAndId(), serviceActionLayoutData);
        });

        return serviceActionsByObjectTypeAndId;
    }


}
