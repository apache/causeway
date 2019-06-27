/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.unittestsupport.comparable;

import java.util.List;

import org.apache.isis.unittestsupport.comparable.ComparableContractTest_compareTo;

public class CategorizedDomainObjectComparableContractTest_compareTo extends ComparableContractTest_compareTo<CategorizedDomainObject> {

    @SuppressWarnings("unchecked")
    @Override
    protected List<List<CategorizedDomainObject>> orderedTuples() {
        return listOf(
                listOf(
                        newObject(null, null),
                        newObject(1, null),
                        newObject(1, null),
                        newObject(2, null)
                        ),
                listOf(
                        newObject(1, null),
                        newObject(1, 1),
                        newObject(1, 1),
                        newObject(1, 2)
                        )
                );
    }

    private CategorizedDomainObject newObject(Integer category, Integer subcategory) {
        final CategorizedDomainObject obj = new CategorizedDomainObject();
        obj.setCategory(category);
        obj.setSubcategory(subcategory);
        return obj;
    }

}
