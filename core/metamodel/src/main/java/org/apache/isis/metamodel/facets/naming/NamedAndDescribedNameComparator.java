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


package org.apache.isis.metamodel.facets.naming;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.isis.core.metamodel.spec.NamedAndDescribed;


/**
 * Compares {@link NamedAndDescribed}s by name.
 */
public class NamedAndDescribedNameComparator implements Comparator<NamedAndDescribed>, Serializable {

    private static final long serialVersionUID = 1L;

    public int compare(final NamedAndDescribed o1, final NamedAndDescribed o2) {

        final String name1 = o1.getName();
        final String name2 = o2.getName();

        if (name1 == null && name2 == null) {
            return 0;
        }
        if (name1 == null && name2 != null) {
            return -1;
        }
        if (name1 != null && name2 == null) {
            return +1;
        }
        if (name1 != null && name2 != null) {
            return name1.compareTo(name2);
        }
        return 0;
    }

}
