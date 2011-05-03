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

package org.apache.isis.runtimes.dflt.runtime.testsystem;

import java.util.Date;

import org.apache.isis.core.metamodel.adapter.version.Version;

public class TestProxyVersion implements Version {
    private static final long serialVersionUID = 1L;
    private final int value;

    public TestProxyVersion() {
        this(1);
    }

    public TestProxyVersion(final int value) {
        this.value = value;
    }

    @Override
    public boolean different(final Version version) {
        return value != ((TestProxyVersion) version).value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestProxyVersion other = (TestProxyVersion) obj;
        if (value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Version#" + value;
    }

    @Override
    public String getUser() {
        return "USER";
    }

    @Override
    public Date getTime() {
        return new Date(0);
    }

    public Version next() {
        return new TestProxyVersion(value + 1);
    }

    @Override
    public String sequence() {
        return "" + value;
    }

}
