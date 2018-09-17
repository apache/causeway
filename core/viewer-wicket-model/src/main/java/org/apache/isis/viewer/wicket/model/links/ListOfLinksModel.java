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

package org.apache.isis.viewer.wicket.model.links;

import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;

import org.apache.wicket.model.LoadableDetachableModel;


public class ListOfLinksModel extends LoadableDetachableModel<List<LinkAndLabel>> {

    private static final long serialVersionUID = 1L;

    private final List<LinkAndLabel> links;

    public ListOfLinksModel(List<LinkAndLabel> links) {
        // copy, in case supplied list is a non-serializable guava list using lazy evaluation;
        this.links = _Lists.newArrayList(links);
    }

    @Override
    protected List<LinkAndLabel> load() {
        return links;
    }

}
