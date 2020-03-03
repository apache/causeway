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
 * How prompting for new values of a property/arguments for an action should be performed
 */
// tag::refguide[]
@XmlType(
        namespace = "http://isis.apache.org/applib/layout/component"
        )
public enum PromptStyle {

    // end::refguide[]
    /**
     * Prompt using the style configured by <tt>isis.viewer.wicket.promptStyle</tt>.
     *
     * <p>
     *      If no style is configured, then {@link #INLINE} is assumed.
     * </p>
     */
    // tag::refguide[]
    AS_CONFIGURED,

    // end::refguide[]
    /**
     * Use a dialog for the prompt.
     *
     * <p>
     *     This will be either modal dialog (same as if {@link #DIALOG_MODAL} was selected) or sidebar (same as if
     *     {@link #DIALOG_SIDEBAR} was selected) depending on the value of the
     *     <code>isis.viweer.wicket.dialogMode</code> configuration property.
     * </p>
     */
    // tag::refguide[]
    DIALOG,

    // end::refguide[]
    /**
     * Use a dialog for the prompt, rendered in a sidebar.
     */
    // tag::refguide[]
    DIALOG_SIDEBAR,

    // end::refguide[]
    /**
     * Use a dialog for the prompt, rendered in a modal dialog.
     */
    // tag::refguide[]
    DIALOG_MODAL,

    // end::refguide[]
    /**
     * Show the form inline, temporarily replacing the rendering of the property.
     */
    // tag::refguide[]
    INLINE,

    // end::refguide[]
    /**
     * Applies only to actions, show the form inline, invoked as if editing the property.
     *
     * <p>Note that:
     * <ul>
     * <li>
     *     Only one such action should have this attribute set per property.  If there are multiple actions, then
     *     the first one discovered with the attribute is used.
     * </li>
     * <li>
     *     If the property is editable, then this attribute is ignored (and the action is treated as having a prompt style of {@link #INLINE}).
     * </li>
     * <li>
     *     If applied to a property, then is the property's prompt style is simply treated as {@link #INLINE}.
     * </li>
     * </ul>
     * </p>
     *
     */
    // tag::refguide[]
    INLINE_AS_IF_EDIT,

    // end::refguide[]
    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or superclasses/interfaces).
     */
    // tag::refguide[]
    NOT_SPECIFIED;

    // end::refguide[]
    public boolean isDialog() { return this == DIALOG || this == DIALOG_MODAL || this == DIALOG_SIDEBAR; }
    public boolean isInline() { return this == INLINE; }
    public boolean isInlineAsIfEdit() { return this == INLINE_AS_IF_EDIT; }
    public boolean isInlineOrInlineAsIfEdit() { return this == INLINE || this == INLINE_AS_IF_EDIT; }
    // tag::refguide[]

}
// end::refguide[]
