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
package demoapp.dom.types.isisext.sse;

import java.util.concurrent.atomic.LongAdder;

import lombok.Data;
import lombok.val;

@Data(staticConstructor="of")
public class TaskProgress {

    private final LongAdder stepsProgressed;
    private final long totalSteps;

    public double progressedRelative() {
        val totalReciprocal = 1./totalSteps;

        return stepsProgressed.doubleValue() * totalReciprocal;
    }

    public double progressedPercent() {
        return Math.min(progressedRelative()*100., 100.);
    }

    public int progressedPercentAsInt() {
        return (int) Math.round(progressedPercent());
    }

    public String toHtmlProgressBar() {
        val percent = progressedPercentAsInt();

        return
                "<div class=\"progress\">" +
                "  <div class=\"progress-bar\" role=\"progressbar\" style=\"width: "+percent+"%\" aria-valuenow=\""+percent+"\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div>" +
                "</div>";
    }


}
