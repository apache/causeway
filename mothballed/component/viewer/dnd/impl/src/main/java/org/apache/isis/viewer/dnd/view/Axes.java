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

package org.apache.isis.viewer.dnd.view;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.viewer.dnd.util.ViewerException;

public class Axes {
    private static final Logger LOG = LoggerFactory.getLogger(Axes.class);
    private final Map<Class<?>, ViewAxis> axes = new HashMap<Class<?>, ViewAxis>();

    public void add(final ViewAxis axis) {
        if (axis != null) {
            final Class<? extends ViewAxis> cls = axis.getClass();
            add(axis, cls);
        }
    }

    public void add(final ViewAxis axis, final Class<? extends ViewAxis> cls) {
        final ViewAxis previous = axes.put(cls, axis);
        if (previous != null) {
            LOG.debug(axis + " replacing " + previous);
        } else {
            LOG.debug("adding " + axis);
        }
    }

    public <T extends ViewAxis> T getAxis(final Class<T> axisClass) {
        final ViewAxis viewAxis = axes.get(axisClass);
        if (viewAxis == null) {
            throw new ViewerException("No axis of type " + axisClass + " in " + this);
        }
        return (T) viewAxis;
    }

    public boolean contains(final Class<? extends ViewAxis> axisClass) {
        return axes.containsKey(axisClass);
    }

    public void add(final Axes axes) {
        this.axes.putAll(axes.axes);
    }

    @Override
    public String toString() {
        // TODO provide flag to list as elements, rather than fields
        final ToString s = new ToString(this);
        for (final ViewAxis axis : axes.values()) {
            s.append(axis.toString());
        }
        return s.toString();
    }
}
