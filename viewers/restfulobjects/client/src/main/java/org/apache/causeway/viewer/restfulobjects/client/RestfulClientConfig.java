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
package org.apache.causeway.viewer.restfulobjects.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.ProcessingException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.causeway.viewer.restfulobjects.client.log.ClientConversationFilter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@XmlRootElement(name="restful-client-config")
@XmlAccessorType(XmlAccessType.FIELD)
@Data @Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestfulClientConfig {

    @XmlElement(name="restfulBase")
    private String restfulBase;

    @XmlElement(name="useBasicAuth")
    private boolean useBasicAuth;

    @XmlElement(name="restfulAuthUser")
    private String restfulAuthUser;

    @XmlElement(name="restfulAuthPassword")
    private String restfulAuthPassword;

    /**
     * If enabled, logs conversation (request/response) details.
     */
    @XmlElement(name="useRequestDebugLogging")
    private boolean useRequestDebugLogging;

    /**
     * Set the connect timeout.
     * <p>
     * Value {@code 0} represents infinity. Negative values are not allowed.
     * <p>
     * The default value is infinity (0).
     * @see javax.ws.rs.client.ClientBuilder#connectTimeout(long, TimeUnit)
     */
    @XmlElement(name="connectTimeoutInMillis")
    @Builder.Default
    private long connectTimeoutInMillis = 0L;

    /**
     * Set the read timeout.
     * <p>
     * The value is the timeout to read a response. If the server doesn't respond within the defined timeframe,
     * {@link ProcessingException} is thrown with {@link TimeoutException} as a cause.
     * <p>
     * Value {@code 0} represents infinity. Negative values are not allowed.
     * <p>
     * The default value is infinity (0).
     * @see javax.ws.rs.client.ClientBuilder#readTimeout(long, TimeUnit)
     */
    @XmlElement(name="readTimeoutInMillis")
    @Builder.Default
    private long readTimeoutInMillis = 0L;

    @XmlTransient
    @Builder.Default
    private final List<ClientConversationFilter> clientConversationFilters = new ArrayList<>();

}
