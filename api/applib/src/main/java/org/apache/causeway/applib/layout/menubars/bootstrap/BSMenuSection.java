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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutDataOwner;
import org.apache.causeway.applib.layout.menubars.MenuSection;

import lombok.Getter;
import lombok.Setter;

/**
 * Corresponds to a domain service that contributes its serviceActions under a particular {@link BSMenuBar}.
 *
 * @since 1.x {@index}
 */
@XmlType(name = "section", propOrder = {"named", "serviceActions"})
@XmlAccessorType(XmlAccessType.FIELD)
public final class BSMenuSection implements MenuSection, Serializable, ServiceActionLayoutDataOwner {
    private static final long serialVersionUID = 1L;

    public BSMenuSection() {}

    @XmlElement(required = false)
    @Getter @Setter
    private String named;

    @XmlElement(name = "serviceAction", required = true)
    @Getter
    private final List<ServiceActionLayoutData> serviceActions = new ArrayList<>();

}
