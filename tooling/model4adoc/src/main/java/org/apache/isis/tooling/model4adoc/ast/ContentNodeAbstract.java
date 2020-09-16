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
package org.apache.isis.tooling.model4adoc.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.Document;

import org.apache.isis.core.commons.internal.base._Strings;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public abstract class ContentNodeAbstract implements ContentNode {

    @Getter @Setter private String id;
    @Getter @Setter private String context;
    @Getter @Setter private ContentNode parent;
    @Getter @Setter private Document document;
    @Getter @Setter private String nodeName;
    @Getter @Setter private boolean inline;
    @Getter @Setter private boolean block;
    @Getter private final Map<String, Object> attributes = new HashMap<>();
    @Getter @Setter private String role;
    @Getter private final List<String> roles = new ArrayList<>(); 
    @Getter @Setter private String reftext;
    
    @Override
    public String id() {
        return getId();
    }
    
    @Override
    public String context() {
        return getContext();
    }

    @Override
    public ContentNode parent() {
        return getParent();
    }

    @Override
    public Document document() {
        return getDocument();
    }

    @Override
    @Deprecated
    public Object getAttr(Object name, Object defaultValue, boolean inherit) {
        return getAttribute(name, defaultValue, inherit);
    }

    @Override
    @Deprecated
    public Object getAttr(Object name, Object defaultValue) {
        return getAttribute(name, defaultValue);
    }

    @Override
    @Deprecated
    public Object getAttr(Object name) {
        return getAttribute(name);
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue, boolean inherit) {
        return attributes.getOrDefault(name, defaultValue);
    }

    @Override
    public Object getAttribute(Object name, Object defaultValue) {
        return attributes.getOrDefault(name, defaultValue);
    }

    @Override
    public Object getAttribute(Object name) {
        return attributes.get(name);
    }

    @Override
    @Deprecated
    public boolean isAttr(Object name, Object expected, boolean inherit) {
        return isAttribute(name, expected, inherit);
    }

    @Override
    @Deprecated
    public boolean isAttr(Object name, Object expected) {
        return isAttribute(name, expected);
    }

    @Override
    public boolean isAttribute(Object name, Object expected, boolean inherit) {
        return Boolean.TRUE == getAttribute(name, expected, inherit);
    }

    @Override
    public boolean isAttribute(Object name, Object expected) {
        return Boolean.TRUE == getAttribute(name, expected);
    }

    @Override
    @Deprecated
    public boolean hasAttr(Object name) {
        return hasAttribute(name);
    }

    @Override
    @Deprecated
    public boolean hasAttr(Object name, boolean inherited) {
        return hasAttribute(name, inherited);
    }

    @Override
    public boolean hasAttribute(Object name) {
        return getAttribute(name)!=null;
    }

    @Override
    public boolean hasAttribute(Object name, boolean inherited) {
        return getAttribute(name, inherited)!=null;
    }

    @Override
    @Deprecated
    public boolean setAttr(Object name, Object value, boolean overwrite) {
        return setAttribute(name, value, overwrite);
    }

    @Override
    public boolean setAttribute(Object name, Object value, boolean overwrite) {
        val key = (String)name;
        return overwrite 
                ? attributes.put(key, value)==null
                : attributes.get(key)!=null
                    ? false
                    : attributes.put(key, value)==null;
    }

    @Override
    public boolean isOption(Object name) {
        return false; // FIXME
    }

    @Override
    public boolean isRole() {
        return !_Strings.isNullOrEmpty(getRole());
    }

    @Override
    @Deprecated
    public String role() {
        return getRole();
    }

    @Override
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @Override
    public void addRole(String role) {
        roles.add(role);
    }

    @Override
    public void removeRole(String role) {
        roles.remove(role);
    }

    @Override
    public boolean isReftext() {
        return !_Strings.isNullOrEmpty(getReftext());
    }

    @Override
    public String iconUri(String name) {
        return String.format("[icon_uri %s]", name);
    }

    @Override
    public String mediaUri(String target) {
        return String.format("[media_uri %s]", target);
    }

    @Override
    public String imageUri(String targetImage) {
        return String.format("[image_uri %s]", targetImage);
    }

    @Override
    public String imageUri(String targetImage, String assetDirKey) {
        return String.format("[media_uri %s %s]", targetImage, assetDirKey);
    }

    @Override
    public String readAsset(String path, Map<Object, Object> opts) {
        return String.format("[read_asset %s %s]", path, opts);
    }

    @Override
    public String normalizeWebPath(String path, String start, boolean preserveUriTarget) {
        return String.format("[normalize_web_path %s %s %b]", path, start, preserveUriTarget);
    }

    
}
