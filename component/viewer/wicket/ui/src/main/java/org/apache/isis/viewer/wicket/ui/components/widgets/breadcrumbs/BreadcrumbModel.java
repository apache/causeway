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
package org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

public class BreadcrumbModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final int MAX_SIZE = 5;

    private final Map<String, EntityModel> entityModelByOidStr = Maps.newHashMap();
    private final Map<EntityModel, String> titleByEntityModel = Maps.newHashMap();
    private final Map<EntityModel, String> oidStrByEntityModel = Maps.newHashMap();
    private final List<EntityModel> list = Lists.newArrayList();
    
    public List<EntityModel> getList() {
        return Collections.unmodifiableList(list);
    }

    public void visited(final EntityModel entityModel) {

        // ignore view models
        if(entityModel.getTypeOfSpecification().isViewModel()) {
            return;
        }

        final String oidStr = oidStrFor(entityModel);
        
        removeExisting(oidStr);
        addToStart(oidStr, entityModel);
        
        trimTo(MAX_SIZE);
    }

    private String oidStrFor(final EntityModel entityModel) {
        final PageParameters pageParameters = entityModel.getPageParametersWithoutUiHints();
        return PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
    }

    private void addToStart(final String oidStr, final EntityModel entityModel) {
        entityModelByOidStr.put(oidStr, entityModel);
        titleByEntityModel.put(entityModel, entityModel.getTitle());
        oidStrByEntityModel.put(entityModel, oidStr);
        list.add(0, entityModel);
    }

    private void removeExisting(final String oidStr) {
        final EntityModel existingModel = entityModelByOidStr.get(oidStr);
        if(existingModel != null) {
            remove(oidStr, existingModel);
        }
    }

    private void trimTo(final int size) {
        if(list.size() <= size) {
            return;
        } 
        final List<EntityModel> modelsToRemove = list.subList(size, list.size());
        for (final EntityModel model : modelsToRemove) {
            final String oidStr = oidStrByEntityModel.get(model);
            remove(oidStr, model);
        }
    }

    private void remove(final String oidStr, final EntityModel model) {
        entityModelByOidStr.remove(oidStr);
        titleByEntityModel.remove(model);
        oidStrByEntityModel.remove(model);
        list.remove(model);
    }

    public void remove(String oidStr) {
        EntityModel removedModel = entityModelByOidStr.remove(oidStr);
        if(removedModel != null) {
            remove(removedModel);
        }
    }

    public void remove(EntityModel entityModel) {
        String oidStr = oidStrByEntityModel.get(entityModel);
        if(oidStr != null) {
            remove(oidStr, entityModel);
        }
    }

    public String titleFor(final EntityModel model) {
        return titleByEntityModel.get(model);
    }

    public EntityModel lookup(String oidStr) {
        if(oidStr == null) {
            return null;
        }
        return entityModelByOidStr.get(oidStr);
    }



}
