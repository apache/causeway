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
package org.apache.isis.viewer.wicket.model.links;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public class LinkAndLabel implements Serializable {

    private static final long serialVersionUID = 1L;

    public static List<LinkAndLabel> positioned(
            final List<LinkAndLabel> entityActionLinks,
            final ActionLayout.Position position) {
        return _Lists.filter(entityActionLinks, Predicates.positioned(position));
    }

    public static LinkAndLabel newLinkAndLabel(
            final ManagedObject objectAdapter,
            final ObjectAction objectAction,
            final AbstractLink link,
            final boolean blobOrClob) {

        final String name = ObjectAction.Util.nameFor(objectAction);

        final boolean isPrototype = objectAction.isPrototype();
        final String actionIdentifier = ObjectAction.Util.actionIdentifierFor(objectAction);
        final String description = ObjectAction.Util.descriptionOf(objectAction);
        final String cssClass = ObjectAction.Util.cssClassFor(objectAction, objectAdapter);
        final String cssClassFa = ObjectAction.Util.cssClassFaFor(objectAction);
        final CssClassFaPosition cssClassFaPosition = ObjectAction.Util.cssClassFaPositionFor(objectAction);
        final ActionLayout.Position actionLayoutPosition = ObjectAction.Util.actionLayoutPositionOf(objectAction);
        final PromptStyle promptStyle = ObjectAction.Util.promptStyleFor(objectAction);
        final SemanticsOf semantics = objectAction.getSemantics();

        return new LinkAndLabel(
                link, name,
                description,
                blobOrClob, isPrototype,
                actionIdentifier,
                cssClass, cssClassFa, cssClassFaPosition, actionLayoutPosition,
                semantics,
                promptStyle,
                Parameters.fromParameterCount(objectAction.getParameterCount()));
    }

    public enum Parameters {
        NO_PARAMETERS,
        TAKES_PARAMETERS;

        public static Parameters fromParameterCount(final int parameterCount) {
            return parameterCount > 0? TAKES_PARAMETERS: NO_PARAMETERS;
        }

        public boolean isNoParameters() {
            return this == NO_PARAMETERS;
        }
    }

    private final AbstractLink link;
    private final String label;
    private final String descriptionIfAny;
    private final boolean blobOrClob;
    private final boolean prototype;
    private final String actionIdentifier;
    private final String cssClass;
    private final String cssClassFa;
    private final CssClassFaPosition cssClassFaPosition;
    private final ActionLayout.Position position;
    private final SemanticsOf semanticsOf;
    private final PromptStyle promptStyle;
    private Parameters parameters;

    private LinkAndLabel(
            final AbstractLink link,
            final String label,
            final String descriptionIfAny,
            final boolean blobOrClob,
            final boolean prototype,
            final String identifier,
            final String cssClass,
            final String cssClassFa,
            final CssClassFaPosition cssClassFaPosition,
            final ActionLayout.Position position,
            final SemanticsOf semanticsOf,
            final PromptStyle promptStyle,
            final Parameters parameters) {
        this.link = link;
        this.label = label;
        this.descriptionIfAny = descriptionIfAny;
        this.blobOrClob = blobOrClob;
        this.prototype = prototype;
        this.actionIdentifier = identifier;
        this.cssClass = cssClass;
        this.cssClassFa = cssClassFa;
        this.cssClassFaPosition = cssClassFaPosition;
        this.position = position;
        this.semanticsOf = semanticsOf;
        this.promptStyle = promptStyle;
        this.parameters = parameters;
    }

    public AbstractLink getLink() {
        return link;
    }

    public String getLabel() {
        return label;
    }

    public String getDescriptionIfAny() {
        return descriptionIfAny;
    }

    public boolean isBlobOrClob() {
        return blobOrClob;
    }

    public boolean isPrototype() {
        return prototype;
    }

    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getCssClassFa() {
        return cssClassFa;
    }

    public CssClassFaPosition getCssClassFaPosition() {
        return cssClassFaPosition;
    }

    public ActionLayout.Position getPosition() {
        return position;
    }

    public SemanticsOf getSemantics() {
        return semanticsOf;
    }

    public PromptStyle getPromptStyle() {
        return promptStyle;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public static class Predicates {
        public static Predicate<LinkAndLabel> positioned(final ActionLayout.Position position) {
            return (LinkAndLabel input) ->  input.getPosition() == position;
        }
    }

}
