package org.apache.isis.viewer.json.viewer.representations;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

class Node {
    private static final Pattern NODE = Pattern.compile("^([^\\[]+)(\\[(.+)\\])?$");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern KEY_VALUE = Pattern.compile("^([^=]+)=(.+)$");
    
    public static final Node NULL = new Node("", Collections.<String,String>emptyMap());
    
    public static Node parse(String path) {
        Matcher nodeMatcher = NODE.matcher(path);
        if(!nodeMatcher.matches()) {
            return null;
        } 
        final int groupCount = nodeMatcher.groupCount();
        if(groupCount < 1) {
            return null;
        }
        final String key = nodeMatcher.group(1);
        final Map<String, String> criteria = Maps.newHashMap();
        final String criteriaStr = nodeMatcher.group(3);
        if(criteriaStr != null) {
            for (String criterium : Splitter.on(WHITESPACE).split(criteriaStr)) {
                final Matcher keyValueMatcher = KEY_VALUE.matcher(criterium);
                if(keyValueMatcher.matches()) {
                    criteria.put(keyValueMatcher.group(1), keyValueMatcher.group(2));
                }
            }
        }

        return new Node(key, criteria);
    }
    
    private final String key;
    private final Map<String,String> criteria;
    private Node(String key, Map<String, String> criteria) {
        this.key = key;
        this.criteria = Collections.unmodifiableMap(criteria);
    }
    public String getKey() {
        return key;
    }
    public Map<String, String> getCriteria() {
        return criteria;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return key + (criteria.isEmpty()?"":criteria);
    }
    
}