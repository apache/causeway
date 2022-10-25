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
package org.apache.causeway.applib.services.confview;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.commons.internal.base._Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @since 2.0 {@index}
 */
@XmlRootElement(name = "configurationProperty")
@XmlType(
        propOrder = {
                "key",
                "value"
        }
)
@Named(ConfigurationProperty.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED)
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObjectLayout(paged = 999)
@NoArgsConstructor
public class ConfigurationProperty implements Comparable<ConfigurationProperty> {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE_CONF + ".ConfigurationProperty";

    public ConfigurationProperty(final String key, final String value){
        this.key = key;
        this.value = value;
    }

    @XmlElement(required = true)
    @Title
    @Getter @Setter
    private String key;

    @XmlElement(required = true)
    @Getter @Setter
    private String value;

    @Override
    public int compareTo(final ConfigurationProperty other) {
        return _Objects.compareNullsLast(getKey(), other.getKey());
    }

}
