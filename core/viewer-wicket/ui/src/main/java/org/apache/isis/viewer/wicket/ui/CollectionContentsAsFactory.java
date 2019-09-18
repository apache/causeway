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
package org.apache.isis.viewer.wicket.ui;

import org.apache.wicket.model.IModel;

/**
 * <p>
 * An interface for all {@link org.apache.isis.viewer.wicket.ui.ComponentFactory component factories}
 * (e.g. CollectionContentAsXyzFactory-ies) which want to provide specific title and CSS class(es)
 * for their representation in {@link org.apache.isis.viewer.wicket.ui.components.collectioncontents.multiple.CollectionContentsMultipleViewsPanel}.
 * </p>
 * <p>
 * If the {@link org.apache.isis.viewer.wicket.ui.ComponentFactory} doesn't implement this interface or the implementation
 * of any of its methods return {@code null} then {@link ComponentFactory#getName()} will be used as title and its
 * {@link org.apache.isis.metamodel.commons.StringExtensions#asLowerDashed(java.lang.String) dashed representation}
 * as CSS class for the optional image.
 * </p>
 */
public interface CollectionContentsAsFactory {

    /**
     * @return A model that will be used as a label for the "View as" dropdown for "collection contents as"
     * component factories
     */
    IModel<String> getTitleLabel();

    /**
     * @return A model that will be used as a CSS class for the icon/image next to "View as" dropdown
     * for "collection contents as" component factories
     */
    IModel<String> getCssClass();
}
