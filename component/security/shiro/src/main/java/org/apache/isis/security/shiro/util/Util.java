package org.apache.isis.security.shiro.util;

import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Util {

    public static Map<String, List<String>> parse(String permissionsByRoleStr) {
        Map<String,List<String>> perms = Maps.newHashMap();
        for(String roleAndPermsStr: Splitter.on(";").split(permissionsByRoleStr)) {
            final Iterable<String> split = Splitter.on("=").split(roleAndPermsStr);
            final String[] roleAndPerms = Iterables.toArray(split, String.class);
            if(roleAndPerms.length != 2) {
                continue;
            }
            final String role = roleAndPerms[0].trim();
            final String permStr = roleAndPerms[1].trim();
            perms.put(role, Lists.newArrayList(Splitter.on(",").split(permStr)));
        }
        return perms;
    }
}
