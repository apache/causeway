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
package org.apache.isis.extensions.commandlog.impl.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BigDecimalUtils {

    /**
     * @return in seconds, to 3 decimal places.
     */
    public static BigDecimal durationBetween(Timestamp startedAt, Timestamp completedAt) {
        if (completedAt == null) {
            return null;
        } else {
            long millis = completedAt.getTime() - startedAt.getTime();
            return toSeconds(millis);
        }
    }

    private static final BigDecimal DIVISOR = new BigDecimal(1000);

    private static BigDecimal toSeconds(long millis) {
        return new BigDecimal(millis)
                .divide(DIVISOR, RoundingMode.HALF_EVEN)
                .setScale(3, RoundingMode.HALF_EVEN);
    }

}
