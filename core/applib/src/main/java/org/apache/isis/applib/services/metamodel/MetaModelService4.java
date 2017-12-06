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
package org.apache.isis.applib.services.metamodel;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifest2;
import org.apache.isis.applib.annotation.Programmatic;

public interface MetaModelService4 extends MetaModelService3 {

    /**
     * @return as {@link #getAppManifest()}, downcasted (else null).
     */
    @Programmatic
    AppManifest2 getAppManifest2();

    /**
     * @return the {@link AppManifest} used to bootstrap the application.
     */
    @Programmatic
    AppManifest getAppManifest();

}
