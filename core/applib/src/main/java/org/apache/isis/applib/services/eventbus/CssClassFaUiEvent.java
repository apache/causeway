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
package org.apache.isis.applib.services.eventbus;

import java.util.EventObject;

import org.apache.isis.applib.annotation.DomainObjectLayout;

/**
 * Emitted for subscribers to obtain a cssClassFa hint (equivalent to the {@link DomainObjectLayout#cssClassFa()} attribute), providing the CSS class for a font-awesome
 * icon for this domain object.
 */
public abstract class CssClassFaUiEvent<S> extends AbstractUiEvent<S> {

    private static final long serialVersionUID = 1L;

    //region > constructors
    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Because the {@link EventObject} superclass prohibits a null source, a dummy value is temporarily used.
     * </p>
     */
    public CssClassFaUiEvent() {
        this(null);
    }

    public CssClassFaUiEvent(final S source) {
        super(source);
    }

    //endregion

    //region > Default class

    /**
     * Propagated if no custom subclass was specified using
     * {@link org.apache.isis.applib.annotation.DomainObjectLayout#iconUiEvent()} annotation attribute.
     */
    public static class Default extends CssClassFaUiEvent<Object> {
        private static final long serialVersionUID = 1L;
    }
    //endregion

    //region > cssClassFa
    private String cssClassFa;

    /**
     * The CSS class for a font-awesome icon for this domain object, as provided by a subscriber using {@link #setCssClassFa(String)}.
     */
    public String getCssClassFa() {
        return cssClassFa;
    }


    /**
     * For subscribers to call to provide a CSS class for a font-awesome icon for this object.
     */
    public void setCssClassFa(final String cssClass) {
        this.cssClassFa = cssClass;
    }
    //endregion

    //region > cssClassFaPosition
    private DomainObjectLayout.CssClassFaPosition cssClassFaPosition;

    /**
     * The {@link DomainObjectLayout.CssClassFaPosition position} as provided by a subscriber using {@link #setCssClassFaPosition(org.apache.isis.applib.annotation.DomainObjectLayout.CssClassFaPosition)}.
     *
     * <p>
     *     This attribute is currently ignored by Isis viewers.
     * </p>
     */
    public DomainObjectLayout.CssClassFaPosition getCssClassFaPosition() {
        return cssClassFaPosition;
    }

    /**
     * For subscribers to call to provide the positioning of the font-awesome icon for this object.
     *
     * <p>
     *     This attribute is currently ignored by Isis viewers.
     * </p>
     */
    public void setCssClassFaPosition(
            final DomainObjectLayout.CssClassFaPosition cssClassFaPosition) {
        this.cssClassFaPosition = cssClassFaPosition;
    }
    //endregion

}