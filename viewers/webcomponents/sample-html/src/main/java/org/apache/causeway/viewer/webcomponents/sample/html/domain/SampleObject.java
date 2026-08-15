/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.html.domain;

import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.persistence.jpa.applib.integration.HasVersion;

@Entity
@Table(schema = "public", name = "WebComponentSampleObject")
@Named(SampleObject.LOGICAL_TYPE_NAME)
@DomainObject(nature = Nature.ENTITY, bounding = Bounding.BOUNDED)
@DomainObjectLayout(describedAs = "Deterministic domain object for web-component verification")
public class SampleObject implements HasVersion<Long> {

    public static final String LOGICAL_TYPE_NAME = "causeway.webcomponents.sample.SampleObject";
    public static final String SAMPLE_ID = "sample-1";
    public static final String SAMPLE_BOOKMARK_ID = "s_" + SAMPLE_ID;
    public static final String SAMPLE_NAME = "Framework-neutral components";
    public static final String SAMPLE_CODE = "WC-001";
    public static final String SAMPLE_SECRET = "Hidden sample value";
    public static final String CODE_DISABLED_REASON = "The sample code is fixed.";

    @Id
    @Column(name = "id", nullable = false, length = 40)
    private String id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "secret", nullable = false, length = 120)
    private String secret;

    protected SampleObject() {
    }

    public SampleObject(final String id, final String name, final String code, final String secret) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.secret = secret;
    }

    @ObjectSupport
    public String title() {
        return String.format("%s [%s]", name, code);
    }

    @Override
    @Programmatic
    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    @MemberSupport
    public String disableCode() {
        return CODE_DISABLED_REASON;
    }

    public String getSecret() {
        return secret;
    }

    @MemberSupport
    public boolean hideSecret() {
        return true;
    }
}
