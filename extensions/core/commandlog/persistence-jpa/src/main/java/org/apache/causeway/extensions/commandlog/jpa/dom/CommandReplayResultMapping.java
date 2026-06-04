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
package org.apache.causeway.extensions.commandlog.jpa.dom;

import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping.Nq;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        schema = CommandReplayResultMapping.SCHEMA,
        name = CommandReplayResultMapping.TABLE,
        indexes = {
                @Index(name = "CommandReplayResultMapping__recorded__IDX", columnList = "recordedLogicalTypeName, recordedIdentifier")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "CommandReplayResultMapping__recorded__UNQ", columnNames = { "recordedLogicalTypeName", "recordedIdentifier" })
        }
)
@NamedQueries({
    @NamedQuery(
            name = Nq.FIND,
            query = "SELECT m "
                  + "  FROM CommandReplayResultMapping m "
                  + " ORDER BY m.recordedLogicalTypeName ASC, m.recordedIdentifier ASC"),
    @NamedQuery(
            name = Nq.FIND_BY_RECORDED_BOOKMARK,
            query = "SELECT m "
                  + "  FROM CommandReplayResultMapping m "
                  + " WHERE m.recordedLogicalTypeName = :recordedLogicalTypeName "
                  + "   AND m.recordedIdentifier = :recordedIdentifier")
})
@Named(org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@EntityListeners(CausewayEntityListener.class)
@NoArgsConstructor
public class CommandReplayResultMapping
extends org.apache.causeway.extensions.commandlog.applib.dom.CommandReplayResultMapping {

    @Id
    @GeneratedValue
    @Getter @Setter
    private Long id;

    @Column(nullable = RecordedLogicalTypeName.NULLABLE, length = RecordedLogicalTypeName.MAX_LENGTH)
    @RecordedLogicalTypeName
    @Getter @Setter
    private String recordedLogicalTypeName;

    @Column(nullable = RecordedIdentifier.NULLABLE, length = RecordedIdentifier.MAX_LENGTH)
    @RecordedIdentifier
    @Getter @Setter
    private String recordedIdentifier;

    @Column(nullable = ActualLogicalTypeName.NULLABLE, length = ActualLogicalTypeName.MAX_LENGTH)
    @ActualLogicalTypeName
    @Getter @Setter
    private String actualLogicalTypeName;

    @Column(nullable = ActualIdentifier.NULLABLE, length = ActualIdentifier.MAX_LENGTH)
    @ActualIdentifier
    @Getter @Setter
    private String actualIdentifier;

}
