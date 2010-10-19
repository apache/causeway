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


package org.apache.isis.metamodel.facets.actions.executed;

import org.apache.isis.metamodel.facets.EnumerationAbstract;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.spec.Target;


/**
 * Whether the action should be invoked locally, remotely, or on the default location depending on its
 * persistence state.
 * 
 * <p>
 * In the standard [[NAME]] Programming Model, corresponds to annotating the action method using
 * <tt>@Executed</tt>.
 */
public interface ExecutedFacet extends Facet {

    public static final class Where extends EnumerationAbstract {

        public static Where DEFAULT = new Where(0, "DEFAULT", "Default");
        public static Where LOCALLY = new Where(1, "LOCAL", "Locally");
        public static Where REMOTELY = new Where(2, "REMOTE", "Remotely");

        private Where(final int num, final String nameInCode, final String friendlyName) {
            super(num, nameInCode, friendlyName);
        }

    }

    public Where value();

    public Target getTarget();
}
