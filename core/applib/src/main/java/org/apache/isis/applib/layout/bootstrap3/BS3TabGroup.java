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
package org.apache.isis.applib.layout.bootstrap3;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a tab group containing one or more {@link BS3Tab tab}s.
 */
@XmlType(
        name = "tabGroup"
        , propOrder = {
            "tabs"
        }
)
public class BS3TabGroup extends BS3ElementAbstract implements BS3TabOwner {

    private static final long serialVersionUID = 1L;


    private List<BS3Tab> tabs = new ArrayList<BS3Tab>(){{
        add(new BS3Tab());
    }};

    // no wrapper
    @XmlElement(name = "tab", required = true)
    public List<BS3Tab> getTabs() {
        return tabs;
    }

    public void setTabs(final List<BS3Tab> tabs) {
        this.tabs = tabs;
    }


    private BS3TabGroupOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BS3TabGroupOwner getOwner() {
        return owner;
    }

    public void setOwner(final BS3TabGroupOwner owner) {
        this.owner = owner;
    }

}

