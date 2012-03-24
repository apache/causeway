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

package org.apache.isis.viewer.dnd.table;

import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class DefaultColumnWidthStrategy implements ColumnWidthStrategy {

    private final int minimum;
    private final int preferred;
    private final int maximum;

    public DefaultColumnWidthStrategy() {
        this(18, 70, 250);
    }

    public DefaultColumnWidthStrategy(final int minimum, final int preferred, final int maximum) {
        if (minimum <= 0) {
            throw new IllegalArgumentException("minimum width must be greater than zero");
        }
        if (preferred <= minimum || preferred >= maximum) {
            throw new IllegalArgumentException("preferred width must be greater than minimum and less than maximum");
        }
        this.minimum = minimum;
        this.preferred = preferred;
        this.maximum = maximum;
    }

    @Override
    public int getMinimumWidth(final int i, final ObjectAssociation specification) {
        return minimum;
    }

    @Override
    public int getPreferredWidth(final int i, final ObjectAssociation specification) {
        return preferred;
    }

    @Override
    public int getMaximumWidth(final int i, final ObjectAssociation specification) {
        return maximum;
    }
}
