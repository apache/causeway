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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.isis.applib.annotations.Programmatic;

/**
 * Common superclass for any content of a row.
 *
 * <p>
 *     Most commonly the content of a row is {@link BS3Col col}umns, but it may be either of the
 *     {@link BS3ClearFix clearfix} classes.
 * </p>
 *
 * @since 1.x {@index}
 */
public abstract class BS3RowContent extends BS3ElementAbstract {

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


    private BS3RowContentOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BS3RowContentOwner getOwner() {
        return owner;
    }

    public void setOwner(final BS3RowContentOwner owner) {
        this.owner = owner;
    }

    @Override
    @XmlTransient
    @Programmatic
    public BS3Grid getGrid() {
        return getOwner().getGrid();
    }


}
