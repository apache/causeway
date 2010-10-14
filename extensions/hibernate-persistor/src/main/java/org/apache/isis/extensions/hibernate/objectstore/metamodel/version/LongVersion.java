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


package org.apache.isis.extensions.hibernate.objectstore.metamodel.version;

import java.io.IOException;
import java.util.Date;

import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.adapter.version.VersionUserAbstract;
import org.apache.isis.metamodel.adapter.version.VersionUserAndTimeAbstract;
import org.apache.isis.metamodel.encoding.DataInputExtended;
import org.apache.isis.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.metamodel.encoding.DataOutputExtended;
import org.apache.isis.metamodel.encoding.DataOutputStreamExtended;
import org.apache.isis.metamodel.encoding.Encodable;


public class LongVersion implements Version, Encodable {
	
    private static final long serialVersionUID = 1L;
    
    private String user;
    private Date time;
    private Long versionNumber;

    public LongVersion() {
    	this.time = null;
    	initialized();
	}

    public LongVersion(final Long versionNumber, final String user, final Date time) {
    	this.user = user;
    	this.time = time;
        this.versionNumber = versionNumber;
        initialized();
    }

    public LongVersion(DataInputExtended input) throws IOException {
    	user = input.readUTF();
    	boolean hasTime = input.readBoolean();
    	if (hasTime) {
    		this.time = new Date(input.readLong());
    	}
        this.versionNumber = input.readLong();
        initialized();
    }

    public void encode(DataOutputExtended output)
    		throws IOException {
		output.writeUTF(user);
    	boolean hasTime = time == null;
    	output.writeBoolean(hasTime);
    	if (hasTime) {
    		output.writeLong(time.getTime());
    	}
    	output.writeLong(versionNumber);
    }
    
	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    // Properties
    /////////////////////////////////////////////////////////

	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public Date getTime() {
		return getTime();
	}

	public void setTime(final Date time) {
		this.time = time;
	}
	
    public Long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(final Long versionNumber) {
        this.versionNumber = versionNumber;
    }


    /////////////////////////////////////////////////////////
    // different
    /////////////////////////////////////////////////////////

    public boolean different(final Version version) {
        if (!(version instanceof LongVersion)) {
            return false;
        } 
        final LongVersion other = (LongVersion) version;
		return !versionNumber.equals(other.versionNumber);
    }


    /////////////////////////////////////////////////////////
    // equals, hashCode
    /////////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof LongVersion) {
            return !different((LongVersion) obj);
        } else {
            return false;
        }
    }


    /**
     * TODO: dubious - if {@link #setVersionNumber(Long) version number set} then will invalidate.
     */
    @Override
    public int hashCode() {
        return (int) (versionNumber.longValue() ^ (versionNumber.longValue() >>> 32));
    }


    /////////////////////////////////////////////////////////
    // toString, sequence
    /////////////////////////////////////////////////////////

    public String sequence() {
        return Long.toString(versionNumber, 16);
    }


    @Override
    public String toString() {
        return "LongVersion#" + versionNumber + " " + ToString.timestamp(getTime());
    }

}
