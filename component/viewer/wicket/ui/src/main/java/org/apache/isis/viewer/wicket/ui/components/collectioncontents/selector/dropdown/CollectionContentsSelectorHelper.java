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

package org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.dropdown;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.links.CollectionContentsLinksSelectorPanelFactory;

public class CollectionContentsSelectorHelper {

    private static final long serialVersionUID = 1L;

    private final ComponentType componentType;
    private final ComponentFactoryRegistry componentFactoryRegistry;
    private final EntityCollectionModel model;
    private final ComponentFactory ignoreFactory;


    public CollectionContentsSelectorHelper(
            final ComponentType componentType,
            final ComponentFactoryRegistry componentFactoryRegistry,
            final EntityCollectionModel model,
            final ComponentFactory ignoreFactory) {
        this.componentType = componentType;
        this.componentFactoryRegistry = componentFactoryRegistry;
        this.model = model;
        this.ignoreFactory = ignoreFactory;
    }


    public List<ComponentFactory> findOtherComponentFactories() {
        final List<ComponentFactory> componentFactories = componentFactoryRegistry.findComponentFactories(componentType, model);
        ArrayList<ComponentFactory> otherFactories = Lists.newArrayList(Collections2.filter(componentFactories, new Predicate<ComponentFactory>() {
            @Override
            public boolean apply(final ComponentFactory input) {
                return input != ignoreFactory && input.getClass() != CollectionContentsLinksSelectorPanelFactory.class;
            }
        }));
        return ordered(otherFactories);
    }

    private static List<ComponentFactory> ordered(List<ComponentFactory> componentFactories) {
        return orderAjaxTableToEnd(componentFactories);
    }



    static List<ComponentFactory> orderAjaxTableToEnd(List<ComponentFactory> componentFactories) {
        int ajaxTableIdx = findAjaxTable(componentFactories);
        if(ajaxTableIdx>=0) {
            List<ComponentFactory> orderedFactories = Lists.newArrayList(componentFactories);
            ComponentFactory ajaxTableFactory = orderedFactories.remove(ajaxTableIdx);
            orderedFactories.add(ajaxTableFactory);
            return orderedFactories;
        } else {
            return componentFactories;
        }
    }

    public static int findAjaxTable(List<ComponentFactory> componentFactories) {
        for(int i=0; i<componentFactories.size(); i++) {
            if(componentFactories.get(i) instanceof CollectionContentsAsAjaxTablePanelFactory) {
                return i;
            }
        }
        return -1;
    }



}
