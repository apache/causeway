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
package org.apache.isis.extensions.commandreplay.secondary.analysis;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.stereotype.Service;

import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.extensions.commandreplay.secondary.analyser.CommandReplayAnalyser;

import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.commandReplaySecondary.CommandReplayAnalysisService")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Log4j2
public class CommandReplayAnalysisService {

    /**
     * if hit an issue with the command having been replayed, then mark this
     * as in error.
     * This will effectively block the running of any further commands until the administrator fixes the issue.
     */
    public void analyse(final CommandModel commandModel) {
        final String analysis = analyseReplay(commandModel);

        commandModel.saveAnalysis(analysis);
    }

    private String analyseReplay(final CommandModel commandJdo) {

        for (final CommandReplayAnalyser analyser : analysers) {
            try {
                String reason = analyser.analyzeReplay(commandJdo);
                if (reason != null) {
                    return reason;
                }
            } catch(Exception ex) {
                final String className = analyser.getClass().getName();
                log.warn("{} threw exception: ", className, ex);
                return className + " threw exception: " + ex.getMessage();
            }
        }
        return null;
    }

    @Inject List<CommandReplayAnalyser> analysers;

}
