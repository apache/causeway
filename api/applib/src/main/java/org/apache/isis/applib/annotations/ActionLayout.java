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
package org.apache.isis.applib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.layout.component.CssClassFaPosition;

/**
 * Layout hints for actions.
 *
 * @see PropertyLayout
 * @see CollectionLayout
 * @see DomainObjectLayout
 * @see Action
 *
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@DomainObject(nature=Nature.MIXIN, mixinMethod = "act") // meta annotation, only applies at class level
@Domain.Include // meta annotation, in support of meta-model validation
public @interface ActionLayout {

    /**
     * Associates this action with a property or collection, specifying its id.
     *
     * <p>
     *     To specify the layout order use {@link ActionLayout#sequence()}.
     * </p>
     *
     * <p>
     *     For example <code>@ActionLayout(associateWith="items") @ActionLayout(sequence="2.1")</code>
     * </p>
     *
     * <p>
     *     Note that it is also possible to associate an action with a collection using {@link Action#choicesFrom()}
     *     (which has the additional semantic of the rows of the element being used as choices for that action's
     *     collection parameter of the same type as the elements of the collection).
     * </p>
     *
     * @see Action#choicesFrom()
     * @see ActionLayout#sequence()
     * @see ActionLayout#fieldSetId()
     * @see ActionLayout#fieldSetName()
     */
    String associateWith()
            default "";

    /**
     * Whether (and how) this action can be bookmarked in the UI.
     *
     * <p>
     *     For bookmarkable actions, either {@link org.apache.isis.applib.annotations.BookmarkPolicy#AS_ROOT}
     *     and {@link org.apache.isis.applib.annotations.BookmarkPolicy#AS_CHILD} can be used (they are treated
     *     identically).
     * </p>
     */
    BookmarkPolicy bookmarking()
            default BookmarkPolicy.NOT_SPECIFIED;

    /**
     * Indicates the css class that an action should have.
     *
     * <p>
     *     For the Wicket viewer, this can be a bootstrap class such as <code>btn-info</code>.
     * </p>
     *
     * @see PropertyLayout#cssClass()
     * @see ParameterLayout#cssClass()
     * @see CollectionLayout#cssClass()
     * @see DomainObjectLayout#cssClass()
     */
    String cssClass()
            default "";

    /**
     * Indicates the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a> CSS class to decorate an
     * action (button or menu item).
     *
     * @see ActionLayout#cssClassFaPosition()
     * @see DomainObjectLayout#cssClassFa()
     */
    String cssClassFa()
            default "";

    /**
     * Indicates the position of the <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a>
     * icon.
     *
     * <p>
     * The icon could be rendered on the left or the right of the action button
     * </p>
     *
     * @see ActionLayout#cssClassFa()
     */
    CssClassFaPosition cssClassFaPosition()
            default CssClassFaPosition.LEFT;

    /**
     * Description of this action, eg to be rendered in a tooltip.
     *
     * @see PropertyLayout#describedAs()
     * @see ParameterLayout#describedAs()
     * @see CollectionLayout#describedAs()
     * @see DomainObjectLayout#describedAs()
     */
    String describedAs()
            default "";

    /**
     * Specifies the <b>id</b> of associated <i>FieldSet</i>.
     * <p>
     * For a more in depth description see the analogous {@link PropertyLayout#fieldSetId()}.
     * </p>
     *
     * To associate an <i>Action</i> with a <i>Collection</i>, use {@link Action#choicesFrom()}
     * instead.
     *
     * @apiNote An <i>Action</i> can be associated with with a <i>Property</i> or <i>Collection</i>
     * its so called <i>peer</i>.
     * It will then be positioned close to its <i>peer</i>, either under it or on the header panel
     * depending on other layout facets. Such an association is made available via
     * {@link ActionLayout#associateWith()}. However, with the presence of a {@link ActionLayout#fieldSetId()}
     * or a {@link ActionLayout#fieldSetName()}
     * this default placement is overruled.
     *
     * @see Action#choicesFrom()
     * @see ActionLayout#associateWith()
     * @see ActionLayout#fieldSetName()
     * @see PropertyLayout#fieldSetId()
     * @see PropertyLayout#fieldSetName()
     * @see PropertyLayout#sequence()
     */
    String fieldSetId()
            default "__infer";

    /**
     * Specifies the <b>friendly-name</b> of associated <i>FieldSet</i> or <i>Collection</i>.
     * <p>
     * For a more in depth description see the analogous {@link PropertyLayout#fieldSetId()};
     * </p>
     *
     * To associate an <i>Action</i> with a <i>Collection</i>, use {@link Action#choicesFrom()}
     * instead.
     *
     * @see Action#choicesFrom()
     * @see ActionLayout#associateWith()
     * @see ActionLayout#fieldSetId()
     * @see PropertyLayout#fieldSetId()
     * @see PropertyLayout#fieldSetName()
     * @see PropertyLayout#sequence()
     */
    String fieldSetName()
            default "__infer";

    /**
     * Indicates where in the UI the action should <i>not</i>not be visible.
     */
    Where hidden()
            default Where.NOT_SPECIFIED;

    /**
     * Name of this action (overriding the name derived from its name in code).
     *
     * <p>
     * A typical use case is if the desired name is a reserved Java keyword, such as `default` or `package`.
     * </p>
     *
     * @see PropertyLayout#named()
     * @see ParameterLayout#named()
     * @see CollectionLayout#named()
     * @see DomainObjectLayout#named()
     * @see DomainServiceLayout#named()
     */
    String named()
            default "";

    /**
     * For actions that are associated with a property (using either
     * {@link Action#choicesFrom()} or {@link ActionLayout#fieldSetId()}
     * or {@link ActionLayout#fieldSetName()},
     * indicates the positioning of the action's button relative to the
     * property.
     *
     * <p>
     * Ignored if the action has not been associated with a property.
     * </p>
     */
    Position position()
            default Position.NOT_SPECIFIED;

    @XmlType(namespace = "http://isis.apache.org/applib/layout/component")
    enum Position {
        BELOW,
        RIGHT,
        PANEL,
        PANEL_DROPDOWN,
        NOT_SPECIFIED
    }

    /**
     * How the parameters for this action are prompted, either {@link PromptStyle#DIALOG dialog} or
     * {@link PromptStyle#INLINE inline}.
     */
    PromptStyle promptStyle()
            default PromptStyle.AS_CONFIGURED;

    /**
     * If the action returns its target, then determines whether to update the
     * page or instead to redirect (forcing a re-rendering of a new page).
     *
     * <p>
     *     Not re-rendering can provide a smoother UI experience.
     * </p>
     *
     * <p>
     *     Supported by the Wicket viewer.
     * </p>
     */
    Redirect redirectPolicy()
            default Redirect.AS_CONFIGURED;

    /**
     * The order of this member relative to other members in the same (layout) group,
     * given in <i>Dewey-decimal</i> notation.
     * <p>
     *     An alternative is to use the <code>Xxx.layout.xml</code> file,
     *     where <code>Xxx</code> is the domain object name.
     * </p>
     * @see CollectionLayout#sequence()
     * @see PropertyLayout#sequence()
     */
    String sequence()
            default "";

}
