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
package org.apache.isis.applib.layout.common;

import java.util.LinkedHashMap;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.fixedcols.FCPage;
import org.apache.isis.applib.layout.fixedcols.FCTab;
import org.apache.isis.applib.services.layout.PageService;

/**
 * All top-level page layout classes should implement this interface.
 *
 * <p>
 *     It is used by the {@link PageService} as a common based type for any layouts read in from XML.
 * </p>
 */
public interface Page {

    boolean isNormalized();

    void setNormalized(final boolean normalized);

    @Programmatic
    LinkedHashMap<String, PropertyLayoutData> getAllPropertiesById();

    @Programmatic
    LinkedHashMap<String, CollectionLayoutData> getAllCollectionsById();

    @Programmatic
    LinkedHashMap<String, ActionLayoutData> getAllActionsById();

    @Programmatic
    LinkedHashMap<String, FieldSet> getAllFieldSetsByName();

}
