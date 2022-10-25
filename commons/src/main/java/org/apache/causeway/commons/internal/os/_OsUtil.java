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
package org.apache.causeway.commons.internal.os;

import java.io.File;
import java.io.FileWriter;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.OptionalLong;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides some OS related utilities.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
public class _OsUtil {

    public static enum OS {
        WINDOWS,
        LINUX,
        MAC_OS,
        OTHER;
        public boolean isWindows() { return this==WINDOWS; }
        public boolean isLinux() { return this==LINUX; }
        public boolean isMacOs() { return this==MAC_OS; }
        public static OS current() {
            val osName = System.getProperty("os.name").toLowerCase();
            if (osName.startsWith("windows")) {
                return OS.WINDOWS;
            }
            if (osName.contains("linux")) {
                return OS.LINUX;
            }
            if (osName.contains("mac")) {
                return OS.MAC_OS;
            }
            return OS.OTHER;
        }
    }

    /**
     * Uses given {@code pidFile} as a way to communicate between processes,
     * whether there is already a running one.
     * And if so, terminates the already running one.
     * <p>
     * This is useful for web-app development from within an IDE, such that,
     * when placed within the app's {@code main} method, a previous run can be
     * auto-terminated.
     * <p>Example<pre>
     * void main(String[] args) {
     *     _OsUtil.thereCanBeOnlyOne(new File("pid.log"));
     *     ...
     * }
     * </pre>
     */
    @SneakyThrows
    public void thereCanBeOnlyOne(final File pidFile) {

        if(pidFile.exists()) {
            _Text.readLinesFromFile(pidFile, StandardCharsets.UTF_8)
            .filter(_Strings::isNotEmpty)
            .getFirst()
            .ifPresent(pid->terminateProcessByPid(pid));
        }

        final long newPid = ProcessHandle.current().pid();
        try(val fw = new FileWriter(pidFile, StandardCharsets.UTF_8)) {
            fw.write("" + newPid);
        }
    }

    /**
     * Terminates an instance of this application,
     * if there is an already running one found.
     * <p>
     * Use in conjunction with system environment variable
     * {@code THERE_CAN_BE_ONLY_ONE=true}, in order to switch this feature on.
     * @see #thereCanBeOnlyOne(File)
     */
    public void thereCanBeOnlyOne() {
        if("true".equalsIgnoreCase(System.getenv("THERE_CAN_BE_ONLY_ONE"))) {
            _OsUtil.thereCanBeOnlyOne(new File("pid.log"));
        }
    }

    @SneakyThrows
    public void terminateProcessByPid(final @Nullable String pid) {
        val pidTrimmed = _Strings.blankToNullOrTrim(pid);
        if(pidTrimmed==null) {
            return; // do nothing
        }
        val rt = Runtime.getRuntime();
        val os = OS.current();
        final String cmd;
        switch(os) {
        case WINDOWS:
            cmd = String.format("taskkill /F /PID %s /T", pidTrimmed);
            break;
        case LINUX:
            //XXX implement eventually
        case MAC_OS:
            //XXX implement eventually
        default:
            throw _Exceptions.unsupportedOperation("OS " + os + " not (yet) supported");
        }
        rt.exec(cmd);
    }

    /**
     * Optionally returns a machine specific unique number, based on whether
     * the algorithm was able to generate one.
     * <p>
     * Should at least be stable until machine reboot.
     */
    public OptionalLong machineId() {
        try {

            long hash = 5381L;
            boolean valid = false;

            for (NetworkInterface netint : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                val hwAddr = netint.getHardwareAddress();
                if(_NullSafe.size(hwAddr)<6) {
                    continue;
                }
                for(byte b:hwAddr) {
                    hash = hash*33L + b;
                }
                valid = true;
            }

            if(valid) {
                return OptionalLong.of(hash);
            }

            // fallback to empty

        } catch (Throwable e) {
            // fallback to empty
        }
        return OptionalLong.empty();
    }

}
