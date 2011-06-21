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
package org.apache.isis.viewer.restful.viewer2.resources.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.restful.applib.resources.BootstrapResource;
import org.apache.isis.viewer.restful.applib.resources.HomePageResource;
import org.apache.isis.viewer.restful.viewer2.resources.ResourceAbstract;

import com.google.common.io.ByteStreams;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than the
 * interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/")
public class BootstrapResourceImpl extends ResourceAbstract implements BootstrapResource {


    @Override
    @Produces(MediaType.TEXT_HTML)
    public String bootstrap() {
        init();
        try {
            InputStream resourceAsStream = BootstrapResourceImpl.class.getResourceAsStream("/index.html");
            byte[] byteArray = ByteStreams.toByteArray(resourceAsStream);
            return new String(byteArray, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}