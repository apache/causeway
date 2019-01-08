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
package org.apache.isis.applib.annotation;

import javax.xml.bind.annotation.XmlType;

/**
 * The available policies for rendering the next page if the result is the same as the target
 * (in other words, an action that returns "this").
 */
@XmlType(
        namespace = "http://isis.apache.org/applib/layout/component"
)
public enum Redirect {
    /**
     * As defined by configuration property <code>isis.viewer.wicket.redirectEvenIfSameObject</code>
     */
    AS_CONFIGURED,

    /**
     * Redirect (meaning render a new page) even if the result of the action is the same as the target.
     */
    EVEN_IF_SAME,

    /**
     * Don't redirect if the result is the same as the target, instead just update the existing page.
     *
     * <p>
     *     Of course, a redirect is still performed if the result of the action is different from the target.
     * </p>
     */
    ONLY_IF_DIFFERS,
}
