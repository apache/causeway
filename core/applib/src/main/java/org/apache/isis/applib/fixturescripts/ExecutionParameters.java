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
package org.apache.isis.applib.fixturescripts;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.CharSource;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

public class ExecutionParameters {

    private final static Pattern keyEqualsValuePattern = Pattern.compile("([^=]*)=(.*)");

    private final String parameters;
    private final Map<String, String> parameterMap;

    public ExecutionParameters(final String parameters) {
        this.parameters = parameters;
        this.parameterMap = asKeyValueMap(parameters);
    }

    static Map<String, String> asKeyValueMap(final String parameters) {
        final Map<String, String> keyValues = Maps.newLinkedHashMap();
        if (parameters != null) {
            try {
                final ImmutableList<String> lines = CharSource.wrap(parameters).readLines();
                for (final String line : lines) {
                    if (line == null) {
                        continue;
                    }
                    final Matcher matcher = keyEqualsValuePattern.matcher(line);
                    if (matcher.matches()) {
                        keyValues.put(matcher.group(1).trim(), matcher.group(2).trim());
                    }
                }
            } catch (final IOException e) {
                // ignore, shouldn't happen
            }
        }
        return keyValues;
    }

    public String getParameters() {
        return parameters;
    }

    public String getParameter(final String parameterName) {
        return parameterMap.get(parameterName);
    }

    public <T> T getParameterAsT(final String parameterName, final Class<T> cls) {
        T value = null;
        if (Enum.class.isAssignableFrom(cls)) {
            Class enumClass = cls;
            value = (T) getParameterAsEnum(parameterName, enumClass);
        } else if (cls == Boolean.class) {
            value = (T) getParameterAsBoolean(parameterName);
        } else if (cls == Byte.class) {
            value = (T) getParameterAsByte(parameterName);
        } else if (cls == Short.class) {
            value = (T) getParameterAsShort(parameterName);
        } else if (cls == Integer.class) {
            value = (T) getParameterAsInteger(parameterName);
        } else if (cls == Long.class) {
            value = (T) getParameterAsLong(parameterName);
        } else if (cls == Float.class) {
            value = (T) getParameterAsFloat(parameterName);
        } else if (cls == Double.class) {
            value = (T) getParameterAsDouble(parameterName);
        } else if (cls == Character.class) {
            value = (T) getParameterAsCharacter(parameterName);
        } else if (cls == BigDecimal.class) {
            value = (T) getParameterAsBigDecimal(parameterName);
        } else if (cls == BigInteger.class) {
            value = (T) getParameterAsBigInteger(parameterName);
        } else if (cls == LocalDate.class) {
            value = (T) getParameterAsLocalDate(parameterName);
        } else if (cls == LocalDateTime.class) {
            value = (T) getParameterAsLocalDateTime(parameterName);
        } else if (cls == String.class) {
            value = (T) getParameter(parameterName);
        }
        return value;
    }

    public Boolean getParameterAsBoolean(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Boolean.valueOf(value);
    }

    public Byte getParameterAsByte(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Byte.valueOf(value);
    }

    public Short getParameterAsShort(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Short.valueOf(value);
    }

    public Integer getParameterAsInteger(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Integer.valueOf(value);
    }

    public Long getParameterAsLong(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    public Float getParameterAsFloat(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Float.valueOf(value);
    }

    public Double getParameterAsDouble(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Double.valueOf(value);
    }

    public Character getParameterAsCharacter(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Character.valueOf(value.charAt(0));
    }

    public BigInteger getParameterAsBigInteger(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return new BigInteger(value);
    }

    public BigDecimal getParameterAsBigDecimal(final String parameterName) {
        final String value = getParameter(parameterName);
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    public LocalDate getParameterAsLocalDate(final String parameterName) {
        final String value = getParameter(parameterName);
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value);
    }

    public LocalDateTime getParameterAsLocalDateTime(final String parameterName) {
        final String value = getParameter(parameterName);
        if (value == null) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    public <T extends Enum<T>> T getParameterAsEnum(final String parameterName, final Class<T> enumClass) {
        final String value = getParameter(parameterName);
        return valueOfElseNull(enumClass, value);
    }

    private static <T extends Enum<T>> T valueOfElseNull(final Class<T> enumClass, final String value) {
        if (value == null) {
            return null;
        }
        final T[] enumConstants = enumClass.getEnumConstants();
        for (T enumConstant : enumConstants) {
            if (enumConstant.name().equals(value)) {
                return enumConstant;
            }
        }
        return null;
    }


    public Map<String, String> getParameterMap() {
        return Collections.unmodifiableMap(parameterMap);
    }

    public void setParameterIfNotPresent(final String parameterName, final String parameterValue) {
        if (parameterName == null) {
            throw new IllegalArgumentException("parameterName required");
        }
        if (parameterValue == null) {
            // ignore
            return;
        }
        if (parameterMap.containsKey(parameterName)) {
            // ignore; the existing parameter take precedence
            return;
        }
        parameterMap.put(parameterName, parameterValue);
    }

    public void setParameter(final String parameterName, final Boolean parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Byte parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Short parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Integer parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Long parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Float parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Double parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Character parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final BigInteger parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final java.util.Date parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final java.sql.Date parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final LocalDate parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final LocalDateTime parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final DateTime parameterValue) {
        final StringBuffer buf = new StringBuffer();
        ISODateTimeFormat.dateTimeParser().printTo(buf, parameterValue);
        setParameter(parameterName, parameterValue != null ? buf.toString() : null);
    }

    public void setParameter(final String parameterName, final BigDecimal parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    public void setParameter(final String parameterName, final Enum<?> parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.name() : null);
    }

    public void setParameter(final String parameterName, final String parameterValue) {
        if (parameterName == null) {
            throw new IllegalArgumentException("parameterName required");
        }
        if (parameterValue == null) {
            // ignore
            return;
        }
        parameterMap.put(parameterName, parameterValue);
    }

}
