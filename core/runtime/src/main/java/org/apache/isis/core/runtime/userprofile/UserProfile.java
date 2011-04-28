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

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.adapters.Localization;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

import com.google.common.collect.Lists;

public class UserProfile /* implements DebuggableWithTitle */ {
	

    public UserProfile() {}

    
    /////////////////////////////////////////////////////////
    // Perspective
    /////////////////////////////////////////////////////////

    private PerspectiveEntry entry;

    public PerspectiveEntry getPerspective() {
        if (entry == null) {
            if (entries.size() == 0) {
                throw new IsisException("No perspective in user profile");
            } else {
                entry = entries.get(0);
            }
        }
        return entry;
    }

    public PerspectiveEntry newPerspective(String name) {
        entry = new PerspectiveEntry();
        entry.setName(name);
        entries.add(entry);
        return entry;
    }

    public void removeCurrent() {
        if (entries.size() > 1) {
            entries.remove(entry);
            entry = entries.get(0);
        }
    }

    
    /////////////////////////////////////////////////////////
    // Perspective Entries
    /////////////////////////////////////////////////////////

    private final List<PerspectiveEntry> entries = Lists.newArrayList();

    public PerspectiveEntry getPerspective(String name) {
        for (PerspectiveEntry entry : entries) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        throw new IsisException("No perspective " + name);
    }

    public void  addToPerspectives(PerspectiveEntry perspective) {
        PerspectiveEntry e = new PerspectiveEntry();
        e.copy(perspective);
        entries.add(e);
    }


    public List<String> list() {
        List<String> list = Lists.newArrayList();
        for (PerspectiveEntry entry : entries) {
            list.add(entry.getName());
        }
        return list;
    }

    public void select(String name) {
        for (PerspectiveEntry entry : entries) {
            if (entry.getName().equals(name)) {
                this.entry = entry;
                break;
            }
        }
    }

    public void copy(UserProfile template) {
        for (PerspectiveEntry entry : template.entries) {
            PerspectiveEntry e = new PerspectiveEntry();
            e.copy(entry);
            entries.add(e);
        }
        options.copy(template.getOptions());
    }

    /**
     * Introduced for debugging.
     */
    public List<PerspectiveEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
    
    

    /////////////////////////////////////////////////////////
    // Options
    /////////////////////////////////////////////////////////

    private final Options options = new Options();
    private Localization localization;

    public Options getOptions() {
        return options;
    }

    public void addToOptions(String name, String value) {
        options.addOption(name, value);
    }
    
    
    /////////////////////////////////
    // Localization
    /////////////////////////////////

    public Localization getLocalization() {
        return localization;
    }
    
    public void setLocalization(Localization localization) {
        this.localization = localization;
    }

    

    /////////////////////////////////
    // Save
    /////////////////////////////////

    public void saveObjects(List<ObjectAdapter> objects) {
        entry.save(objects);
    }


}

