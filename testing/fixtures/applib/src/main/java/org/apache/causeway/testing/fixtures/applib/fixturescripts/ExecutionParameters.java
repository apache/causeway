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
package org.apache.causeway.testing.fixtures.applib.fixturescripts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Represents the execution parameters (as passed initially as a string)
 * when invoking {@link FixtureScripts#runScript(String, String)}) for
 * use by the fixtures themselves (accessible using
 * {@link org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript.ExecutionContext}).
 *
 * @since 2.0 {@index}
 */
public interface ExecutionParameters {

    String getParameters();

    Map<String, String> getParameterMap();

    String getParameter(String parameterName);

    <T> T getParameterAsT(String parameterName, Class<T> cls);

    Boolean getParameterAsBoolean(String parameterName);
    Byte getParameterAsByte(String parameterName);
    Short getParameterAsShort(String parameterName);
    Integer getParameterAsInteger(String parameterName);
    Long getParameterAsLong(String parameterName);
    Float getParameterAsFloat(String parameterName);
    Double getParameterAsDouble(String parameterName);
    Character getParameterAsCharacter(String parameterName);
    BigInteger getParameterAsBigInteger(String parameterName);
    BigDecimal getParameterAsBigDecimal(String parameterName);
    LocalDate getParameterAsLocalDate(String parameterName);
    LocalDateTime getParameterAsLocalDateTime(String parameterName);

    <T extends Enum<T>> T getParameterAsEnum(String parameterName, Class<T> enumClass);

    void setParameterIfNotPresent(String parameterName, String parameterValue);

    void setParameter(String parameterName, Boolean parameterValue);
    void setParameter(String parameterName, Byte parameterValue);
    void setParameter(String parameterName, Short parameterValue);
    void setParameter(String parameterName, Integer parameterValue);
    void setParameter(String parameterName, Long parameterValue);
    void setParameter(String parameterName, Float parameterValue);
    void setParameter(String parameterName, Double parameterValue);
    void setParameter(String parameterName, Character parameterValue);
    void setParameter(String parameterName, BigInteger parameterValue);
    void setParameter(String parameterName, BigDecimal parameterValue);
    void setParameter(String parameterName, LocalDate parameterValue);
    void setParameter(String parameterName, LocalDateTime parameterValue);
    void setParameter(String parameterName, DateTime parameterValue);
    void setParameter(String parameterName, java.util.Date parameterValue);
    void setParameter(String parameterName, java.sql.Date parameterValue);
    void setParameter(String parameterName, Enum<?> parameterValue);
    void setParameter(String parameterName, String parameterValue);

}
