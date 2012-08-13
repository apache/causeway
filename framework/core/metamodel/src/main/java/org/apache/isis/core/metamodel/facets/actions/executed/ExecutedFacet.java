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

package org.apache.isis.core.metamodel.facets.actions.executed;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.facets.EnumerationAbstract;
import org.apache.isis.core.metamodel.facets.SingleValueFacet;
import org.apache.isis.core.metamodel.spec.Target;

/**
 * Whether the action should be invoked locally, remotely, or on the default
 * location depending on its persistence state.
 * 
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * action method using <tt>@Executed</tt>.
 */
public interface ExecutedFacet extends SingleValueFacet<ExecutedFacet.Where> {

    public static final class Where extends EnumerationAbstract {

        public static Where DEFAULT = new Where(0, "DEFAULT", "Default");
        public static Where LOCALLY = new Where(1, "LOCAL", "Locally");
        public static Where REMOTELY = new Where(2, "REMOTE", "Remotely");

        public static final String REMOTE_PREFIX = "Remote";
        public static final String LOCAL_PREFIX = "Local";

        private Where(final int num, final String nameInCode, final String friendlyName) {
            super(num, nameInCode, friendlyName);
        }

        public static Where lookup(final Method actionMethod) {
            final String capitalizedName = NameUtils.capitalizeName(actionMethod.getName());
            if (capitalizedName.startsWith(LOCAL_PREFIX)) {
                return LOCALLY;
            } else if (capitalizedName.startsWith(REMOTE_PREFIX)) {
                return REMOTELY;
            }
            return null;
        }

    }

    public Target getTarget();
}
