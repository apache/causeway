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

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

public class BreadcrumbModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final int MAX_SIZE = 5;

    private static final OidMarshaller OID_MARSHALLER = OidMarshaller.INSTANCE;

    private final Map<String, EntityModel> entityModelByOidStr = Maps.newHashMap();
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

        final String oidStr = oidStrFrom(entityModel);
        
        remove(oidStr);
        addToStart(oidStr, entityModel);
        
        trimTo(MAX_SIZE);
    }

    private String oidStrFrom(final EntityModel entityModel) {
        final PageParameters pageParameters = entityModel.getPageParametersWithoutUiHints();
        return oidStrFrom(pageParameters);
    }

    private String oidStrFrom(final PageParameters pageParameters) {
        String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        if(oidStr == null) {
            return null;
        }
        try {
            final RootOid unmarshal = OID_MARSHALLER.unmarshal(oidStr, RootOid.class);
            return unmarshal.enStringNoVersion();
        } catch(Exception ex) {
            return null;
        }
    }

    private void addToStart(final String oidStr, final EntityModel entityModel) {
        entityModelByOidStr.put(oidStr, entityModel);
        oidStrByEntityModel.put(entityModel, oidStr);
        list.add(0, entityModel);
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

    public String titleFor(final EntityModel model) {
        return model.getObjectAdapterMemento().getObjectAdapter(AdapterManager.ConcurrencyChecking.NO_CHECK,
                model.getPersistenceSession(), model.getSpecificationLoader()).titleString(null);
    }

    public EntityModel lookup(final String oidStr) {
        if(oidStr == null) {
            return null;
        }
        return entityModelByOidStr.get(oidStr);
    }

    public void detach() {
        for (EntityModel entityModel : list) {
            entityModel.detach();
        }
    }

    public RootOid getId(final EntityModel choice) {
        try {
            final PageParameters pageParameters = choice.getPageParameters();
            final String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
            return RootOid.deString(oidStr);
        } catch (Exception ex) {
            remove(choice);
            return null;
        }

    }

    void remove(final String rootOid) {
        final EntityModel existingModel = entityModelByOidStr.get(rootOid);
        if(existingModel != null) {
            remove(rootOid, existingModel);
        }
    }

    public void remove(final EntityModel entityModel) {
        final String oidStr = oidStrByEntityModel.get(entityModel);
        if(oidStr != null) {
            remove(oidStr, entityModel);
        }
    }

    private void remove(final String rootOid, final EntityModel model) {
        entityModelByOidStr.remove(rootOid);
        oidStrByEntityModel.remove(model);
        list.remove(model);
    }

}
