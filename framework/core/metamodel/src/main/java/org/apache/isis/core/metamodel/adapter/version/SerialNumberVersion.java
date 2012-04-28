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

package org.apache.isis.core.metamodel.adapter.version;

import java.io.IOException;
import java.util.Date;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.lang.ToString;

public class SerialNumberVersion extends VersionUserAndTimeAbstract {

    private static final long serialVersionUID = 1L;
    private final long versionNumber;

    public SerialNumberVersion(final long number, final String user, final Date time) {
        super(user, time);
        this.versionNumber = number;
        initialized();
    }

    public SerialNumberVersion(final DataInputExtended input) throws IOException {
        super(input);
        this.versionNumber = input.readLong();
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeLong(versionNumber);
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////

    public long getSequence() {
        return versionNumber;
    }

    @Override
    public String sequence() {
        return Long.toString(versionNumber, 16);
    }

    @Override
    public boolean different(final Version version) {
        if (version instanceof SerialNumberVersion) {
            final SerialNumberVersion other = (SerialNumberVersion) version;
            return versionNumber != other.versionNumber;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof SerialNumberVersion) {
            return !different((SerialNumberVersion) obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) (versionNumber ^ (versionNumber >>> 32));
    }

    @Override
    public String toString() {
        return "SerialNumberVersion#" + versionNumber + " " + ToString.timestamp(getTime());
    }

}
