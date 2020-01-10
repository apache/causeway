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
package org.apache.isis.applib.layout.grid.bootstrap3;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.commons.internal.collections._Lists;

/**
 * Contains a row of content, either on the top-level {@link BS3Grid page} or at any other lower-level element that can
 * contain rows, eg {@link BS3Tab tabs}.
 *
 * <p>
 *     It is rendered as a &lt;div class=&quot;row ...&quot;&gt;
 * </p>
 */
@XmlType(
        name = "row"
        , propOrder = {
                "cols"
                , "metadataError"
        }
        )
public class BS3Row extends BS3ElementAbstract implements HasCssId, BS3RowContentOwner {

    private static final long serialVersionUID = 1L;


    private String id;

    /**
     * As per &lt;div id=&quot;...&quot;&gt;...&lt;/div&gt; : must be unique across entire page.
     */
    @Override
    @XmlAttribute(required = false)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }



    private List<BS3RowContent> cols = _Lists.newArrayList();

    // no wrapper
    @XmlElementRefs({
        @XmlElementRef(type = BS3Col.class, name="col", required = true),
        @XmlElementRef(type = BS3ClearFixVisible.class,  name="clearFixVisible", required = false),
        @XmlElementRef(type = BS3ClearFixHidden.class,  name="clearFixHidden", required = false)
    })
    public List<BS3RowContent> getCols() {
        return cols;
    }

    public void setCols(final List<BS3RowContent> cols) {
        this.cols = cols;
    }




    private String metadataError;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    public String getMetadataError() {
        return metadataError;
    }

    public void setMetadataError(final String metadataError) {
        this.metadataError = metadataError;
    }


    private BS3RowOwner owner;


    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BS3RowOwner getOwner() {
        return owner;
    }

    public void setOwner(final BS3RowOwner owner) {
        this.owner = owner;
    }



    @Override
    @XmlTransient
    @Programmatic
    public BS3Grid getGrid() {
        return getOwner().getGrid();
    }


    @Override public String toString() {
        return "BS3Row{" +
                "id='" + id + '\'' +
                '}';
    }


}
