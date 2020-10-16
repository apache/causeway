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
package org.apache.isis.viewer.restfulobjects.applib.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Represents a nullable scalar value,
 * as used by ContentNegotiationServiceOrgApacheIsisV2 and its clients.
 * @since Oct 16, 2020
 */
@JsonIgnoreProperties({"links", "extensions"})
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalarValueDtoV2 {
   
   public static ScalarValueDtoV2 forNull(@NonNull Class<?> type) {
       return new ScalarValueDtoV2(type.getSimpleName(), null);
   }
    
   public static ScalarValueDtoV2 forValue(@NonNull Object value) {
       return new ScalarValueDtoV2(value.getClass().getSimpleName(), value);
   }
   
   private String type;
   private Object value;
   
   @JsonIgnore
   public boolean isNull() {
       return value == null;
   }
   
}