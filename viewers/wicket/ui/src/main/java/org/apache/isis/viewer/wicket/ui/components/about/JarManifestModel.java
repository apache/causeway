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
package org.apache.isis.viewer.wicket.ui.components.about;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.commons.CloseableExtensions;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;

public class JarManifestModel extends ModelAbstract<JarManifestModel> {

    private static final long serialVersionUID = 1L;

//    private static final List<String> VERSION_KEY_CANDIDATES = 
//            Arrays.asList("Implementation-Version", "Build-Time");

    private final List<JarManifestAttributes> manifests = _Lists.newArrayList();

    /**
     * @param commonContext 
     * @param metaInfManifestIs provide using <tt>getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF")</tt>
     */
    public JarManifestModel(IsisAppCommonContext commonContext, InputStream metaInfManifestIs) {

        super(commonContext);
        
        Manifest manifest;
        try {
            manifest = new Manifest(metaInfManifestIs);
            manifests.add(JarManifestAttributes.jarName("Web archive (war file)"));
            manifests.add(JarManifestAttributes.jarUrl(null));
            addAttributes(manifest, manifests);

        } catch (Exception ex) {
            // ignore
        } finally {
            CloseableExtensions.closeSafely(metaInfManifestIs);
        }

        Enumeration<?> resEnum;
        try {
            resEnum = _Context.getDefaultClassLoader().getResources(JarFile.MANIFEST_NAME);
        } catch (IOException e) {
            return;
        }
        final List<JarManifest> jarManifests = _Lists.newArrayList();
        while (resEnum.hasMoreElements()) {
            URL url = (URL)resEnum.nextElement();
            JarManifest jarManifest = new JarManifest(url);
            jarManifests.add(jarManifest);

            InputStream is = null;
            try {
                is = url.openStream();
                if (is != null) {
                    manifest = new Manifest(is);
                    jarManifest.addAttributesFrom(manifest);
                }
            } catch(Exception e3) {
                // ignore
            } finally {
                CloseableExtensions.closeSafely(is);
            }
        }

        Collections.sort(jarManifests);

        for (JarManifest jarManifest : jarManifests) {
            jarManifest.addAttributesTo(manifests);
        }
    }

    private static class JarManifest implements Comparable<JarManifest> {
        private final List<JarManifestAttributes> attributes = _Lists.newArrayList();

        private final URL url;

        private JarName jarName;

        public JarManifest(URL url) {
            this.url = url;
            jarName = asJarName(url);
        }

        void addAttributesFrom(Manifest manifest) {
            addAttributes(manifest, attributes);
        }

        void addAttributesTo(List<JarManifestAttributes> manifests) {
            manifests.add(JarManifestAttributes.jarName(jarName.name));
            manifests.add(JarManifestAttributes.jarUrl(url));
            manifests.addAll(attributes);
        }

        @Override
        public int compareTo(JarManifest o) {
            return jarName.compareTo(o.jarName);
        }
    }

    static class JarName implements Comparable<JarName>{
        enum Type {
            CLASSES, JAR, OTHER
        }
        Type type;
        String name;
        JarName(Type type, String name) {
            this.type = type;
            this.name = name;
        }
        @Override
        public int compareTo(JarName o) {
            int x = type.compareTo(o.type);
            if(x != 0) return x;
            return name.compareTo(o.name);
        }
    }

    private static JarName asJarName(URL url) {
        final String path = url.getPath();
        // strip off the meta-inf
        String strippedPath = stripSuffix(path, "/META-INF/MANIFEST.MF");
        strippedPath = stripSuffix(strippedPath, "!");

        // split the path into parts, and reverse
        List<String> parts = _Strings.splitThenStream(strippedPath, "/")
        .flatMap(s->_Strings.splitThenStream(s, "\\"))
        .filter(_Strings::isNotEmpty)
        .collect(Collectors.toList());
        
        //XXX legacy of
        //List<String> parts = _Lists.newArrayList(Splitter.on(CharMatcher.anyOf("/\\")).split(strippedPath));
        Collections.reverse(parts);

        // searching from the end, return the jar name if possible
        for (String part : parts) {
            if(part.endsWith(".jar")) {
                return new JarName(JarName.Type.JAR, part);
            }
        }

        // see if running in an IDE, under target*/classes; return the part prior to that.
        if(parts.size()>=3) {
            if(parts.get(0).equals("classes") && parts.get(1).startsWith("target")) {
                return new JarName(JarName.Type.CLASSES, parts.get(2));
            }
        }

        // otherwise, return the stripped path
        return new JarName(JarName.Type.OTHER, strippedPath);
    }

    public static String stripSuffix(String path, String suffix) {
        int indexOf = path.indexOf(suffix);
        if(indexOf != -1) {
            path = path.substring(0, indexOf);
        }
        return path;
    }

    static void addAttributes(Manifest manifest, List<JarManifestAttributes> attributes) {
        final Attributes mainAttribs = manifest.getMainAttributes();
        Set<Entry<Object, Object>> entrySet = mainAttribs.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            JarManifestAttributes attribute = JarManifestAttributes.attribute(entry);
            attributes.add(attribute);
        }
    }

    @Override
    protected JarManifestModel load() {
        return this;
    }

    @Override
    public void setObject(JarManifestModel ex) {
        // no-op
    }

    public List<JarManifestAttributes> getDetail() {
        return manifests;
    }

}
