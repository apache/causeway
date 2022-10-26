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
package org.apache.causeway.core.config.messages;

import java.util.List;

import org.apache.causeway.commons.internal.collections._Lists;

public class MessageRegistry {

    public static final String MSG_ARE_YOU_SURE = "Are you sure?";
    public static final String MSG_CONFIRM = "Confirm";
    public static final String MSG_CANCEL = "Cancel";

    public MessageRegistry(){}

    public List<String> listMessages() {
        return _Lists.of(
                MSG_ARE_YOU_SURE,
                MSG_CONFIRM,
                MSG_CANCEL);
    }


}
