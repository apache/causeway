/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.executionoutbox.restclient.api.delete;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.apache.causeway.extensions.executionoutbox.restclient.api.Jsonable;

import lombok.Getter;

public class DeleteMessage implements Jsonable {

    private static final ObjectWriter writer;

    static {
        final ObjectMapper mapper = new ObjectMapper();
        writer = mapper.writer().withDefaultPrettyPrinter();
    }

    @Getter
    private final StringValue interactionId;
    @Getter
    private final IntValue sequence;

    public DeleteMessage(final String interactionId, final int sequence) {
        this.interactionId = new StringValue(interactionId);
        this.sequence = new IntValue(sequence);
    }

    public String asJson() {
        try {
            return writer.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "[DELETE MESSAGE] \n" +
                "interactionId: " + interactionId + "\n" +
                "sequence     : " + sequence + "\n";
    }

}
