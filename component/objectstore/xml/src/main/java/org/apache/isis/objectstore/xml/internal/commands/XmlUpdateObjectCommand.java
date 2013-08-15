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

package org.apache.isis.objectstore.xml.internal.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.objectstore.xml.internal.data.Data;
import org.apache.isis.objectstore.xml.internal.data.DataManager;
import org.apache.isis.objectstore.xml.internal.version.FileVersion;

public final class XmlUpdateObjectCommand extends AbstractXmlPersistenceCommand implements SaveObjectCommand {

    private static final Logger LOG = LoggerFactory.getLogger(XmlUpdateObjectCommand.class);

    public XmlUpdateObjectCommand(final ObjectAdapter adapter, final DataManager dataManager) {
        super(adapter, dataManager);
    }

    @Override
    public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("  save object " + onAdapter());
        }
        final String user = getAuthenticationSession().getUserName();
        onAdapter().setVersion(FileVersion.create(user));

        final Data data = createObjectData(onAdapter(), true);
        getDataManager().save(data);
    }

    @Override
    public String toString() {
        return "SaveObjectCommand [object=" + onAdapter() + "]";
    }

}
