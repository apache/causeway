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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.causeway.applib.annotation.Programmatic;

/**
 * Common superclass for any content of a row.
 *
 * <p>
 *     Most commonly the content of a row is {@link BSCol col}umns, but it may be either of the
 *     {@link BSClearFix clearfix} classes.
 * </p>
 *
 * @since 1.x {@index}
 */
public abstract class BSRowContent extends BSElementAbstract {

    private static final long serialVersionUID = 1L;

    private Size size;

    /**
     * Default if not specified is {@link Size#MD}.
     */
    @XmlAttribute(required = false)
    public Size getSize() {
        return size;
    }

    public void setSize(final Size size) {
        this.size = size;
    }


    private BSRowContentOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BSRowContentOwner getOwner() {
        return owner;
    }

    public void setOwner(final BSRowContentOwner owner) {
        this.owner = owner;
    }

    @Override
    @XmlTransient
    @Programmatic
    public BSGrid getGrid() {
        return getOwner().getGrid();
    }


}
