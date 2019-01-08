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

import javax.xml.bind.annotation.XmlType;

/**
 * Layout hints for actions.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionLayout {


    /**
     * Whether (and how) this action can be bookmarked in the UI.
     *
     * <p>
     *     For bookmarkable actions, either {@link org.apache.isis.applib.annotation.BookmarkPolicy#AS_ROOT}
     *     and {@link org.apache.isis.applib.annotation.BookmarkPolicy#AS_CHILD} can be used (they are treated
     *     identically).
     * </p>
     */
    BookmarkPolicy bookmarking() default BookmarkPolicy.NEVER;

    // //////////////////////////////////////

    /**
     * Indicates the css class that an action should have.
     *
     * <p>
     *     For the Wicket viewer, this can be a bootstrap class such as <code>btn-info</code>.
     * </p>
     */
    String cssClass() default "";

    // //////////////////////////////////////

    /**
     * Indicates the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a> CSS class to decorate an
     * action (button or menu item).
     */
    String cssClassFa() default "";

    // //////////////////////////////////////

    /**
     * Indicates the position of the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a>
     * icon. The icon could be rendered on the left or the right of the action button
     */
    CssClassFaPosition cssClassFaPosition() default CssClassFaPosition.LEFT;

    @XmlType(
            namespace = "http://isis.apache.org/applib/layout/component"
    )
    enum CssClassFaPosition {
        LEFT, RIGHT
    }

    // //////////////////////////////////////

    /**
     * Description of this action, eg to be rendered in a tooltip.
     */
    String describedAs() default "";

    // //////////////////////////////////////

    /**
     * Indicates where in the UI the action should <i>not</i>not be visible.
     */
    Where hidden() default Where.NOT_SPECIFIED;

    // //////////////////////////////////////

    /**
     * Name of this action (overriding the name derived from its name in code).
     */
    String named() default "";

    // //////////////////////////////////////

    /**
     * For actions that are associated with a property, indicates the positioning of the
     * action's button relative to the property.
     *
     * <p>
     * Ignored if the action has not been associated with a property.
     * </p>
     */
    Position position() default Position.BELOW;

    @XmlType(
            namespace = "http://isis.apache.org/applib/layout/component"
    )
    enum Position {
        BELOW,
        RIGHT,
        PANEL,
        PANEL_DROPDOWN
    }

    /**
     * How this parameters for this action are prompted, either {@link PromptStyle#DIALOG dialog} or {@link PromptStyle#INLINE inline}.
     */
    PromptStyle promptStyle() default PromptStyle.AS_CONFIGURED;


    // //////////////////////////////////////

    /**
     * If the action returns its target, then determines whether to update the page or
     * instead to redirect (forcing a re-rendering of a new page).
     */
    Redirect redirectPolicy() default Redirect.AS_CONFIGURED;

    // //////////////////////////////////////

    /**
     * For actions of domain services that can be viewed and contributed (that is, whose
     * {@link DomainService#nature() nature} is either {@link org.apache.isis.applib.annotation.NatureOfService#VIEW}
     * or {@link org.apache.isis.applib.annotation.NatureOfService#VIEW_CONTRIBUTIONS_ONLY}), specifies how the
     * contribution should be implemented, as an action, as an association, or as both.
     *
     * <p>
     *     Has no meaning for actions of domain entities.
     * </p>
     */
    Contributed contributed() default Contributed.AS_BOTH;


}
