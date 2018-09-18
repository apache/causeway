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
package org.apache.isis.core.unittestsupport.comparable;

import com.google.common.collect.Ordering;


public class CategorizedDomainObject implements Comparable<CategorizedDomainObject> {

    private Integer category;

    public Integer getCategory() {
        return category;
    }

    public void setCategory(final Integer category) {
        this.category = category;
    }



    private Integer subcategory;

    public Integer getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(final Integer subcategory) {
        this.subcategory = subcategory;
    }



    @Override
    public int compareTo(CategorizedDomainObject other) {
        return ORDER_BY_CATEGORY.compound(ORDER_BY_SUBCATEGORY).compare(this, other);
    }

    private static Ordering<CategorizedDomainObject> ORDER_BY_CATEGORY = new Ordering<CategorizedDomainObject>() {
        @Override
        public int compare(CategorizedDomainObject left, CategorizedDomainObject right) {
            return Ordering.natural().nullsFirst().compare(left.getCategory(), right.getCategory());
        }
    };

    private static Ordering<CategorizedDomainObject> ORDER_BY_SUBCATEGORY = new Ordering<CategorizedDomainObject>() {
        @Override
        public int compare(CategorizedDomainObject left, CategorizedDomainObject right) {
            return Ordering.natural().nullsFirst().compare(left.getSubcategory(), right.getSubcategory());
        }

    };



}
