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

package org.apache.isis.viewer.wicket.model.models;

import java.util.Comparator;

import org.apache.isis.applib.services.bookmark.Oid;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

final class BookmarkTreeNodeComparator implements Comparator<BookmarkTreeNode> {

    private final SpecificationLoader specificationLoader;

    public BookmarkTreeNodeComparator(final SpecificationLoader specificationLoader){
        this.specificationLoader = specificationLoader;
    }

    @Override
    public int compare(BookmarkTreeNode o1, BookmarkTreeNode o2) {

        final Oid oid1 = o1.getOidNoVer();
        final Oid oid2 = o2.getOidNoVer();

        // sort by entity type
        final String className1 = classNameOf(oid1);
        final String className2 = classNameOf(oid2);

        final int classNameComparison = className1.compareTo(className2);
        if(classNameComparison != 0) {
            return classNameComparison;
        }

        final String title1 = o1.getTitle();
        final String title2 = o2.getTitle();

        return title1.compareTo(title2);
    }

    private String classNameOf(Oid oid) {
        return specificationLoader.specForLogicalTypeNameElseFail(oid.getLogicalTypeName())
                .getFeatureIdentifier().getClassName();
    }

}
