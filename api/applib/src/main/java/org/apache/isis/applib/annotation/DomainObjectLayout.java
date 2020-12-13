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
 * 
 * 
 * @since 1.x {@index}
 */
// tag::refguide[]
@Inherited
@Target({
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
// tag::refguide-ui-events[]
public @interface DomainObjectLayout {

    // end::refguide[]
    // ...
    // end::refguide-ui-events[]
    /**
     * Whether (and how) this domain object can be bookmarked in the UI.
     */
    // tag::refguide[]
    BookmarkPolicy bookmarking()                    // <.>
            default BookmarkPolicy.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * Indicates the css class that a domain class (type) should have.
     */
    // tag::refguide[]
    String cssClass()                               // <.>
            default "";

    // end::refguide[]
    /**
     * Indicates the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a> CSS class to decorate an
     * domain object.
     */
    // tag::refguide[]
    String cssClassFa()                             // <.>
            default "";

    // end::refguide[]
    /**
     * Indicates the position of the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a>
     * icon. The icon could be rendered on the left or the right of the object's title.
     *
     * <p>
     *     This attribute is currently ignored by Isis viewers.
     * </p>
     */
    // tag::refguide[]
    CssClassFaPosition cssClassFaPosition()         // <.>
            default CssClassFaPosition.LEFT;

    // end::refguide[]
    /**
     * Description of this class, eg to be rendered in a tooltip.
     */
    // tag::refguide[]
    String describedAs()                            // <.>
            default "";

    // end::refguide[]
    /**
     * Name of this class (overriding the name derived from its name in code).
     */
    // tag::refguide[]
    String named()                                  // <.>
            default "";

    // end::refguide[]
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
    // tag::refguide[]
    int paged()                                     // <.>
            default -1;

    // end::refguide[]
    /**
     * The plural name of the class.
     */
    // tag::refguide[]
    String plural()                                 // <.>
            default "";

    // end::refguide[]
    /**
     * Which subclass of {@link CssClassUiEvent} should be used to obtain a CSS class.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-ui-events[]
    Class<? extends CssClassUiEvent<?>>
            cssClassUiEvent()                       // <.>
            default CssClassUiEvent.Default.class;

    // end::refguide-ui-events[]
    /**
     * Which subclass of {@link IconUiEvent} should be used to obtain an icon.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-ui-events[]
    Class<? extends IconUiEvent<?>>
            iconUiEvent()                           // <.>
            default IconUiEvent.Default.class;

    // end::refguide-ui-events[]
    /**
     * Which subclass of {@link LayoutUiEvent} should be used to obtain a layout.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-ui-events[]
    Class<? extends LayoutUiEvent<?>>
            layoutUiEvent()                         // <.>
            default LayoutUiEvent.Default.class;

    // end::refguide-ui-events[]
    /**
     * Which subclass of {@link TitleUiEvent} should be used to obtain a title.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-ui-events[]
    Class<? extends TitleUiEvent<?>>
            titleUiEvent()                          // <.>
            default TitleUiEvent.Default.class;

    // end::refguide-ui-events[]

    // tag::refguide[]
    // ...
    // tag::refguide-ui-events[]
}
// end::refguide[]
// end::refguide-ui-events[]
