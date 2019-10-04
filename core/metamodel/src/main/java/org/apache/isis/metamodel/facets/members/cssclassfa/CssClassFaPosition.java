/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.metamodel.facets.members.cssclassfa;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.ViewModelLayout;

/**
 * Generalizes 
 * {@link org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition}, 
 * {@link org.apache.isis.applib.annotation.DomainObjectLayout.CssClassFaPosition} and 
 * {@link org.apache.isis.applib.annotation.ViewModelLayout.CssClassFaPosition}.
 *
 * <p>
 *     This isn't part of the applib only because didn't want to break backward 
 *     compatibility of the existing {@link org.apache.isis.applib.annotation.ActionLayout} 
 *     annotation.
 * </p>
 */
public enum CssClassFaPosition {
    LEFT, RIGHT;

    @Deprecated
    public static CssClassFaPosition from(final DomainObjectLayout.CssClassFaPosition cssClassFaPosition) {
        if(cssClassFaPosition == null) {
            return null;
        }
        switch (cssClassFaPosition) {
        case LEFT:
            return LEFT;
        case RIGHT:
            return RIGHT;
        }
        throw new IllegalArgumentException("not recognized: " + cssClassFaPosition);
    }

    @Deprecated
    public static CssClassFaPosition from(final ViewModelLayout.CssClassFaPosition cssClassFaPosition) {
        if(cssClassFaPosition == null) {
            return null;
        }
        switch (cssClassFaPosition) {
        case LEFT:
            return LEFT;
        case RIGHT:
            return RIGHT;
        }
        throw new IllegalArgumentException("not recognized: " + cssClassFaPosition);
    }

    @Deprecated
    public static CssClassFaPosition from(final ActionLayout.CssClassFaPosition cssClassFaPosition) {
        if(cssClassFaPosition == null) {
            return null;
        }
        switch (cssClassFaPosition) {
        case LEFT:
            return LEFT;
        case RIGHT:
            return RIGHT;
        }
        throw new IllegalArgumentException("not recognized: " + cssClassFaPosition);
    }
    @Deprecated
    public DomainObjectLayout.CssClassFaPosition toDomainObjectLayoutPosition() {
        if (this == CssClassFaPosition.LEFT) {
            return DomainObjectLayout.CssClassFaPosition.LEFT;
        } else {
            return DomainObjectLayout.CssClassFaPosition.RIGHT;
        }
    }
    @Deprecated
    public ViewModelLayout.CssClassFaPosition toViewModelLayoutPosition() {
        if (this == CssClassFaPosition.LEFT) {
            return ViewModelLayout.CssClassFaPosition.LEFT;
        } else {
            return ViewModelLayout.CssClassFaPosition.RIGHT;
        }
    }
    @Deprecated
    public ActionLayout.CssClassFaPosition toActionLayoutPosition() {
        if (this == CssClassFaPosition.LEFT) {
            return ActionLayout.CssClassFaPosition.LEFT;
        } else {
            return ActionLayout.CssClassFaPosition.RIGHT;
        }
    }
}
