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
package org.apache.causeway.applib.services.jaxb;

import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;

/**
 * A {@link StreamResult} that contains its own writer.
 *
 * <p>
 *     The point is that the writer is only ever queried lazily AFTER the result has been generated.
 * </p>
 */
class StreamResultWithWriter extends StreamResult {
    private final StringWriter writer;

    public StreamResultWithWriter() {
        this(new StringWriter());
    }

    private StreamResultWithWriter(StringWriter writer) {
        super(writer);
        this.writer = writer;
    }

    public String asString() {
        return writer.toString();
    }
}
