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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;

import org.apache.isis.applib.services.dto.Dto;

@XmlRootElement
@XmlType(
        propOrder = {
                "actions"
                , "tabGroups"
        }
)
public class DomainObject implements Dto {

    private List<Action> actions = Lists.newArrayList();

    @XmlElementWrapper(required = true)
    @XmlElement(name = "action", required = false)
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }


    // must have at least one tab group
    private List<TabGroup> tabGroups = new ArrayList<TabGroup>() {{
        add(new TabGroup());
    }};

    @XmlElementWrapper(required = true)
    @XmlElement(name = "tabGroup", required = true)
    public List<TabGroup> getTabGroups() {
        return tabGroups;
    }

    public void setTabGroups(List<TabGroup> tabGroups) {
        this.tabGroups = tabGroups;
    }
}
