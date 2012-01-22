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

package org.apache.isis.examples.onlinedemo.objstore.dflt.items;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.examples.onlinedemo.dom.items.Categories;
import org.apache.isis.examples.onlinedemo.dom.items.Category;

public class CategoriesDefault extends AbstractFactoryAndRepository implements Categories {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "categories";
    }

    public String iconName() {
        return "Category";
    }

    // }}

    @Override
    public List<Category> all() {
        return allInstances(Category.class);
    }

    @Override
    public Category newCategory(final String description) {
        Category category = find(description);
        if (category != null) {
            return category;
        }
        category = newTransientInstance(Category.class);
        category.setDescription(description);
        persist(category);
        return category;
    }

    @Override
    public Category find(final String description) {
        return firstMatch(Category.class, Category.matching(description));
    }

}
