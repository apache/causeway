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
package org.apache.isis.viewer.bdd.common.fixtures.perform;

import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsContainment;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.AssertsEmpty;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Disabled;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Hidden;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.PerformCheckThatAbstract;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Usable;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.Visible;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections.Containment;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections.Emptiness;

public class CheckCollection extends PerformCheckThatAbstract {

    public CheckCollection(final Perform.Mode mode) {
        super("check collection", OnMemberColumn.REQUIRED, mode, new Hidden(), new Visible(), new Disabled(), new Usable(), new Emptiness(AssertsEmpty.EMPTY), new Emptiness(AssertsEmpty.NOT_EMPTY), new Containment(AssertsContainment.CONTAINS), new Containment(AssertsContainment.DOES_NOT_CONTAIN));
    }

}
