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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Layout hints for domain services.
 *
 * <p>
 * Also indicates the name, and menu ordering UI hints.
 * </p>
 */
// tag::refguide[]
@Inherited
@Target({
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainServiceLayout {

    // end::refguide[]
    /**
     * The menubar in which the menu that holds this service's actions should reside.
     */
    // tag::refguide[]
    MenuBar menuBar()                           // <.>
            default MenuBar.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * Name of this class (overriding the name derived from its name in code).
     */
    // tag::refguide[]
    String named()                              // <.>
            default "";

    // end::refguide[]

    // tag::refguide-menu-bar[]
    public enum MenuBar {
        PRIMARY,
        SECONDARY,
        TERTIARY,
        NOT_SPECIFIED
    }
    // end::refguide-menu-bar[]

    // tag::refguide[]
}
// end::refguide[]
