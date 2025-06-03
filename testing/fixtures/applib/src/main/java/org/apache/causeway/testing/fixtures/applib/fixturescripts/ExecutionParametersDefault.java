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
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.io.TextUtils;

/**
 * Responsible for parsing the string parameter passed when executing
 * fixtures through the UI to the {@link FixtureScripts} domain service.
 *
 * <p>
 *     The class is instantiated by the {@link ExecutionParametersService}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public class ExecutionParametersDefault implements ExecutionParameters {

    private static final Pattern keyEqualsValuePattern = Pattern.compile("([^=]*)=(.*)");

    private final String parameters;
    private final Map<String, String> parameterMap;

    public ExecutionParametersDefault(final String parameters) {
        this.parameters = parameters;
        this.parameterMap = asKeyValueMap(parameters);
    }

    static Map<String, String> asKeyValueMap(final String parameters) {
        final Map<String, String> keyValues = _Maps.newLinkedHashMap();
        if (parameters != null) {
            try {
                TextUtils.streamLines(parameters)
                .forEach(line->{
                    final Matcher matcher = keyEqualsValuePattern.matcher(line);
                    if (matcher.matches()) {
                        keyValues.put(matcher.group(1).trim(), matcher.group(2).trim());
                    }
                });
            } catch (final Exception e) {
                // ignore, shouldn't happen
            }
        }
        return keyValues;
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    @Override
    public String getParameter(final String parameterName) {
        return parameterMap.get(parameterName);
    }

    @Override
    public <T> T getParameterAsT(final String parameterName, final Class<T> cls) {
        return _Casts.uncheckedCast(getParameterAsObject(parameterName, cls));
    }

    protected Object getParameterAsObject(final String parameterName, final Class<?> cls) {
        final Object value;
        if (Enum.class.isAssignableFrom(cls)) {
            Class<?> enumClass = cls;
            value = getParameterAsEnum(parameterName, _Casts.uncheckedCast(enumClass));
        } else if (cls == Boolean.class) {
            value = getParameterAsBoolean(parameterName);
        } else if (cls == Byte.class) {
            value = getParameterAsByte(parameterName);
        } else if (cls == Short.class) {
            value = getParameterAsShort(parameterName);
        } else if (cls == Integer.class) {
            value = getParameterAsInteger(parameterName);
        } else if (cls == Long.class) {
            value = getParameterAsLong(parameterName);
        } else if (cls == Float.class) {
            value = getParameterAsFloat(parameterName);
        } else if (cls == Double.class) {
            value = getParameterAsDouble(parameterName);
        } else if (cls == Character.class) {
            value = getParameterAsCharacter(parameterName);
        } else if (cls == BigDecimal.class) {
            value = getParameterAsBigDecimal(parameterName);
        } else if (cls == BigInteger.class) {
            value = getParameterAsBigInteger(parameterName);
        } else if (cls == LocalDate.class) {
            value = getParameterAsLocalDate(parameterName);
        } else if (cls == LocalDateTime.class) {
            value = getParameterAsLocalDateTime(parameterName);
        } else if (cls == String.class) {
            value = getParameter(parameterName);
        } else {
            value = null;
        }
        return value;
    }

    @Override
    public Boolean getParameterAsBoolean(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Boolean.valueOf(value);
    }

    @Override
    public Byte getParameterAsByte(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Byte.valueOf(value);
    }

    @Override
    public Short getParameterAsShort(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Short.valueOf(value);
    }

    @Override
    public Integer getParameterAsInteger(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Integer.valueOf(value);
    }

    @Override
    public Long getParameterAsLong(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    @Override
    public Float getParameterAsFloat(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Float.valueOf(value);
    }

    @Override
    public Double getParameterAsDouble(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Double.valueOf(value);
    }

    @Override
    public Character getParameterAsCharacter(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return Character.valueOf(value.charAt(0));
    }

    @Override
    public BigInteger getParameterAsBigInteger(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return new BigInteger(value);
    }

    @Override
    public BigDecimal getParameterAsBigDecimal(final String parameterName) {
        final String value = getParameter(parameterName);
        if (_Strings.isNullOrEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    @Override
    public LocalDate getParameterAsLocalDate(final String parameterName) {
        final String value = getParameter(parameterName);
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value);
    }

    @Override
    public LocalDateTime getParameterAsLocalDateTime(final String parameterName) {
        final String value = getParameter(parameterName);
        if (value == null) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    @Override
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

    @Override
    public Map<String, String> getParameterMap() {
        return Collections.unmodifiableMap(parameterMap);
    }

    @Override
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

    @Override
    public void setParameter(final String parameterName, final Boolean parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Byte parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Short parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Integer parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Long parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Float parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Double parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Character parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final BigInteger parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final java.util.Date parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final java.sql.Date parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final LocalDate parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final LocalDateTime parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final OffsetDateTime parameterValue) {
        setParameter(parameterName, parameterValue != null ? DateTimeFormatter.ISO_DATE_TIME.format(parameterValue) : null);
    }

    @Override
    public void setParameter(final String parameterName, final ZonedDateTime parameterValue) {
        setParameter(parameterName, parameterValue != null ? DateTimeFormatter.ISO_DATE_TIME.format(parameterValue) : null);
    }

    @Override
    public void setParameter(final String parameterName, final BigDecimal parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.toString() : null);
    }

    @Override
    public void setParameter(final String parameterName, final Enum<?> parameterValue) {
        setParameter(parameterName, parameterValue != null ? parameterValue.name() : null);
    }

    @Override
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
