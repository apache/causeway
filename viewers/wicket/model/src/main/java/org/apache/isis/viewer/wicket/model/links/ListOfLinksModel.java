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

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;

import lombok.NonNull;
import lombok.val;

public class ListOfLinksModel extends LoadableDetachableModel<List<LinkAndLabel>> {

    private static final long serialVersionUID = 1L;

    private transient Can<LinkAndLabel> links;

    public ListOfLinksModel(final @NonNull Can<LinkAndLabel> links) {
        this.links = links;
    }

    @Override
    protected List<LinkAndLabel> load() {
        return links.toList();
    }

    public boolean hasAnyVisibleLink() {

        for (val linkAndLabel : links) {
            val link = linkAndLabel.getUiComponent();
            if(link.isVisible()) {
                return true;
            }
        }
        return false;
    }

    // -- SERIALIZATION PROXY

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final @NonNull ArrayList<LinkAndLabel> links;

        private SerializationProxy(ListOfLinksModel model) {
            this.links = new ArrayList<LinkAndLabel>(model.links.toList());
        }

        private Object readResolve() {
            return recover();
        }

        private ListOfLinksModel recover() {
            val linksToUse = (links.size()>0
                    && !(links.get(0) instanceof LinkAndLabel))
                ? LinkAndLabel.recoverFromIncompleteDeserialization(_Casts.uncheckedCast(links))
                : links;
            return new ListOfLinksModel(Can.ofCollection(linksToUse));
        }
    }


}
