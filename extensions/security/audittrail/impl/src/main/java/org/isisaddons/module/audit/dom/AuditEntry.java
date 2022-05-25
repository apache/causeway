package org.isisaddons.module.audit.dom;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.HasUsername;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.objectstore.jdo.applib.service.DomainChangeJdoAbstract;
import org.apache.isis.objectstore.jdo.applib.service.JdoColumnLength;
import org.apache.isis.objectstore.jdo.applib.service.Util;

import org.isisaddons.module.audit.AuditModule;

import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.DATASTORE,
        schema = "isisaudit",
        table="AuditEntry")
@javax.jdo.annotations.DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY,
        column="id")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name="findFirstByTarget", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE targetStr == :targetStr "
                    + "ORDER BY timestamp ASC "
                    + "RANGE 0,2"),
    @javax.jdo.annotations.Query(
            name="findRecentByTarget", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE targetStr == :targetStr "
                    + "ORDER BY timestamp DESC "
                    + "RANGE 0,100"),
    @javax.jdo.annotations.Query(
            name="findRecentByTargetAndPropertyId", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE targetStr == :targetStr "
                    + "&&    propertyId == :propertyId "
                    + "ORDER BY timestamp DESC "
                    + "RANGE 0,30"),
    @javax.jdo.annotations.Query(
            name="findByTransactionId", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE transactionId == :transactionId"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBetween", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE targetStr == :targetStr "
                    + "&&    timestamp >= :from "
                    + "&&    timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampAfter", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE targetStr == :targetStr "
                    + "&&    timestamp >= :from "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTargetAndTimestampBefore", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE targetStr == :targetStr "
                    + "&&    timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTarget", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE targetStr == :targetStr "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBetween", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE timestamp >= :from "
                    + "&&    timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampAfter", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE timestamp >= :from "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="findByTimestampBefore", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "WHERE timestamp <= :to "
                    + "ORDER BY timestamp DESC"),
    @javax.jdo.annotations.Query(
            name="find", language="JDOQL",
            value="SELECT "
                    + "FROM org.isisaddons.module.audit.dom.AuditEntry "
                    + "ORDER BY timestamp DESC")
})
//@Indices({
//    @Index(name="AuditEntry_ak", unique="true",
//            columns={
//                @javax.jdo.annotations.Column(name="transactionId"),
//                @javax.jdo.annotations.Column(name="sequence"),
//                @javax.jdo.annotations.Column(name="target"),
//                @javax.jdo.annotations.Column(name="propertyId")
//                }),
//    @Index(name="AuditEntry_target_ts_IDX", unique="false",
//            members={ "targetStr", "timestamp" }),
//})
@DomainObject(
        editing = Editing.DISABLED,
        objectType = "isisaudit.AuditEntry"
)
public class AuditEntry extends DomainChangeJdoAbstract implements HasTransactionId, HasUsername {

    //region > domain events
    public static abstract class PropertyDomainEvent<T> extends AuditModule.PropertyDomainEvent<AuditEntry, T> {
    }
    //endregion

    public AuditEntry() {
        super(ChangeType.AUDIT_ENTRY);
    }

    //region > title

    public String title() {

        // nb: not thread-safe
        // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        final TitleBuffer buf = new TitleBuffer();
        buf.append(format.format(getTimestamp()));
        buf.append(" ").append(getMemberIdentifier());
        return buf.toString();
    }

    //endregion

