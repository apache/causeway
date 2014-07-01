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

package org.apache.isis.objectstore.nosql.versions;

import java.util.Date;

import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;

public class VersionCreatorDefault implements VersionCreator {

    @Override
    public String versionString(final Version version) {
        final long sequence = version.getSequence();
        return Long.toHexString(sequence);
    }

    @Override
    public String timeString(final Version version) {
        final Date time = version.getTime();
        return Long.toHexString(time.getTime());
    }

    @Override
    public Version version(final String versionString, final String user, final String timeString) {
        final Long sequence = Long.valueOf(versionString, 16);
        final Long time = Long.valueOf(timeString, 16);
        final Date date = new Date(time);
        return SerialNumberVersion.create(sequence, user, date);
    }

    @Override
    public Version newVersion(final String user) {
        return SerialNumberVersion.create(1, user, new Date());
    }

    @Override
    public Version nextVersion(final Version version, final String user) {
        final long sequence = version.getSequence() + 1;
        return SerialNumberVersion.create(sequence, user, new Date());
    }
}
