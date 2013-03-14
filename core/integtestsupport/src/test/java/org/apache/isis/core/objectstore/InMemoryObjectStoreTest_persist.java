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

package org.apache.isis.core.objectstore;

import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures.Listener;
import org.apache.isis.core.integtestsupport.tck.ObjectStoreContractTest_persist;
import org.apache.isis.core.runtime.installerregistry.installerapi.PersistenceMechanismInstaller;

public class InMemoryObjectStoreTest_persist extends ObjectStoreContractTest_persist {

    @Override
    protected PersistenceMechanismInstaller createPersistenceMechanismInstaller() {
        return new InMemoryPersistenceMechanismInstaller();
    }

    @Override
    protected Listener iswfListener() {
        return null;
    }
}