    //region > user (property)
    public static class UserDomainEvent extends PropertyDomainEvent<String> {
    }

    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.USER_NAME)
    @Property(
            domainEvent = UserDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES
    )
    @Getter @Setter
    private String user;

    @Programmatic
    public String getUsername() {
        return getUser();
    }
    //endregion

    //region > timestamp (property)

    public static class TimestampDomainEvent extends PropertyDomainEvent<Timestamp> {
    }

    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = TimestampDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES
    )
    @Getter @Setter
    private Timestamp timestamp;

    //endregion

    //region > transactionId (property)

    public static class TransactionIdDomainEvent extends PropertyDomainEvent<UUID> {
    }

    /**
     * The unique identifier (a GUID) of the interaction in which this audit entry was persisted.
     *
     * <p>
     * The combination of (({@link #getTransactionId() transactionId}, {@link #getSequence() sequence}) makes up the
     * unique transaction identifier.
     * </p>
     *
     * <p>
     * The combination of ({@link #getTransactionId() transactionId}, {@link #getSequence()}, {@link #getTargetStr() target}, {@link #getPropertyId() propertyId} ) makes up the
     * alternative key.
     * </p>
     */
    @javax.jdo.annotations.Column(allowsNull="false", length=JdoColumnLength.TRANSACTION_ID)
    @Property(
            domainEvent = TransactionIdDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES,
            typicalLength = 36
    )
    @Getter @Setter
    private UUID transactionId;

    //endregion

    //region > sequence (property)

    public static class SequenceDomainEvent extends PropertyDomainEvent<UUID> {
    }

    /**
     * The 0-based sequence number of the transaction in which this audit entry was persisted.
     *
     * <p>
     * The combination of (({@link #getTransactionId() transactionId}, {@link #getSequence() sequence}) makes up the
     * unique transaction identifier.
     * </p>
     *
     * <p>
     * The combination of (({@link #getTransactionId() transactionId}, {@link #getSequence() sequence}, {@link #getTargetStr() target}, {@link #getPropertyId() propertyId} ) makes up the
     * alternative key.
     * </p>
     */
    @javax.jdo.annotations.Column(allowsNull="false")
    @Property(
            domainEvent = SequenceDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES
    )
    @Getter @Setter
    private int sequence;

    //endregion

    //region > targetClass (property)

    public static class TargetClassDomainEvent extends PropertyDomainEvent<String> {
    }

    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.TARGET_CLASS)
    @Property(
            domainEvent = TargetClassDomainEvent.class
    )
    @PropertyLayout(
            named = "Class",
            typicalLength = 30
    )
    @Getter
    private String targetClass;

    public void setTargetClass(final String targetClass) {
        this.targetClass = Util.abbreviated(targetClass, JdoColumnLength.TARGET_CLASS);
    }

    //endregion

    //region > targetStr (property)

    public static class TargetStrDomainEvent extends PropertyDomainEvent<String> {
    }

    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.BOOKMARK, name="target")
    @Property(
            domainEvent = TargetStrDomainEvent.class
    )
    @PropertyLayout(
            named = "Object"
    )
    @Getter @Setter
    private String targetStr;
    //endregion

    //region > memberIdentifier (property)

    public static class MemberIdentifierDomainEvent extends PropertyDomainEvent<String> {
    }

    /**
     * This is the fully-qualified class and property Id, as per
     * {@link Identifier#toClassAndNameIdentityString()}.
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.MEMBER_IDENTIFIER)
    @Property(
            domainEvent = MemberIdentifierDomainEvent.class
    )
    @PropertyLayout(
            typicalLength = 60,
            hidden = Where.ALL_TABLES
    )
    @Getter
    private String memberIdentifier;

    public void setMemberIdentifier(final String memberIdentifier) {
        this.memberIdentifier = Util.abbreviated(memberIdentifier, JdoColumnLength.MEMBER_IDENTIFIER);
    }
    //endregion

    //region > propertyId (property)

    public static class PropertyIdDomainEvent extends PropertyDomainEvent<String> {
    }

    /**
     * This is the property name (without the class).
     */
    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.AuditEntry.PROPERTY_ID)
    @Property(
            domainEvent = PropertyIdDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.NOWHERE
    )
    @Getter
    private String propertyId;

    public void setPropertyId(final String propertyId) {
        this.propertyId = Util.abbreviated(propertyId, JdoColumnLength.AuditEntry.PROPERTY_ID);
    }

    //endregion

    //region > preValue (property)

    public static class PreValueDomainEvent extends PropertyDomainEvent<String> {
    }

    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.AuditEntry.PROPERTY_VALUE)
    @Property(
            domainEvent = PreValueDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.NOWHERE
    )
    @Getter
    private String preValue;

    public void setPreValue(final String preValue) {
        this.preValue = Util.abbreviated(preValue, JdoColumnLength.AuditEntry.PROPERTY_VALUE);
    }
    //endregion

    //region > postValue (property)

    public static class PostValueDomainEvent extends PropertyDomainEvent<String> {
    }

    @javax.jdo.annotations.Column(allowsNull="true", length=JdoColumnLength.AuditEntry.PROPERTY_VALUE)
    @Property(
            domainEvent = PostValueDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.NOWHERE
    )
    @Getter
    private String postValue;

    public void setPostValue(final String postValue) {
        this.postValue = Util.abbreviated(postValue, JdoColumnLength.AuditEntry.PROPERTY_VALUE);
    }

    //endregion

    //region > helpers: toString

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "timestamp,user,targetStr,memberIdentifier");
    }
    //endregion

}
