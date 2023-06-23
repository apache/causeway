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
package org.apache.causeway.viewer.wicket.ui.components.scalars.datepicker;

import lombok.experimental.UtilityClass;

@UtilityClass
class _TimeFormatUtil {

    /**
     * Java time-zone formats<pre>
     * V       time-zone ID                zone-id           America/Los_Angeles; Z; -08:30
     * z       time-zone name              zone-name         Pacific Standard Time; PST
     * O       localized zone-offset       offset-O          GMT+8; GMT+08:00; UTC-08:00;
     * X       zone-offset 'Z' for zero    offset-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
     * x       zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
     * Z       zone-offset                 offset-Z          +0000; -0800; -08:00;
     * </pre>
     * <p>
     * momentJs<pre>
     * Z ZZ +12:00  Offset from UTC as +-HH:mm, +-HHmm, or Z
     * </pre>
     */
    String convertToMomentJsFormat(final String javaDateTimeFormat) {
        String momentJsFormat = javaDateTimeFormat;
        momentJsFormat = momentJsFormat.replace('d', 'D');
        momentJsFormat = momentJsFormat.replace('y', 'Y');

        // time-zone format conversion: basically convert anything to Z or ZZ
        momentJsFormat = momentJsFormat.replace('x', 'Z');
        momentJsFormat = momentJsFormat.replace('X', 'Z');
        // trim down any occurrence of ZZ* to just ZZ
        while(momentJsFormat.contains("ZZZ")) {
            momentJsFormat = momentJsFormat.replace("ZZZ", "ZZ");
        }

        return momentJsFormat.trim();
    }

}
