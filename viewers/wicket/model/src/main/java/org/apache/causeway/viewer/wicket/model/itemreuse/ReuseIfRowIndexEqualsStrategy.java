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
package org.apache.causeway.viewer.wicket.model.itemreuse;

import java.util.Iterator;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.model.IModel;

import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;

/**
 * Item reuse strategy for {@link DataRowWkt} models based on row index.
 * If model is not a {@link DataRowWkt}, does not reuse.
 *
 * @see ReuseIfModelsEqualStrategy
 */
public class ReuseIfRowIndexEqualsStrategy implements IItemReuseStrategy {
    private static final long serialVersionUID = 1L;

    private static ReuseIfRowIndexEqualsStrategy INSTANCE = new ReuseIfRowIndexEqualsStrategy();

    public static ReuseIfRowIndexEqualsStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * @see org.apache.wicket.markup.repeater.IItemReuseStrategy#getItems(org.apache.wicket.markup.repeater.IItemFactory,
     *      java.util.Iterator, java.util.Iterator)
     */
    @Override
    public <T> Iterator<Item<T>> getItems(
            final IItemFactory<T> factory,
            final Iterator<IModel<T>> newModels,
            final Iterator<Item<T>> existingItems) {

        var itemByRowIndex = new ItemByRowIndexMap<T>();
        existingItems.forEachRemaining(itemByRowIndex::putItem);

        return new Iterator<Item<T>>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return newModels.hasNext();
            }

            @Override
            public Item<T> next() {
                final IModel<T> model = newModels.next();
                final Item<T> item = itemByRowIndex.getItem(model)
                        .map(oldItem->withIndex(index, oldItem))
                        .orElseGet(()->factory.newItem(index, model));
                index++;
                return item;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    // -- HELPER

    private static <T> Item<T> withIndex(final int index, final Item<T> item) {
        item.setIndex(index);
        return item;
    }

    private static class ItemByRowIndexMap<T> extends TreeMap<Integer, Item<T>> {
        private static final long serialVersionUID = 1L;

        void putItem(final Item<T> item) {
            final int rowIndex = rowIndex(item.getModel());
            if(rowIndex > -1) {
                super.put(rowIndex, item);
            }
        }

        Optional<Item<T>> getItem(final IModel<T> model) {
            final int rowIndex = rowIndex(model);
            return rowIndex > -1
                 ? Optional.ofNullable(get(rowIndex))
                 : Optional.empty();
        }

        private static int rowIndex(final IModel<?> model) {
            return model instanceof DataRowWkt
                ? ((DataRowWkt)model).getRowIndex()
                : -1;
        }
    }

}
