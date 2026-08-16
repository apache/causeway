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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.inject.Named;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Bounding;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.persistence.jpa.applib.integration.HasVersion;

@Entity
@Table(schema = "public", name = "WebComponentSampleObject")
@Named(SampleObject.LOGICAL_TYPE_NAME)
@DomainObject(nature = Nature.ENTITY, bounding = Bounding.BOUNDED, editing = Editing.ENABLED)
@DomainObjectLayout(describedAs = "Deterministic domain object for web-component verification")
public class SampleObject implements HasVersion<Long> {

    public static final String LOGICAL_TYPE_NAME = "causeway.webcomponents.sample.SampleObject";
    public static final String SAMPLE_ID = "sample-1";
    public static final String SAMPLE_BOOKMARK_ID = "s_" + SAMPLE_ID;
    public static final String SAMPLE_NAME = "Framework-neutral components";
    public static final String SAMPLE_CODE = "WC-001";
    public static final String SAMPLE_SECRET = "Hidden sample value";
    public static final String SAMPLE_SUMMARY = "A deterministic reference page composed entirely from semantic Causeway web components.";
    public static final int SAMPLE_CAPACITY = 24;
    public static final boolean SAMPLE_FEATURED = true;
    public static final SampleStatus SAMPLE_STATUS = SampleStatus.ACTIVE;
    public static final String CODE_DISABLED_REASON = "The sample code is fixed.";
    public static final String ARCHIVE_DISABLED_REASON = "Archiving is disabled in the read-only sample.";

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

    @Column(name = "summary", nullable = false, length = 240)
    private String summary;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "featured", nullable = false)
    private boolean featured;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SampleStatus status;

    @Column(name = "notes", length = 200)
    private String notes;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SampleRelatedObject> relatedObjects = new ArrayList<>();

    protected SampleObject() {
    }

    public SampleObject(final String id, final String name, final String code, final String secret) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.secret = secret;
        this.summary = SAMPLE_SUMMARY;
        this.capacity = SAMPLE_CAPACITY;
        this.featured = SAMPLE_FEATURED;
        this.status = SAMPLE_STATUS;
        this.notes = null;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(final int capacity) {
        this.capacity = capacity;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(final boolean featured) {
        this.featured = featured;
    }

    public SampleStatus getStatus() {
        return status;
    }

    public void setStatus(final SampleStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public SampleRelatedObject getRelatedObject() {
        return relatedObjects.isEmpty() ? null : relatedObjects.get(0);
    }

    public List<SampleRelatedObject> getRelatedObjects() {
        return Collections.unmodifiableList(relatedObjects);
    }

    public List<SampleRelatedObject> getEmptyRelatedObjects() {
        return Collections.emptyList();
    }

    @Programmatic
    public void addRelatedObject(final String id, final String relatedName, final String relatedCode) {
        relatedObjects.add(new SampleRelatedObject(id, this, relatedName, relatedCode));
    }

    public String inspect() {
        return title();
    }

    public void archive() {
        // Deliberately disabled by disableArchive(); the read-only sample never invokes this action.
    }

    @MemberSupport
    public String disableArchive() {
        return ARCHIVE_DISABLED_REASON;
    }

    @Action
    @ActionLayout(hidden = Where.EVERYWHERE)
    public void hiddenAction() {
        // Deliberately hidden; the rich schema still exposes its hidden state.
    }
}
