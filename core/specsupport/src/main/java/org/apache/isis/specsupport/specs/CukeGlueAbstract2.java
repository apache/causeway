/**
O *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.specsupport.specs;

import org.apache.isis.core.runtime.headless.HeadlessAbstract;

/**
 * Base class for BDD spec glue.
 *
 * <p>
 *     Note that there also needs to be at least one spec glue that performs the bootstrapping.
 *     This should inline the boilerplate that can be found in {@link CukeGlueBootstrappingAbstract} (it's not possible
 *     to inherit from that class, unfortunately).
 * </p>
 */
public abstract class CukeGlueAbstract2 extends HeadlessAbstract {

}
