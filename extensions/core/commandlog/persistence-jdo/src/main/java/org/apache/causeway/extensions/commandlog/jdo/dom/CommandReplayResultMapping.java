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
package org.apache.causeway.extensions.commandlog.jdo.dom;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping.Nq;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = CommandReplayResultMapping.SCHEMA,
        table = CommandReplayResultMapping.TABLE)
@Indices({
        @Index(name = "CommandReplayResultMapping__recordedBookmark__IDX", members = { "recordedBookmark" }, unique = "true"),
})
@Queries({
    @Query(
            name = Nq.FIND,
            value = "SELECT "
                  + "  FROM " + CommandReplayResultMapping.FQCN),
    @Query(
            name = Nq.FIND_BY_RECORDED_BOOKMARK,
            value = "SELECT "
                  + "  FROM " + CommandReplayResultMapping.FQCN + " "
                  + " WHERE recordedBookmark == :recordedBookmark")
})
@Named(org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@NoArgsConstructor
public class CommandReplayResultMapping
extends org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping {

    public static final String FQCN = "org.apache.causeway.extensions.commandlog.jdo.dom.CommandReplayResultMapping";

    @Persistent
    @Column(allowsNull = RecordedBookmark.ALLOWS_NULL, length = RecordedBookmark.MAX_LENGTH)
    @RecordedBookmark
    @Getter @Setter
    private Bookmark recordedBookmark;

    @Persistent
    @Column(allowsNull = ActualBookmark.ALLOWS_NULL, length = ActualBookmark.MAX_LENGTH)
    @ActualBookmark
    @Getter @Setter
    private Bookmark actualBookmark;

}
