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

package org.apache.isis.remoting.protocol;

import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.remoting.data.query.PersistenceQueryData;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;

/**
 * TODO: this would be a good candidate for genericizing.
 */
public interface PersistenceQueryEncoder {

    Class<?> getPersistenceQueryClass();

    /**
     * Injected directly after instantiation (note that encoders can potentially be loaded reflectively, from the
     * {@link IsisConfiguration configuration} using the {@value ProtocolConstants#ENCODER_CLASS_NAME_LIST} key.
     */
    void setObjectEncoder(ObjectEncoderDecoder objectEncoder);

    PersistenceQueryData encode(PersistenceQuery persistenceQuery);

    PersistenceQuery decode(PersistenceQueryData persistenceQueryData);
}
