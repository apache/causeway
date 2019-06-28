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

package org.apache.isis.core.webserver;

import java.io.IOException;
import java.util.Arrays;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class WebServer {

    public static void main(final String[] args) {
        new WebServer().run(args);
    }

    public void run(final String[] args) {

        final OptionParser parser = new OptionParser();

        final OptionSpec<Class> appManifestClassOptSpec =
                parser.acceptsAll( Arrays.asList("m", "manifest", "appManifest"), "AppManifest implementation" )
                        .withRequiredArg()
                        .withValuesConvertedBy(new ValueConverter<Class>() {
                            @Override public Class convert(final String className) {
                                try {
                                    final Class<?> aClass = _Context.getDefaultClassLoader().loadClass(className);
                                    _Exceptions.throwNotImplemented();
//                                    if(!AppManifest.class.isAssignableFrom(aClass)) {
//                                        throw new ValueConversionException("not a subclass of " + AppManifest.class.getName() );
//                                    }
                                    return aClass;
                                } catch (ClassNotFoundException e) {
                                    throw new ValueConversionException("not recognised as a class");
                                }
                            }

                            @Override public Class<? extends Class> valueType() {
                                return Class.class;
                            }

                            @Override public String valuePattern() {
                                return null;
                            }
                        });

        final OptionSpec<Integer> portOptSpec =
                parser.acceptsAll( Arrays.asList("p", "port"), "port to listen on" )
                        .withRequiredArg()
                        .ofType( Integer.class )
                        .defaultsTo(8080);
        final OptionSpec<Void> prototypeOptSpec =
                parser.acceptsAll( Arrays.asList("d", "dev", "prototype"), "Prototype mode" );

        final OptionSet optionSet = parser.parse(args);
        try {
            optionSet.valueOfOptional(appManifestClassOptSpec).ifPresent(
                    appManifestClass -> System.setProperty("isis.appManifest", appManifestClass.getName()));
            if(optionSet.has(prototypeOptSpec)) {
                System.setProperty("isis.deploymentType", "PROTOTYPING");
            }
        } catch (Exception ex) {
            try {
                parser.printHelpOn(System.err);
                System.err.println();
                System.err.print(ex.getMessage());
                final Throwable cause = ex.getCause();
                if(cause != null) {
                    System.err.println(": " + cause.getMessage());
                    System.err.println();
                }
            } catch (IOException e) {
                // ignore
            }
            System.exit(1);
        }

        final int port = optionSet.valueOf(portOptSpec);
        // create and start
        log.info("Running Jetty on port '{}' to serve the web application", port);

        final Server jettyServer = new Server(port);
        final WebAppContext context = new WebAppContext("src/main/webapp", "");
        jettyServer.setHandler(context);

        start(jettyServer);
    }

    private void start(final Server jettyServer) {
        long start = System.currentTimeMillis();
        try {
            jettyServer.start();
            log.info("Started the application in {}ms", System.currentTimeMillis() - start);

        } catch (final Exception ex) {
            try {
                jettyServer.stop();
                jettyServer.join();
            } catch (Exception e) {
                // ignore
            }
            throw new IsisException("Unable to start Jetty server", ex);
        }
        try {
            System.in.read();
            System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
            jettyServer.stop();
            jettyServer.join();

        } catch (final Exception ex) {
            throw new IsisException("Unable to stop Jetty server", ex);
        }
    }
}
