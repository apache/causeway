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

import com.google.common.collect.Lists;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public class PerspectiveEntry {

    public PerspectiveEntry() {
    }

    // ///////////////////////////////////////////////////////
    // Name & Title
    // ///////////////////////////////////////////////////////

    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTitle() {
        return name + " (" + services.size() + " classes)";
    }

    // ///////////////////////////////////////////////////////
    // Objects, save
    // ///////////////////////////////////////////////////////

    private final List<Object> objects = Lists.newArrayList();

    // REVIEW should this deal with Isis, and the services with IDs (or Isis)
    public List<Object> getObjects() {
        return objects;
    }

    public void addToObjects(final Object obj) {
        if (!objects.contains(obj)) {
            objects.add(obj);
        }
    }

    public void removeFromObjects(final Object obj) {
        objects.remove(obj);
    }

    public void save(final List<ObjectAdapter> adapters) {
        this.objects.clear();
        for (final ObjectAdapter adapter : adapters) {
            addToObjects(adapter.getObject());
        }
    }

    // ///////////////////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////////////////

    private final List<Object> services = Lists.newArrayList();

    public List<Object> getServices() {
        return services;
    }

    public void addToServices(final Object service) {
        if (service != null && !services.contains(service)) {
            services.add(service);
        }
    }

    public void removeFromServices(final Object service) {
        if (service != null && services.contains(service)) {
            services.remove(service);
        }
    }

    // ///////////////////////////////////////////////////////
    // copy
    // ///////////////////////////////////////////////////////

    public void copy(final PerspectiveEntry template) {
        name = template.getName();
        for (final Object service : template.getServices()) {
            addToServices(service);
        }
        for (final Object obj : template.getObjects()) {
            addToObjects(obj);
        }
    }

}
