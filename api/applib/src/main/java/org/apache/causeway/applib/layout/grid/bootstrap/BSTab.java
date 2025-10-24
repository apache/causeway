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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Represents a tab within a {@link BSTabGroup tab group}.
 *
 * <p>They simply contain one or more {@link BSRow row}s.
 *
 * @since 1.x {@index}
 */
@XmlType(
        name = "tab",
        propOrder = {"name", "rows"})
public final class BSTab extends BSElementAbstract implements BSRowOwner {

    private static final long serialVersionUID = 1L;

    private String name;
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    private List<BSRow> rows = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "row", required = true)
    public List<BSRow> getRows() {
        return rows;
    }

    public void setRows(final List<BSRow> rows) {
        this.rows = rows;
    }

    private BSTabOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BSTabOwner getOwner() {
        return owner;
    }

    public void setOwner(final BSTabOwner owner) {
        this.owner = owner;
    }

    @Override public String toString() {
        return "BSTab{" +
                "name='" + name + '\'' +
                '}';
    }
}
