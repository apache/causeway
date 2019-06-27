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

package org.apache.isis.runtime.headless.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class LeveledLogger {

    private final Logger logger;
    private Level level;

    public LeveledLogger(final Logger logger, final Level level) {
        this.logger = logger;
        this.level = level;
    }

    public void log(final String message) {
    	logger.log(level, message);
    	
//[2112]    	
//        switch (level.) {
//            case ERROR:
//                logger.error(message);
//                break;
//            case WARN:
//                logger.warn(message);
//                break;
//            case INFO:
//                logger.info(message);
//                break;
//            case DEBUG:
//                logger.debug(message);
//                break;
//            case TRACE:
//                logger.trace(message);
//                break;
//        }
    }

}
