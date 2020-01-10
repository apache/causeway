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
package org.apache.isis.persistence.jdo.datanucleus5.datanucleus;

import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.enhancer.EnhancementHelper;

import org.apache.isis.runtime.context.IsisContext;

/**
 *
 * Purges any state associated with DataNucleus.
 * <br/><br/>
 * (requires datanucleus-core 4 or 5 >= 5.1.5)
 *
 * @since 2.0.0
 *
 */
public class DataNucleusLifeCycleHelper {

    public static void cleanUp(PersistenceManagerFactory persistenceManagerFactory) {

        try {

            final ClassLoader cl = IsisContext.getClassLoader();

            persistenceManagerFactory.close();

            // for info, on why we do this see
            // https://github.com/datanucleus/datanucleus-core/issues/272
            EnhancementHelper.getInstance().unregisterClasses(cl);

            // cleanup thread locals
            JDOStateManagerForIsis.hint.remove();

        } catch (Exception e) {
            // ignore, since it only affects re-deploy-ability, which is nice to have but not critical
        }

    }

}
