package org.apache.isis.core.metamodel.adapter.oid;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.version.Version;

public final class OidVersion implements Encodable, Serializable, Version {
    
    private static final long serialVersionUID = 1L;
    
    private final Long sequence;
    private final String user;
    private final Long utcTimestamp;

    // ///////////////////////////////////////////////////////
    // factory methods
    // ///////////////////////////////////////////////////////

    public static OidVersion create(String sequence, String user, String utcTimestamp) {
        if(sequence == null) { 
            return null;
        }
        return create(Long.parseLong(sequence), user, utcTimestamp != null?Long.parseLong(utcTimestamp):null);
    }

    public static OidVersion create(final Long sequence, final String user, final Date time) {
        return create(sequence, user, time !=null? time.getTime(): null);
    }

    public static OidVersion create(Long sequence, String user, Long utcTimestamp) {
        if(sequence == null) { 
            return null;
        }
        return new OidVersion(sequence, user, utcTimestamp);
    }

    private OidVersion(Long sequence, String user, Long utcTimestamp) {
        this.sequence = sequence;
        this.user = user;
        this.utcTimestamp = utcTimestamp;
    }

    
    // ///////////////////////////////////////////////////////
    // encodable
    // ///////////////////////////////////////////////////////

    public OidVersion(final DataInputExtended input) throws IOException {
        this(input.readLong(), input.readUTF(), input.readLong());
    }
    
    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeLong(sequence);
        output.writeUTF(user);
        output.writeLong(utcTimestamp);
    }


    // ///////////////////////////////////////////////////////
    // getters
    // ///////////////////////////////////////////////////////

    public long getSequence() {
        return sequence;
    }
    
    public String getUser() {
        return user;
    }
    
    public Long getUtcTimestamp() {
        return utcTimestamp;
    }

    @Override
    public Date getTime() {
        return new Date(this.utcTimestamp);
    }


    // ///////////////////////////////////////////////////////
    // enString
    // ///////////////////////////////////////////////////////

    public String enString(OidMarshaller oidMarshaller) {
        return oidMarshaller.marshal(this);
    }

    // ///////////////////////////////////////////////////////
    // equals, hashCode
    // ///////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OidVersion other = (OidVersion) obj;
        if (sequence == null) {
            if (other.sequence != null)
                return false;
        } else if (!sequence.equals(other.sequence))
            return false;
        return true;
    }

    @Override
    public boolean different(Version version) {
        return !equals(this);
    }
    
    
    //////////////////////////////////////////////////////////////
    // sequence
    //////////////////////////////////////////////////////////////
    
 
    @Override
    public String toString() {
        return "#" + sequence + " " + getUser() + " " + ToString.timestamp(getTime());
    }
    
    @Override
    public String sequence() {
        return Long.toString(sequence, 16);
    }


}