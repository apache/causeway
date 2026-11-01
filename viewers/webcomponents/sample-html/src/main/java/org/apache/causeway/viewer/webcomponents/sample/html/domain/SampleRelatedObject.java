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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.persistence.jpa.applib.integration.HasVersion;

@Entity
@Table(schema = "public", name = "WebComponentSampleRelatedObject")
@Named(SampleRelatedObject.LOGICAL_TYPE_NAME)
@DomainObject(nature = Nature.ENTITY, bounding = Bounding.BOUNDED)
@DomainObjectLayout(describedAs = "Deterministic collection row for web-component verification")
public class SampleRelatedObject implements HasVersion<Long> {

    public static final String LOGICAL_TYPE_NAME = "causeway.webcomponents.sample.SampleRelatedObject";
    public static final String FIRST_ID = "related-1";
    public static final String FIRST_BOOKMARK_ID = "s_" + FIRST_ID;
    public static final String FIRST_NAME = "Schema-driven components";
    public static final String FIRST_CODE = "ROW-001";
    public static final String SECOND_ID = "related-2";
    public static final String SECOND_BOOKMARK_ID = "s_" + SECOND_ID;
    public static final String SECOND_NAME = "Framework-neutral composition";
    public static final String SECOND_CODE = "ROW-002";

    @Id
    @Column(name = "id", nullable = false, length = 40)
    private String id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sampleObjectId", nullable = false)
    private SampleObject parent;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    protected SampleRelatedObject() {
    }

    public SampleRelatedObject(
            final String id,
            final SampleObject parent,
            final String name,
            final String code) {
        this.id = id;
        this.parent = parent;
        this.name = name;
        this.code = code;
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

    @Programmatic
    public SampleObject getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
