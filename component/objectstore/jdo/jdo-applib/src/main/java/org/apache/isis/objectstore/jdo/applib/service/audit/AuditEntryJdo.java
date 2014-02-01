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
package org.apache.isis.objectstore.jdo.applib.service.audit;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.objectstore.jdo.applib.service.JdoColumnLength;
import org.apache.isis.objectstore.jdo.applib.service.Util;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.DATASTORE,
        table="IsisAuditEntry")
@javax.jdo.annotations.DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY,
        column="id")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findByTransactionId", language="JDOQL",  
            value="SELECT "
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntryJdo "
                    + "WHERE transactionId == :transactionId")
})
@Indices({
    @Index(name="IsisAuditEntry_ak", unique="true", 
            columns={
                @javax.jdo.annotations.Column(name="transactionId"),
                @javax.jdo.annotations.Column(name="target"),
                @javax.jdo.annotations.Column(name="propertyId")
                })
})
@Immutable
@Named("Audit Entry")
@ObjectType("IsisAuditEntry")
@MemberGroupLayout(left={"Identifiers","Target","Detail"})
public class AuditEntryJdo implements HasTransactionId {

    
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(
        new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(getTimestamp()));
        buf.append(",", getUser());
        buf.append(":", getPropertyId());
        return buf.toString();
    }
    

    // //////////////////////////////////////
    // user (property)
    // //////////////////////////////////////

    private String user;

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.USER_NAME)
    @Hidden(where=Where.PARENTED_TABLES)
    @MemberOrder(name="Identifiers",sequence = "10")
    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }
    

    // //////////////////////////////////////
    // timestamp (property)
    // //////////////////////////////////////

    private Timestamp timestamp;

    @javax.jdo.annotations.Column(allowsNull="false")
    @Hidden(where=Where.PARENTED_TABLES)
    @MemberOrder(name="Identifiers",sequence = "20")
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    


    // //////////////////////////////////////
    // transactionId (property)
    // //////////////////////////////////////
    
    private UUID transactionId;

    /**
     * The unique identifier (a GUID) of the transaction in which this audit entry was persisted.
     * 
     * <p>
     * The combination of ({@link #getTransactionId() transactionId}, {@link #getTargetStr() target}, {@link #getPropertyId() propertyId} ) makes up the
     * (non-enforced) alternative key.
     */
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.TRANSACTION_ID)
    @TypicalLength(36)
    @MemberOrder(name="Identifiers",sequence = "30")
    @Hidden(where=Where.PARENTED_TABLES)
    @Disabled
    @Override
    public UUID getTransactionId() {
        return transactionId;
    }

    @Override
    public void setTransactionId(final UUID transactionId) {
        this.transactionId = transactionId;
    }


    // //////////////////////////////////////
    // target (property)
    // openTargetObject (action)
    // //////////////////////////////////////

    @Programmatic
    public Bookmark getTarget() {
        return Util.bookmarkFor(getTargetStr());
    }
    
    @Programmatic
    public void setTarget(Bookmark target) {
        setTargetStr(Util.asString(target));
    }

    // //////////////////////////////////////
    
    private String targetStr;

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.BOOKMARK, name="target")
    @Named("Object")
    @MemberOrder(name="Target", sequence="3")
    public String getTargetStr() {
        return targetStr;
    }

    public void setTargetStr(final String targetStr) {
        this.targetStr = targetStr;
    }

    
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="TargetStr", sequence="1")
    @Named("Open")
    public Object openTargetObject() {
        return Util.lookupBookmark(getTarget(), bookmarkService, container);
    }
    public boolean hideOpenTargetObject() {
        return getTarget() == null;
    }
    

    // //////////////////////////////////////
    // propertyId (property)
    // //////////////////////////////////////
    
    private String propertyId;
    
    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.AuditEntry.PROPERTY_ID)
    @MemberOrder(name="Target",sequence = "5")
    public String getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(final String propertyId) {
        this.propertyId = Util.abbreviated(propertyId, JdoColumnLength.AuditEntry.PROPERTY_ID);
    }
    
    
    // //////////////////////////////////////
    // preValue (property)
    // //////////////////////////////////////

    private String preValue;

    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.AuditEntry.PROPERTY_VALUE)
    @MemberOrder(name="Detail",sequence = "6")
    public String getPreValue() {
        return preValue;
    }

    public void setPreValue(final String preValue) {
        this.preValue = Util.abbreviated(preValue, JdoColumnLength.AuditEntry.PROPERTY_VALUE);
    }
    
    
    // //////////////////////////////////////
    // postValue (property)
    // //////////////////////////////////////

    private String postValue;

    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.AuditEntry.PROPERTY_VALUE)
    @MemberOrder(name="Detail",sequence = "7")
    public String getPostValue() {
        return postValue;
    }

    public void setPostValue(final String postValue) {
        this.postValue = Util.abbreviated(postValue, JdoColumnLength.AuditEntry.PROPERTY_VALUE);
    }
    
    // //////////////////////////////////////
    // Injected services
    // //////////////////////////////////////


    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private DomainObjectContainer container;
}
