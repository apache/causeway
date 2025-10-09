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
package org.apache.causeway.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Collects together all layout hints for a property of a domain object.
 *
 * @see ActionLayout
 * @see CollectionLayout
 * @see DomainObjectLayout
 * @see Property
 *
 * @since 1.x {@index}
 * @see org.apache.causeway.applib.annotation.ParameterLayout
 */
@Inherited
@Target({
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.RECORD_COMPONENT,
    ElementType.TYPE,
    ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DomainObject(nature=Nature.MIXIN, mixinMethod = "prop") // meta annotation, only applies at class level
@Domain.Include // meta annotation, in support of meta-model validation
public @interface PropertyLayout {

    /**
     * Indicates the css class that a property should have, to
     * allow more targeted styling in <code>application.css</code>.
     *
     * @see ActionLayout#cssClass()
     * @see ParameterLayout#cssClass()
     * @see CollectionLayout#cssClass()
     * @see DomainObjectLayout#cssClass()
     */
    String cssClass()
            default "";

    /**
     * Description of this property, eg to be rendered in a tooltip.
     *
     * @see ActionLayout#describedAs()
     * @see ParameterLayout#describedAs()
     * @see CollectionLayout#describedAs()
     * @see DomainObjectLayout#describedAs()
     */
    String describedAs()
            default "";

    /**
     * Specifies the <b>id</b> of associated <i>FieldSet</i>.
     *
     * <p>
     * A <i>FieldSet</i> is a layout component for property grouping, that can either be specified via
     * a <code>Xxx.layout.xml</code> file (with <code>Xxx</code> the domain object name) or is
     * inferred by the framework via annotations (aka the programming model).
     * <i>FieldSet</i>s are represented in-memory and requires a framework internal unique id per domain
     * object type.
     * </p>
     * <p>
     * Following 2 scenarios have slightly different behavior:
     * </p>
     *
     * <h1>XML layout is present</h1>
     * <p>
     * When a XML layout is present, every <i>FieldSet</i> <b>id</b> is either explicitly specified in
     * the file or may be inferred from a non-empty <b>name</b>.
     * If the <b>name</b> is empty "" or missing, then the <b>id</b> within the file is mandatory.
     * </p><p>
     * If this <i>Property</i> is not already explicitly listed within the XML layout, we lookup the
     * associated <i>FieldSet</i> in the XML layout file first matching by <b>id</b>
     * using {@code @PropertyLayout(fieldSetId=...)} if any, then falling back to matching by (friendly)
     * <b>name</b> using @PropertyLayout(fieldSetName=...)} if any.
     * </p>
     *
     * <h1>XML layout is absent</h1>
     * <p>
     * We reify (in-memory) the associated <i>FieldSet</i> using {@code @PropertyLayout(fieldSetId=...)}
     * (if present) as its <b>id</b> and using {@code @PropertyLayout(fieldSetId=...)} as its (friendly)
     * <b>name</b>.
     * While if no <b>id</b> is provided an <b>id</b> is inferred from the (friendly) <b>name</b>, in which
     * case the (friendly) <b>name</b> must not be empty.
     * Whereas if no (friendly) <b>name</b> is provided a (friendly) <b>name</b> is inferred from the
     * <b>id</b>, in which case the <b>id</b> must not be empty.
     * </p><p>
     * With {@code @PropertyLayout(sequence=...)} the relative position within that <i>FieldSet</i> can be
     * specified.
     * </p>
     *
     * @see Action#choicesFrom()
     * @see ActionLayout#fieldSetId()
     * @see ActionLayout#fieldSetName()
     * @see PropertyLayout#fieldSetName()
     * @see PropertyLayout#sequence()
     */
    String fieldSetId()
            default "__infer";

    /**
     * Specifies the <b>friendly-name</b> of associated <i>FieldSet</i>.
     * <p>
     * Explicitly specifying an empty "" <b>friendly-name</b> will suppress the <i>FieldSet</i>'s label
     * from being rendered.
     * </p>
     * <p>
     * For a more in depth description see {@link PropertyLayout#fieldSetId()}.
     * </p>
     *
     * @see Action#choicesFrom()
     * @see ActionLayout#fieldSetId()
     * @see ActionLayout#fieldSetName()
     * @see PropertyLayout#fieldSetId()
     * @see PropertyLayout#sequence()
     */
    String fieldSetName()
            default "__infer";

    /**
     * Indicates where in the UI the property
     * should <i>not</i> be visible.
     *
     * @see ActionLayout#hidden()
     * @see CollectionLayout#hidden()
     */
    Where hidden()
            default Where.NOT_SPECIFIED;

    /**
     * In forms, positioning of the label (left, top or none) relative to the property value.
     *
     * <p>
     * If not specified, the default depends upon the property value's
     * datatype (including whether the field is {@link #multiLine()}.
     * </p>
     *
     * @see #multiLine()
     * @see ParameterLayout#labelPosition()
     */
    LabelPosition labelPosition()
            default LabelPosition.NOT_SPECIFIED;

    /**
     * For string properties (and parameters), render as a text area over specified number of lines.
     *
     * <p>
     *     If set to &gt; 1, then {@link #labelPosition()} defaults to
     *     {@link LabelPosition#TOP top}.
     * </p>
     *
     * @see ParameterLayout#multiLine()
     */
    int multiLine()
            default -1;

    /**
     * Name of this property (overriding the name derived from its name in code).
     *
     * <p>
     * A typical use case is if the desired name is a reserved Java keyword, such as `default` or `package`.
     * </p>
     *
     * @see ActionLayout#named()
     * @see ParameterLayout#named()
     * @see CollectionLayout#named()
     * @see DomainObjectLayout#named()
     * @see DomainServiceLayout#named()
     */
    String named()
            default "";

    /**
     * Whether this property should be used to construct the navigable chain of breadcrumbs in the UI.
     *
     * <p>
     *     Only one property can be annotated as such per domain class.
     * </p>
     */
    Navigable navigable()
            default Navigable.NOT_SPECIFIED;

    /**
     * When set, identifies a logical child, that is navigable via the UI.
     * <p>
     * The order of appearance of this tree branch in the UI relative to other branches of the same tree node,
     * is given in <i>Dewey-decimal</i> notation.
     *
     * @see CollectionLayout#navigableSubtree()
     */
    String navigableSubtree()
        default "";

    /**
     * How the properties of this domain object are be edited, either {@link PromptStyle#DIALOG dialog} or {@link PromptStyle#INLINE inline}.
     */
    PromptStyle promptStyle()
            default PromptStyle.NOT_SPECIFIED;

    /**
     * Indicates that the value held by the property never changes over time, even when other properties of the object
     * do change.
     *
     * <p>
     *     Setting this attribute to <tt>RepaintPolicy.NO_REPAINT</tt> is used as a hint to the viewer to not repaint
     *     the property after an AJAX update of some other property/ies of the object have changed.
     *     This is primarily for performance, eg can improve the user experience when rendering PDFs/blobs.
     * </p>
     *
     * <p>
     *     Note that for this to work, the viewer will also ensure that none of the property's parent component
     *     (such as a tab group panel) are re-rendered.
     * </p>
     *
     * <p>
     *     Design note: we considered implementing this an "immutable" flag on the {@link Property } annotation
     *     (because this flag is typically appropriate for immutable/unchanging properties of a domain object).
     *     However, we decided not to do that, on the basis that it might be interpreted as having a deeper impact
     *     within the framework than simply a hint for rendering.
     * </p>
     */
    Repainting repainting()
            default Repainting.NOT_SPECIFIED;

    /**
     * The order of this member relative to other members in the same (layout) group,
     * given in <i>Dewey-decimal</i> notation.
     * <p>
     * Also governs slot-in order for the layout group that collects all unreferenced <i>Properties</i>,
     * unless overwritten via application scoped config option that enforced alphabetical order:
     * <br>
     * {@code causeway.applib.annotation.propertyLayout.sequencePolicyIfUnreferenced}
     * <p>
     * An alternative is to use the <code>Xxx.layout.xml</code> file,
     * where <code>Xxx</code> is the domain object name.
     * @see ActionLayout#sequence()
     * @see CollectionLayout#sequence()
     */
    String sequence()
            default "";

    /**
     * The typical entry length of a field, use to determine the optimum width
     * for display.
     *
     * <p>
     *     Note: the Wicket viewer does not use this information.
     * </p>
     *
     * @see ParameterLayout#typicalLength()
     */
    int typicalLength()
            default -1;

}
