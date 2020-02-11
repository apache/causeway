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

import org.apache.isis.applib.events.ui.CssClassUiEvent;
import org.apache.isis.applib.events.ui.IconUiEvent;
import org.apache.isis.applib.events.ui.LayoutUiEvent;
import org.apache.isis.applib.events.ui.TitleUiEvent;
import org.apache.isis.applib.layout.component.CssClassFaPosition;

/**
 * Layout hints for domain objects.
 */
@Inherited
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainObjectLayout {


    /**
     * Which subclass of {@link TitleUiEvent} should be used to obtain a title.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends TitleUiEvent<?>> titleUiEvent() default TitleUiEvent.Default.class;

    // //////////////////////////////////////

    /**
     * Which subclass of {@link IconUiEvent} should be used to obtain an icon.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends IconUiEvent<?>> iconUiEvent() default IconUiEvent.Default.class;

    // //////////////////////////////////////

    /**
     * Which subclass of {@link CssClassUiEvent} should be used to obtain a CSS class.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends CssClassUiEvent<?>> cssClassUiEvent() default CssClassUiEvent.Default.class;

    // //////////////////////////////////////

    /**
     * Which subclass of {@link LayoutUiEvent} should be used to obtain a layout.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends LayoutUiEvent<?>> layoutUiEvent() default LayoutUiEvent.Default.class;

    /**
     * Indicates the css class that a domain class (type) should have.
     */
    String cssClass() default "";

    // //////////////////////////////////////

    /**
     * Indicates the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a> CSS class to decorate an
     * domain object.
     */
    String cssClassFa() default "";

    /**
     * Indicates the position of the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a>
     * icon. The icon could be rendered on the left or the right of the object's title.
     *
     * <p>
     *     This attribute is currently ignored by Isis viewers.
     * </p>
     */
    CssClassFaPosition cssClassFaPosition() default CssClassFaPosition.LEFT;


    // //////////////////////////////////////

    /**
     * Description of this class, eg to be rendered in a tooltip.
     */
    String describedAs() default "";


    // //////////////////////////////////////

    /**
     * Name of this class (overriding the name derived from its name in code).
     */
    String named() default "";


    // //////////////////////////////////////

    /**
     * The page size for instances of this class when rendered within
     * a table.
     *
     * <p>
     * If annotated on a collection, then the page size refers to
     * parented collections (eg <tt>Order#lineItems</tt>).
     *
     * <p>
     * If annotated on a type, then the page size refers to standalone
     * collections (eg as returned from a repository query).
     */
    int paged() default -1;


    // //////////////////////////////////////

    /**
     * The plural name of the class.
     */
    String plural() default "";

    // //////////////////////////////////////

    /**
     * Whether (and how) this domain object can be bookmarked in the UI.
     */
    BookmarkPolicy bookmarking() default BookmarkPolicy.NOT_SPECIFIED;

}