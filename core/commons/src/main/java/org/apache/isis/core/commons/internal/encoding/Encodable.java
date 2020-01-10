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

package org.apache.isis.core.commons.internal.encoding;

import java.io.IOException;

/**
 * This interface indicates that an object can be encoded into into a byte array
 * so it can be streamed.
 *
 * <p>
 * By implementing this interface you are agreeing to provide a constructor with
 * a single argument of type {@link DataInputExtended}, which create an instance
 * from the stream.
 */
public interface Encodable {

    /**
     * Returns the domain object's value as an encoded byte array via the
     * encoder.
     */
    void encode(DataOutputExtended outputStream) throws IOException;
}
