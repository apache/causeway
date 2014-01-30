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

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.interaction.Interaction;
import org.apache.isis.applib.util.TitleBuffer;

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
                    + "FROM org.apache.isis.objectstore.jdo.applib.service.audit.AuditEntry "
                    + "WHERE transactionId == :transactionId")
})
@Immutable
@Named("Audit Entry")
@MemberGroupLayout(left={"When/Who","Target","Values"})
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

    private Timestamp timestamp;

    @javax.jdo.annotations.Column(allowsNull="false")
    @MemberOrder(name="When/Who",sequence = "1")
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    
    // //////////////////////////////////////
    
    private UUID transactionId;

    /**
     * The unique identifier (a GUID) of the transaction of the {@link Interaction} that gave rise to this
     * audit entry.
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=36)
    @TypicalLength(36)
    @MemberOrder(name="When/Who",sequence = "20")
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

    
    private String user;

    @javax.jdo.annotations.Column(allowsNull="false", length=50)
    @MemberOrder(name="When/Who",sequence = "2")
    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = abbreviated(user, 50);
    }
    
    
    // //////////////////////////////////////
    // target (property)
    // //////////////////////////////////////

    @Programmatic
    public Bookmark getTarget() {
        return new Bookmark(getTargetStr());
    }
    
    @Programmatic
    public void setTarget(Bookmark target) {
        setTargetStr(target.toString());
    }

    private String targetStr;

    @javax.jdo.annotations.Column(allowsNull="false", length=255, name="target")
    @Named("Target Bookmark")
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(name="Target", sequence="3")
    public String getTargetStr() {
        return targetStr;
    }

    public void setTargetStr(final String targetStr) {
        this.targetStr = abbreviated(targetStr, 255);
    }

    
    // //////////////////////////////////////
    // openTargetObject (action)
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="TargetStr", sequence="1")
    @Named("Open")
    public Object openTargetObject() {
        return lookupBookmark(getTarget());
    }
    public boolean hideOpenTargetObject() {
        return getTargetStr() == null;
    }
    

    // //////////////////////////////////////
    
    private String propertyId;
    
    @javax.jdo.annotations.Column(allowsNull="true", length=50)
    @MemberOrder(name="Target",sequence = "5")
    public String getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(final String propertyId) {
        this.propertyId = abbreviated(propertyId,50);
    }
    
    
    // //////////////////////////////////////

    private String preValue;

    @javax.jdo.annotations.Column(allowsNull="true", length=255)
    @MemberOrder(name="Values",sequence = "6")
    public String getPreValue() {
        return preValue;
    }

    public void setPreValue(final String preValue) {
        this.preValue = abbreviated(preValue,255);
    }
    
    // //////////////////////////////////////

    private String postValue;

    @javax.jdo.annotations.Column(allowsNull="true", length=255)
    @MemberOrder(name="Values",sequence = "7")
    public String getPostValue() {
        return postValue;
    }

    public void setPostValue(final String postValue) {
        this.postValue = abbreviated(postValue, 255);
    }
    
    // //////////////////////////////////////

    private Object lookupBookmark(Bookmark bookmark) {
        try {
        return bookmarkService != null
                ? bookmarkService.lookup(bookmark)
                : null;
        } catch(RuntimeException ex) {
            if(ex.getClass().getName().contains("ObjectNotFoundException")) {
                container.warnUser("Object not found - has it since been deleted?");
                return null;
            } 
            throw ex;
        }
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str != null? (str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "..."): null;
    }
    
    // //////////////////////////////////////


    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private DomainObjectContainer container;
}
