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
     * Name of this class (overriding the name derived from its name in code).
     */
    // tag::refguide[]
    String named() default "";

    public enum MenuBar {
        PRIMARY,
        SECONDARY,
        TERTIARY,
        NOT_SPECIFIED
    }

    // end::refguide[]
    /**
     * The menubar in which the menu that holds this service's actions should reside.
     */
    // tag::refguide[]
    MenuBar menuBar() default MenuBar.NOT_SPECIFIED;

    // end::refguide[]
    //TODO[2140] DomainServiceLayout
    //    /**
    //     * Number in Dewey Decimal format representing the order.
    //     *
    //     * <p>
    //     * Same convention as {@link org.apache.isis.applib.annotation.MemberOrder#sequence()}.  If not specified, placed after any named.
    //     * </p>
    //     *
    //     * <p>
    //     *     Either this attribute or {@link DomainService#menuOrder()} can be used; they are equivalent.
    //     *     Typically this attribute is used for services with a {@link DomainService#nature() nature} of
    //     *     {@link NatureOfService#VIEW_MENU_ONLY} (these are visible in the UI) whereas
    //     *     {@link DomainService#menuOrder()} is used for services with a nature of
    //     *     {@link NatureOfService#DOMAIN domain} (which do not appear in the UI).
    //     * </p>
    //     *
    //     * <p>
    //     *     The default value is set to "Integer.MAX_VALUE - 100" so that any domain services intended to override the
    //     *     default implementations provided by the framework itself will do so without having to specify the
    //     *     menuOrder (with the exception of <tt>EventBusServiceJdo</tt>, all framework implementations have a
    //     *     default order greater than Integer.MAX_VALUE - 50).
    //     * </p>
    //     */
    //    String menuOrder() default Constants.MENU_ORDER_DEFAULT;

    // tag::refguide[]

}
// end::refguide[]
