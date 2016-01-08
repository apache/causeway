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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(
        name="action"
        , propOrder = {
                "id"
                , "layout"
        }
)
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    public Action() {
    }
    public Action(final String id) {
        setId(id);
    }

    private String id;
    /**
     * Method name.
     *
     * <p>
     *     Overloaded methods are not supported.
     * </p>
     */
    @XmlAttribute(name="id", required = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    private ActionLayout layout;

    @XmlElement(name="layout", required = true)
    public ActionLayout getLayout() {
        return layout;
    }

    public void setLayout(ActionLayout layout) {
        this.layout = layout;
    }


    private ActionHolder owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public ActionHolder getOwner() {
        return owner;
    }

    public void setOwner(final ActionHolder owner) {
        this.owner = owner;
    }

}
