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

package org.apache.isis.persistence.jdo.applib.fixturestate;

/**
 * 
 * @since 2.0
 *
 */
public enum FixturesInstalledState {

    /**
     * application scoped state indicating fixture scripts have not been run yet
     */
    not_Installed,

    /**
     * application scoped state indicating fixture scripts are currently in the process 
     * of being installed (are running)
     */
    Installing,

    /**
     * application scoped state indicating fixture scripts have been installed (have run)
     */
    Installed

    ;

    public boolean isNotInstalled() {
        return this == not_Installed;
    }

    public boolean isInstalling() {
        return this == Installing;
    }

    public boolean isInstalled() {
        return this == Installed;
    }
}

