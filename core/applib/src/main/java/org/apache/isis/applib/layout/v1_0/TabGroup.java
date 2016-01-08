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
package org.apache.isis.applib.layout.v1_0;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@XmlType()
public class TabGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    // must be at least one tab.
    private List<Tab> tabs = new ArrayList<Tab>(){{
        add(new Tab());
    }};



    // no wrapper
    @XmlElement(name = "tab", required = true)
    public List<Tab> getTabs() {
        return tabs;
    }

    public void setTabs(List<Tab> tabs) {
        this.tabs = tabs;
    }



    private ObjectLayoutMetadata owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public ObjectLayoutMetadata getOwner() {
        return owner;
    }

    public void setOwner(final ObjectLayoutMetadata owner) {
        this.owner = owner;
    }


    public static class Predicates {
        public static Predicate<TabGroup> notEmpty() {
            return new Predicate<TabGroup>() {
                @Override
                public boolean apply(final TabGroup tabGroup) {
                    return FluentIterable
                            .from(tabGroup.getTabs())
                            .anyMatch(Tab.Predicates.notEmpty());
                }
            };
        }
    }

}
