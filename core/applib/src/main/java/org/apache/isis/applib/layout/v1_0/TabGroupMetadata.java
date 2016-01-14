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

import org.apache.isis.applib.annotation.Programmatic;

@XmlType(
        propOrder = {
                "tabs"
        }

)
public class TabGroupMetadata implements ColumnOwner, Serializable, HasPath, Owned<TabGroupOwner> {

    private static final long serialVersionUID = 1L;

    // must be at least one tab.
    private List<TabMetadata> tabs = new ArrayList<TabMetadata>(){{
        add(new TabMetadata());
    }};



    // no wrapper
    @XmlElement(name = "tab", required = true)
    public List<TabMetadata> getTabs() {
        return tabs;
    }

    public void setTabs(List<TabMetadata> tabs) {
        this.tabs = tabs;
    }



    private TabGroupOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public TabGroupOwner getOwner() {
        return owner;
    }

    public void setOwner(final TabGroupOwner owner) {
        this.owner = owner;
    }




    private String path;

    @Programmatic
    @XmlTransient
    public String getPath() {
        return path;
    }

    @Programmatic
    public void setPath(final String path) {
        this.path = path;
    }



    public static class Predicates {
        public static Predicate<TabGroupMetadata> notEmpty() {
            return new Predicate<TabGroupMetadata>() {
                @Override
                public boolean apply(final TabGroupMetadata tabGroup) {
                    return FluentIterable
                            .from(tabGroup.getTabs())
                            .anyMatch(TabMetadata.Predicates.notEmpty());
                }
            };
        }
    }

}
