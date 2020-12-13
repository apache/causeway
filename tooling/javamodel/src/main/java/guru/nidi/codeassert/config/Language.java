/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.config;

import java.util.List;

import static java.util.Arrays.asList;

//TODO this is a monkey patch that adds ADOC
public enum Language {
    
    JAVA("java", asList(".java")),
    KOTLIN("kotlin", asList(".kt", ".kts")),
    SCALA("scala", asList(".scala")),
    GROOVY("groovy", asList(".groovy", ".gvy", ".gy", ".gsh")),
    ADOC("adoc", asList(".adoc")),
    ;

    final String path;
    final List<String> suffices;

    Language(String path, List<String> suffices) {
        this.path = path;
        this.suffices = suffices;
    }

    public static Language byFilename(String filename) {
        final String suffix = filename.substring(filename.lastIndexOf('.'));
        for (final Language lang : values()) {
            if (lang.suffices.contains(suffix)) {
                return lang;
            }
        }
        return null;
    }
}
