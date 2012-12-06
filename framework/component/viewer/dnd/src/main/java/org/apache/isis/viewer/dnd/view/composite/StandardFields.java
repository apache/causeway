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

package org.apache.isis.viewer.dnd.view.composite;

import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.GlobalViewFactory;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.ViewRequirement;

/**
 * A view factory for the components of a container
 */
public class StandardFields implements ViewFactory {

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        final GlobalViewFactory factory = Toolkit.getViewFactory();

        int requirement = 0;
        if (content.isObject()) {
            requirement = objectRequirement();
        } else if (content.isTextParseable()) {
            requirement = textParseableRequirement();
        } else if (content.isCollection()) {
            requirement = collectionRequirement();
        } else {
            throw new UnknownTypeException(content);
        }

        if (requirement != 0 && include(content, sequence)) {
            final ViewRequirement viewRequirement = new ViewRequirement(content, requirement);
            return factory.createView(viewRequirement);
        } else {
            return null;
        }
    }

    protected boolean include(final Content content, final int sequence) {
        return true;
    }

    protected int objectRequirement() {
        return ViewRequirement.CLOSED | ViewRequirement.SUBVIEW;
    }

    protected int textParseableRequirement() {
        return ViewRequirement.CLOSED | ViewRequirement.SUBVIEW;
    }

    protected int collectionRequirement() {
        return ViewRequirement.CLOSED | ViewRequirement.SUBVIEW;
    }
}
