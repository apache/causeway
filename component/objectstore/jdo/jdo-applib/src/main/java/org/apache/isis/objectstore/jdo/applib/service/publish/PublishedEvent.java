package org.apache.isis.objectstore.jdo.applib.service.publish;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.APPLICATION)
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="publishedevent_of_state", language="JDOQL",  
            value="SELECT FROM org.apache.isis.objectstore.jdo.applib.service.publish.PublishedEvent WHERE state == :state ORDER BY timestamp")
})
//@javax.jdo.annotations.Indices({
//    @javax.jdo.annotations.Index(members={"timestamp,transactionId,sequence"})
//})
@Immutable
public class PublishedEvent {

    public static enum State {
        QUEUED, PROCESSED
    }
    
    // {{ Title (property)
    private String title;

    @Title
    @Hidden
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
    // }}


    // {{ Timestamp (property)
    private long timestamp;

    @MemberOrder(sequence = "1")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }
    // }}

    // {{ Id (property)
    @javax.jdo.annotations.PrimaryKey
    private String id;

    @MemberOrder(sequence = "2")
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
    // }}

    
    // {{ TransactionId (property)
    private String transactionId;

    /**
     * Programmatic because information also available in the {@link #getId() id}.
     */
    @Programmatic
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }
    // }}

    
    // {{ Sequence (property)
    private int sequence;

    /**
     * Programmatic because information also available in the {@link #getId() id}.
     */
    @Programmatic
    public int getSequence() {
        return sequence;
    }

    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }
    // }}


    // {{ User (property)
    private String user;

    @MemberOrder(sequence = "3")
    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }
    // }}

    
    // {{ SerializedForm (property)
    @javax.jdo.annotations.Column(length=4000)
    private String serializedForm;

    @MultiLine(numberOfLines=20)
    @Hidden(where=Where.ALL_TABLES)
    @MemberOrder(sequence = "5")
    public String getSerializedForm() {
        return serializedForm;
    }

    public void setSerializedForm(final String propertyName) {
        this.serializedForm = propertyName;
    }
    // }}

    // {{ State (property)
    private State state;

    @MemberOrder(sequence = "4")
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }
    // }}

}
