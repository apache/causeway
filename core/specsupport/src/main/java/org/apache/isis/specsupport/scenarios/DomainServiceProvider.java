/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.isis.specsupport.scenarios;

/**
 * Provides access to the any domain services
 * that may have been configured.
 *
 * <p>
 * For {@link ScenarioExecution scenario}s with integration-scope, these will be
 * configured services for an end-to-end running system.  For scenarios with
 * unit-scope, these will typically be mocks.
 *
 * @deprecated - with no replacement
 */
@Deprecated
public interface DomainServiceProvider {

    <T> T getService(Class<T> serviceClass);

    /**
     * Replaces the service implementation with some other.
     *
     * <p>
     * Allows services to be mocked out.  It is the responsibility of the test to reinstate the &quot;original&quot;
     * service implementation afterwards.
     * </p>
     */
    <T> void replaceService(T original, T replacement);
}