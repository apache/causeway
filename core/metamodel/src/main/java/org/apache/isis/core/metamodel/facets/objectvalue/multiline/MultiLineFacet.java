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

package org.apache.isis.core.metamodel.facets.objectvalue.multiline;

import org.apache.isis.core.metamodel.facets.MultipleValueFacet;

/**
 * Whether the (string) property or parameter should be rendered over multiple
 * lines.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to the
 * <tt>@MultiLine</tt> annotation.
 */
public interface MultiLineFacet extends MultipleValueFacet {

    /**
     * How many lines to use.
     */
    int numberOfLines();

    /**
     * Whether carriage returns should be used to split over multiple lines or
     * not.
     *
     * <p>
     * If set to <tt>true</tt>, then user must use carriage returns to split. If
     * set to <tt>false</tt>, then the viewer should automatically wrap when
     * spills over the length of one line.
     */
    @Deprecated
    boolean preventWrapping();

}
