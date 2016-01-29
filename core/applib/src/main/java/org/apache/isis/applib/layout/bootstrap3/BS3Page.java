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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.members.v1.ActionLayoutData;
import org.apache.isis.applib.layout.members.v1.DomainObjectLayoutData;
import org.apache.isis.applib.layout.members.v1.Page;
import org.apache.isis.applib.services.dto.Dto;

/**
 * This is the top-level for rendering the domain object's properties, collections and actions.  It simply consists
 * of a number of rows.
 *
 * <p>
 *     The {@link #isHeader()} is intended as a convenience to automatically render the object's icon/title and any
 *     {@link ActionLayoutData action}s not otherwise associated with object members.  It is required to be
 *     specified and will be set to <code>true</code> for the vast majority of domain object.  If set to 
 *     <code>false</code> then the icon/title and actions can instead be rendered within a {@link BS3Col col}umn, at
 *     anywhere on the page.
 * </p>
 *
 * <p>
 *     The element is rendered as a &lt;div class=&quot;...&quot;&gt;
 * </p>
 */
@XmlRootElement(
        name = "page"
)
@XmlType(
        name = "page"
        , propOrder = {
            "rows"
        }
)
public class BS3Page extends BS3ElementAbstract implements Page, Dto {

    private static final long serialVersionUID = 1L;


    private boolean header;

    /**
     * Whether to render the top-level header (consisting of {@link DomainObjectLayoutData icon/title} and any
     * not-otherwise-identified {@link ActionLayoutData action}s.
     *
     * <p>
     *     Most pages will have this set.  Note that it is possible however to leave this unset and then to manually
     *     construct the header (or some other arrangement) using {@link BS3Col col}.
     * </p>
     */
    @XmlAttribute(required = true)
    public boolean isHeader() {
        return header;
    }

    public void setHeader(final boolean header) {
        this.header = header;
    }


    private List<BS3Row> rows = new ArrayList<BS3Row>(){{
        add(new BS3Row());
    }};

    // no wrapper
    @XmlElement(name = "row", required = true)
    public List<BS3Row> getRows() {
        return rows;
    }

    public void setRows(final List<BS3Row> rows) {
        this.rows = rows;
    }



    private boolean normalized;

    @Programmatic
    @XmlTransient
    public boolean isNormalized() {
        return normalized;
    }

    @Programmatic
    public void setNormalized(final boolean normalized) {
        this.normalized = normalized;
    }


}
