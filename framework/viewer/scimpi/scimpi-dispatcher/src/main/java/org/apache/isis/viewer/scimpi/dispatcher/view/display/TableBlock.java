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

package org.apache.isis.viewer.scimpi.dispatcher.view.display;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.scimpi.dispatcher.BlockContent;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request.RepeatMarker;

public class TableBlock implements BlockContent {

    // {{ collection
    private ObjectAdapter collection;

    public void setCollection(final ObjectAdapter collection) {
        this.collection = collection;
    }

    public ObjectAdapter getCollection() {
        return collection;
    }
    // }}
    
    // {{ linkView
    private String linkView;

    public String getlinkView() {
        return linkView;
    }

    public void setlinkView(final String linkView) {
        this.linkView = linkView;
    }
    // }}
    
    // {{ linkName
    private String linkName;

    public String getlinkName() {
        return linkName;
    }

    public void setlinkName(final String linkName) {
        this.linkName = linkName;
    }
    // }}

    // {{ elementName
    private String elementName;

    public String getElementName() {
        return elementName;
    }

    public void setElementName(final String linkObject) {
        this.elementName = linkObject;
    }
    // }}

    private RepeatMarker marker;

    public RepeatMarker getMarker() {
        return marker;
    }

    public void setMarker(final RepeatMarker marker) {
        this.marker = marker;
    }

}
