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
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
import jakarta.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contains a row of content, either on the top-level {@link BSGrid page} or at any other lower-level element that can
 * contain rows, eg {@link BSTab tabs}.
 *
 * <p>It is rendered as a &lt;div class=&quot;row ...&quot;&gt;
 *
 * @since 1.x {@index}
 */
@XmlType(name = "row", propOrder = {"rowContents", "metadataError"})
@NoArgsConstructor @AllArgsConstructor
public final class BSRow extends BSElementAbstract implements HasElementId, BSRowContentOwner {
    private static final long serialVersionUID = 1L;

    /**
     * As per &lt;div id=&quot;...&quot;&gt;...&lt;/div&gt; : must be unique across entire page.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String id;

    @XmlElementRefs({
        @XmlElementRef(type = BSCol.class, name="col", required = true),
        @XmlElementRef(type = BSClearFixVisible.class,  name="clearFixVisible", required = false),
        @XmlElementRef(type = BSClearFixHidden.class,  name="clearFixHidden", required = false)
    })
    @Getter @Setter
    private List<BSRowContent> rowContents = new ArrayList<>();

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String metadataError;

    @Override public String toString() {
        return "BSRow{" + "id='" + id + '\'' + '}';
    }
}
