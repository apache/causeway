/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.settings;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.MemberOrder;


/**
 * Common supertype for both {@link ApplicationSetting} and {@link UserSetting}.
 * 
 * <p>
 * The difference between the two is in the settings unique identifier; 
 * the former just is uniquely identified by a single key (defined in 
 * {@link #getKey() this interface}, whereas the latter is uniquely identified by
 * the combination of the key plus an identifier of the user.
 */
public interface Setting {

    /**
     * In the case of {@link ApplicationSetting}, this constitutes the unique identifier;
     * for a {@link UserSetting}, the unique identifier is both this key plus an identifier
     * for the user.  
     */
    String getKey();

    SettingType getType();
    String getDescription();

    String getValueRaw();

    String valueAsString();
    LocalDate valueAsLocalDate();
    Integer valueAsInt();
    Long valueAsLong();
    Boolean valueAsBoolean();
    
}
