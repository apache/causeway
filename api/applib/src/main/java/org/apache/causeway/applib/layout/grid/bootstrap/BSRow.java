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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Programmatic;

/**
 * Contains a row of content, either on the top-level {@link BSGrid page} or at any other lower-level element that can
 * contain rows, eg {@link BSTab tabs}.
 *
 * <p>
 *     It is rendered as a &lt;div class=&quot;row ...&quot;&gt;
 * </p>
 *
 * @since 1.x {@index}
 */
@XmlType(
        name = "row"
        , propOrder = {
                "cols"
                , "metadataError"
        }
        )
public class BSRow extends BSElementAbstract implements HasCssId, BSRowContentOwner {

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



    private List<BSRowContent> cols = new ArrayList<>();

    // no wrapper
    @XmlElementRefs({
        @XmlElementRef(type = BSCol.class, name="col", required = true),
        @XmlElementRef(type = BSClearFixVisible.class,  name="clearFixVisible", required = false),
        @XmlElementRef(type = BSClearFixHidden.class,  name="clearFixHidden", required = false)
    })
    public List<BSRowContent> getCols() {
        return cols;
    }

    public void setCols(final List<BSRowContent> cols) {
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


    private BSRowOwner owner;


    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BSRowOwner getOwner() {
        return owner;
    }

    public void setOwner(final BSRowOwner owner) {
        this.owner = owner;
    }



    @Override
    @XmlTransient
    @Programmatic
    public BSGrid getGrid() {
        return getOwner().getGrid();
    }


    @Override public String toString() {
        return "BSRow{" +
                "id='" + id + '\'' +
                '}';
    }


}
