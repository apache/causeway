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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Common superclass for any content of a row.
 *
 * <p> Most commonly the content of a row is {@link BSCol col}umns, but it may be either of the
 * {@link BSClearFix clearfix} classes.
 *
 * @since 1.x {@index}
 */
public sealed abstract class BSRowContent extends BSElementAbstract
permits BSCol, BSClearFix {

    private static final long serialVersionUID = 1L;

    /**
     * Default if not specified is {@link Size#MD}.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Size size;

    /**
     * Owner.
     * <p>Set programmatically by framework after reading in from XML.
     */
    @XmlTransient
    @Getter @Setter @Accessors(fluent=true)
    private BSRowContentOwner owner;

}
