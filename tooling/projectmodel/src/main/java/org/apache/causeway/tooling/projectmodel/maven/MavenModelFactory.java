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
package org.apache.causeway.tooling.projectmodel.maven;

import java.io.File;
import java.io.FileReader;
import java.util.Optional;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.resolution.ModelResolver;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class MavenModelFactory {

    public static Model interpolateModel(final Model model, final ModelResolver modelResolver) {

        val pomFile = model.getPomFile();

        log.info("interpolating model {}", pomFile);

        val modelBuildRequest = new DefaultModelBuildingRequest()
        .setProcessPlugins(false)
        .setPomFile(pomFile)
        .setModelResolver(modelResolver)
        .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

        try {
            val modelBuilder = new DefaultModelBuilderFactory().newInstance();
            return modelBuilder.build(modelBuildRequest).getEffectiveModel();
        } catch (ModelBuildingException e) {
             log.warn("maven model interpolation failed {}", pomFile, e);
             //throw new RuntimeException(String.format("maven model building failed %s", pomFile), e);
        }

        // fallback to non interpolated
        return model;

    }


    /** non interpolated read */
    public static Model readModel(File pomFile) {
        val reader = new MavenXpp3Reader();
        try {
            val model =  reader.read(new FileReader(pomFile));
            model.setPomFile(pomFile);
            return model;
        } catch (Exception e) {
            log.error("failed to read {}", pomFile.getAbsolutePath(), e);
            throw new RuntimeException(String.format("failed to read %s", pomFile.getAbsolutePath()), e);
        }
    }

    public static String readArtifactKey(Model model) {
        if(model==null) {
            return null;
        }
        val artifactKey = String.format("%s:%s:%s",
                getGroupId(model),
                model.getArtifactId(),
                getVersion(model));
        return artifactKey;
    }

    public static String getGroupId(@NonNull Model model) {
        return Optional.ofNullable(model.getGroupId()).orElseGet(()->model.getParent().getGroupId());
    }

    public static String getVersion(@NonNull Model model) {
        return Optional.ofNullable(model.getVersion()).orElseGet(()->model.getParent().getVersion());
    }

}
