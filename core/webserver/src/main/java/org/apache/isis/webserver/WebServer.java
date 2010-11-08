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


package org.apache.isis.webserver;



import org.apache.isis.core.commons.lang.ArrayUtils;
import org.apache.isis.runtime.runner.IsisRunner;
import org.apache.isis.webserver.internal.OptionHandlerAddress;
import org.apache.isis.webserver.internal.OptionHandlerDeploymentTypeWebServer;
import org.apache.isis.webserver.internal.OptionHandlerPort;
import org.apache.isis.webserver.internal.OptionHandlerResourceBase;


public class WebServer {

    public static void main(String[] args) {
        new WebServer().run(ArrayUtils.append(args, "--nosplash"));
    }
    
	public void run(final String[] args) {
	    final IsisRunner runner = new IsisRunner(args, new OptionHandlerDeploymentTypeWebServer());

		// adjustments
	    runner.addOptionHandler(new OptionHandlerPort());
	    runner.addOptionHandler(new OptionHandlerAddress());
	    runner.addOptionHandler(new OptionHandlerResourceBase());
        
        if (!runner.parseAndValidate()) {
            return;
        }

        runner.bootstrap(new WebServerBootstrapper(runner));
	}

}
