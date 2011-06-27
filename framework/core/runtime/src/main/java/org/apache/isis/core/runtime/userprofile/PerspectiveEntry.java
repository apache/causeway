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


package org.apache.isis.core.runtime.userprofile;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

import com.google.common.collect.Lists;

public class PerspectiveEntry {

    public PerspectiveEntry() {}

    /////////////////////////////////////////////////////////
    // Name & Title
    /////////////////////////////////////////////////////////

    private String name;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return name + " (" + services.size() + " classes)";
    }


    /////////////////////////////////////////////////////////
    // Objects, save
    /////////////////////////////////////////////////////////

    private final List<Object> objects = Lists.newArrayList();


    // REVIEW should this deal with Isis, and the services with IDs (or Isis)
    public List<Object> getObjects() {
        return objects;
    }

    public void addToObjects(Object obj) {
        if (!objects.contains(obj)) {
            objects.add(obj);
        }
    }

    public void removeFromObjects(Object obj) {
        objects.remove(obj);
    }

    public void save(List<ObjectAdapter> adapters) {
        this.objects.clear();
        for (ObjectAdapter adapter : adapters) {
            addToObjects(adapter.getObject());
        }
    }

    /////////////////////////////////////////////////////////
    // Services
    /////////////////////////////////////////////////////////

    private final List<Object> services = Lists.newArrayList();

    public List<Object> getServices() {
        return services;
    }

    public void addToServices(Object service) {
        if (service != null && !services.contains(service)) {
            services.add(service);
        }
    }

    public void removeFromServices(Object service) {
        if (service != null && services.contains(service)) {
            services.remove(service);
        }
    }


    /////////////////////////////////////////////////////////
    // copy
    /////////////////////////////////////////////////////////

    public void copy(PerspectiveEntry template) {
        name = template.getName();
        for (Object service : template.getServices()) {
            addToServices(service);
        }
        for (Object obj : template.getObjects()) {
            addToObjects(obj);
        }
    }


}


