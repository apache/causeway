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

package org.apache.isis.viewer.dnd.icon;

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.BlankView;

public class IconElementFactory implements ViewFactory {
    private final ViewSpecification objectSpec;
    private final ViewSpecification collectionSpec;
    private final boolean createViewsForEmptyContent;

    public IconElementFactory() {
        this(null, null, false);
    }

    public IconElementFactory(final ViewSpecification objectSpec, final ViewSpecification collectionSpec, final boolean createViewsForEmptyContent) {
        this.objectSpec = this.objectSpec == null ? new SubviewIconSpecification() : objectSpec;
        this.collectionSpec = this.collectionSpec == null ? new SubviewIconSpecification() : collectionSpec;
        this.createViewsForEmptyContent = createViewsForEmptyContent;
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        if (content.isObject()) {
            if (content.getAdapter() == null) {
                return createViewsForEmptyContent ? new BlankView(content) : null;
            } else {
                return objectSpec.createView(content, axes, -1);
            }
        } else if (content.isCollection()) {
            return collectionSpec.createView(content, axes, -1);
        } else {
            // TODO decide what to do with values: use factory, use another
            // SubviewSpec, or ignore always
            return null;
        }
    }
}
