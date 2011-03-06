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


package org.apache.isis.runtimes.dflt.objectstores.nosql;

import java.util.Date;

import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;

public class SerialNumberVersionCreator implements VersionCreator {

    public String versionString(Version version) {
        long sequence = ((SerialNumberVersion) version).getSequence();
        return Long.toHexString(sequence);
    }
    
    public String timeString(Version version) {
        Date time = version.getTime();
        return Long.toHexString(time.getTime());
    }

    public Version version(String versionString, String user, String timeString) {
        Long sequence = Long.valueOf(versionString, 16);
        Long time = Long.valueOf(timeString, 16);
        Date date = new Date(time);
        return new SerialNumberVersion(sequence, user, date);
    }

    public Version newVersion(String user) {
        return new SerialNumberVersion(1, user, new Date());
    }
    
    public Version nextVersion(Version version) {
        long sequence = ((SerialNumberVersion) version).getSequence() + 1;
        String user = version.getUser();
        return new SerialNumberVersion(sequence, user, new Date());
    }
}


