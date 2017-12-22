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

package org.apache.isis.applib.services.config;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Title;

@XmlRootElement(name = "configurationProperty")
@XmlType(
        propOrder = {
                "key",
                "value"
        }
)
@DomainObject(
        editing = Editing.DISABLED,
        objectType = "isisApplib.ConfigurationProperty"
)
@DomainObjectLayout(
        paged = 999
)
public class ConfigurationProperty implements Comparable<ConfigurationProperty> {

    public ConfigurationProperty(){}

    public ConfigurationProperty(final String key, final String value){
        this.key = key;
        this.value = value;
    }

    private String key;

    @XmlElement(required = true)
    @Title
    public String getKey() {
        return key;
    }
    public void setKey(final String key) {
        this.key = key;
    }


    private String value;

    @XmlElement(required = true)
    public String getValue() {
        return Util.maskIfProtected(key, value);
    }

    public void setValue(final String value) {
        this.value = value;
    }


    @Override
    public int compareTo(final ConfigurationProperty other) {
        return ComparisonChain.start()
                .compare(getKey(), other.getKey())
                .result();
    }

    public static class Util {

        private static final List<String> PROTECTED_KEYS =
                Collections.unmodifiableList(Lists.newArrayList("password", "apiKey", "authToken"));

        private Util(){}

        static boolean isProtected(final String key) {
            if(Strings.isNullOrEmpty(key)) {
                return false;
            }
            final String toLowerCase = key.toLowerCase();
            for (String protectedKey : PROTECTED_KEYS) {
                if(toLowerCase.contains(protectedKey.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        public static String maskIfProtected(final String key, final String value) {
            return isProtected(key) ? "********" : value;
        }
    }


}
