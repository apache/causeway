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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.common.Page;
import org.apache.isis.applib.services.dto.Dto;

/**
 * This is the top-level for rendering the domain object's properties, collections and actions.  It simply consists
 * of a number of rows.
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
            , "metadataErrors"
        }
)
public class BS3Page extends BS3ElementAbstract implements Page, Dto {

    private static final long serialVersionUID = 1L;

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




    private List<String> metadataErrors = Lists.newArrayList();

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     * 
     * <p>
     *     For example, if there is not exactly one {@link BS3Col} with the
     *     {@link BS3Col#isUnreferencedActions()} attribute set, then this is an error.  Ditto for
     *     {@link BS3Col#isUnreferencedCollections() collections}
     *     and {@link BS3Col#isUnreferencedProperties() properties}.
     * </p>
     */
    @XmlElement(required = false)
    public List<String> getMetadataErrors() {
        return metadataErrors;
    }

    public void setMetadataErrors(final List<String> metadataErrors) {
        this.metadataErrors = metadataErrors;
    }
}
