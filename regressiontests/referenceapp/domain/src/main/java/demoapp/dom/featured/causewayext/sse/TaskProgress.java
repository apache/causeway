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
package demoapp.dom.featured.causewayext.sse;

import java.util.concurrent.atomic.LongAdder;

import lombok.Data;

//tag::class[]
@Data(staticConstructor="of")
public class TaskProgress {

    private final LongAdder stepsProgressed;
    private final long totalSteps;

    public double progressedRelative() {
        final double totalReciprocal = 1. / totalSteps;
        return stepsProgressed.doubleValue() * totalReciprocal;
    }
    public double progressedPercent() {
        return Math.min(progressedRelative()*100., 100.);
    }
    public int progressedPercentAsInt() {
        return (int) Math.round(progressedPercent());
    }
//tag::toHtmlProgressBar[]
    public String toHtmlProgressBar() {
        final int percent = progressedPercentAsInt();
        return stepsProgressed + "/" + totalSteps +
                "<br/>" +
                "<br/>" +
                "<div class=\"progress\">" +
                "    <div class=\"progress-bar\" " +
                         "role=\"progressbar\" " +
                         "style=\"width: " + percent + "%\" " +
                         "aria-valuenow=\""+percent+"\" " +
                         "aria-valuemin=\"0\" " +
                         "aria-valuemax=\"100\">" +
                    "</div>" +
                "</div>";
    }
//end::toHtmlProgressBar[]
}
//end::class[]
