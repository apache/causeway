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

import java.util.Hashtable;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class TypeBasedColumnWidthStrategy implements ColumnWidthStrategy {
    private final Hashtable<ObjectSpecification, Integer> types = new Hashtable<ObjectSpecification, Integer>();

    public TypeBasedColumnWidthStrategy() {
        /*
         * ObjectSpecificationLoader loader = Isis.getSpecificationLoader();
         * addWidth(loader.loadSpecification(Logical.class), 25);
         * addWidth(loader.loadSpecification(Date.class), 65);
         * addWidth(loader.loadSpecification(Time.class), 38);
         * addWidth(loader.loadSpecification(DateTime.class), 100);
         * addWidth(loader.loadSpecification(TextString.class), 80);
         */
    }

    public void addWidth(final ObjectSpecification specification, final int width) {
        types.put(specification, new Integer(width));
    }

    @Override
    public int getMaximumWidth(final int i, final ObjectAssociation specification) {
        return 0;
    }

    @Override
    public int getMinimumWidth(final int i, final ObjectAssociation specification) {
        return 15;
    }

    // TODO improve the width determination
    @Override
    public int getPreferredWidth(final int i, final ObjectAssociation specification) {
        final ObjectSpecification type = specification.getSpecification();
        if (type == null) {
            return 200;
        }
        final Integer t = types.get(type);
        if (t != null) {
            return t.intValue();
        } else if (type.isNotCollection()) {
            return 120;
        } else {
            return 100;
        }
    }
}
