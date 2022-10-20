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
package org.apache.causeway.applib.services.health;

/**
 * This SPI service allow runtime infrastructure such as Kubernetes or Docker
 * Swarm to monitor the app and (potentially) restart it if required.
 *
 * <p>
 * This SPI service integrates with Spring Boot's
 * <a href="https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/health/HealthIndicator.html">HealthIndicator</a>
 * SPI, surfaced through the
 * <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html">Spring Boot Actuator</a>.
 * It is therefore accessible from the <code>/actuator/health</code> endpoint
 * (Spring allows the endpoint URL to be altered or suppressed).
 * </p>
 *
 * <p>
 * The service, when called, will be within the context of a special internal
 * user <i>__health</i> with the internal role <i>__health-role</i>.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface HealthCheckService {

    Health check();
}


