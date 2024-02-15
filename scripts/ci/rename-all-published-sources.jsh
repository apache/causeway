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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Renames project from oldName to newName.
 *
 * <p>
 * This doesn't rewrite the contents of the following (binary) extensions:
 * <ul>
 *     <li>.jar</li>
 *     <li>.zip</li>
 *     <li>.pptx</li>
 *     <li>.docx</li>
 *     <li>.xlsx</li>
 *     <li>.odt</li>
 *     <li>.rtf</li>
 *     <li>.pdf</li>
 * </ul>
 */
class RenameProject {

    static final boolean DRY_RUN = false;

    // to obtain all the suffixes:
    // find . -type f | sed -rn 's|.*/[^/]+\.([^/.]+)$|\1|p' | sort -u
    static final List<String> EXTENSIONS = List.of(
            "NOTICE",
            "STATUS",
            "MF",
            "TXT",
            // "adoc", // ignore adoc file content
            "bat",
            "cfg",
            "css",
            "dcl",
            "dtd",
            "factories",
            "feature",
            "fxml",
            "gql",
            "graphqls",
            "hbs",
            "html",
            "importorder",
            "info",
            "ini",
            "java",
            "jdo",
            "js",
            "json",
            "kt",
            "kts",
            "ldif",
            "list",
            "md",
            "orm",
            "po",
            "pot",
            "properties",
            "puml",
            "rdf",
            "sh",
            "soc",
            "svg",
            "template",
            "thtml",
            "ts",
            "txt",
            "xml",
            "xsd",
            "yaml",
            "yml"
            );

    static final List<String> PATH_EXLUSIONS = List.of(
            "/build/",
            "/target/",
            "/adoc/", // not published in its legacy form
            "/scripts/ci/", // don't touch
            "/."
            );

    static final List<String> UNCONDITIONAL_INCLUSIONS = List.of(
            "/META-INF/services/");

    public static RenameProject renameBackToLegacy(final File root) {
        return new RenameProject(root, "causeway", "isis");
    }

    private RenameProject(final File root, final String oldName, final String newName) {
        this.root = root;
        this.fromLower = oldName.toLowerCase();
        this.toLower = newName.toLowerCase();
        this.fromUpper = oldName.toUpperCase();
        this.toUpper = newName.toUpperCase();
        this.fromTitle = capitalize(fromLower);
        this.toTitle = capitalize(toLower);
    }

    final File root;

    final String fromLower;
    final String toLower;
    final String fromUpper;
    final String toUpper;
    final String fromTitle;
    final String toTitle;

    static String capitalize(final String s) { return s.substring(0, 1).toUpperCase() + s.substring(1); }

    public void renameAllFiles() throws IOException {
        Files.find(root.toPath(), Integer.MAX_VALUE, (path, attr)->{
            if(isPathExcluded(path.toFile())) {
                return false;
            }
            return !attr.isDirectory();
        }, FileVisitOption.FOLLOW_LINKS)
        .map(Path::toFile)
        //.filter(file->fileNameEndsWithSupportedExtension(file)) // rename files unconditionally
        .forEach(file->{
            renameIfRequired(file);
        });
    }

    private void renameIfRequired(final File file) {
        var relativeFilePathRenamed = pathRelativeToRoot(file)
                .replace(fromTitle, toTitle)
                .replace(fromUpper, toUpper)
                .replace("\\" + fromLower, "\\" + toLower)
                .replace("/" + fromLower, "/" + toLower)
                .replace("-" + fromLower, "-" + toLower)
                .replace("_" + fromLower, "_" + toLower)
                .replace("." + fromLower, "." + toLower);

        var filePathRenamed = pathOf(root) + relativeFilePathRenamed;

        if (!filePathRenamed.equals(pathOf(file))) {

            System.err.printf("rename %s -> %s%n", pathRelativeToRoot(file), relativeFilePathRenamed);
            //System.err.printf("rename %s -> %s%n", pathOf(file), filePathRenamed);

            if(!DRY_RUN) {
                var fileRename = new File(filePathRenamed);
                var parentFile = fileRename.getParentFile();
                parentFile.mkdirs();
                try {
                    Files.move(file.toPath(), fileRename.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void rewriteAllFileContents() throws IOException {

        Files.find(root.toPath(), Integer.MAX_VALUE, (path, attr)->{
            return !attr.isDirectory();
        }, FileVisitOption.FOLLOW_LINKS)
        .map(Path::toFile)
        .filter(File::exists)
        .forEach(file->{
            if(isPathExcluded(file)) {
                return;
            }
            if(isPathUnconditionallyIncluded(file)
                    || fileNameEndsWithSupportedExtension(file)) {
                try {
                    rewriteIfRequired(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void rewriteIfRequired(final File file) throws FileNotFoundException, IOException {
        var lines = readLinesFromFile(file, StandardCharsets.UTF_8);
        var newLines = new ArrayList<String>(lines.size());

        final int linesChangedCount =
            lines.stream().mapToInt(line->{
                var newLine = line
                        .replace(fromLower, toLower)
                        .replace(fromUpper, toUpper)
                        .replace(fromTitle, toTitle)

                        // update schema declarations in .layout.xml files.
                        // terrible hack - we are assuming the target is 'isis'
                        // (also, the first arg has to handle the case that we've already converted replaced 'causeway' -> 'isis' throughout...)
                        .replace(
                                "https://isis.apache.org/applib/layout/menubars/bootstrap3 https://isis.apache.org/applib/layout/menubars/bootstrap3/menubars.xsd",
                                "http://isis.apache.org/applib/layout/menubars/bootstrap3 https://causeway.apache.org/applib/layout-v1/menubars/bootstrap3/menubars.xsd")
                        .replace(
                                "https://isis.apache.org/applib/layout/component https://isis.apache.org/applib/layout/component/component.xsd",
                                "http://isis.apache.org/applib/layout/component https://causeway.apache.org/applib/layout-v1/component/component.xsd")
                        .replace(
                                "https://isis.apache.org/applib/layout/grid/bootstrap3 https://isis.apache.org/applib/layout/grid/bootstrap3/bootstrap3.xsd",
                                "http://isis.apache.org/applib/layout/grid/bootstrap3 https://causeway.apache.org/applib/layout-v1/grid/bootstrap3/bootstrap3.xsd")
                        .replace(
                                "https://isis.apache.org/applib/layout/links https://isis.apache.org/applib/layout/links/links.xsd",
                                "http://isis.apache.org/applib/layout/links https://causeway.apache.org/applib/layout-v1/links/links.xsd")

                        // update namespace declarations in all files (.layout.xml and also constants in .java classes)
                        // (again, the first arg has to handle the case that we've already converted replaced 'causeway' -> 'isis' throughout...)
                        .replace(
                                "\"https://isis.apache.org/applib/layout/menubars/bootstrap3\"",
                                "\"http://isis.apache.org/applib/layout/menubars/bootstrap3\"")
                        .replace(
                                "\"https://isis.apache.org/applib/layout/component\"",
                                "\"http://isis.apache.org/applib/layout/component\"")
                        .replace(
                                "\"https://isis.apache.org/applib/layout/grid/bootstrap3\"",
                                "\"http://isis.apache.org/applib/layout/grid/bootstrap3\"")
                        .replace(
                                "\"https://isis.apache.org/applib/layout/links",
                                "\"http://isis.apache.org/applib/layout/links")
                        .replace(
                                "\"https://isis.apache.org/schema",
                                "\"http://isis.apache.org/schema")

                        ;
                newLines.add(newLine);
                return line.equals(newLine)
                        ? 0
                        : 1;
            })
            .sum();

        if (linesChangedCount>0) {
            System.err.printf("rewriting %s%n", pathRelativeToRoot(file));
            if(!DRY_RUN) {
                writeLinesToFile(newLines, file, StandardCharsets.UTF_8);
            }
        }
    }

    private static boolean fileNameEndsWithSupportedExtension(final File file) {
        for (String ext : EXTENSIONS) {
            if (file.getName().endsWith("." + ext)
                    || file.getName().equals(ext)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPathUnconditionallyIncluded(final File file) {
        var path = pathOf(file);
        for (String incl : UNCONDITIONAL_INCLUSIONS) {
            if (path.contains(incl)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPathExcluded(final File file) {
        var path = pathOf(file);
        for (String excl : PATH_EXLUSIONS) {
            if (path.contains(excl)) {
                return true;
            }
        }
        return false;
    }

    private String pathRelativeToRoot(final File file) {
        var prefix = pathOf(root);
        var path = pathOf(file);
        if(!path.startsWith(prefix)) {
            throw new IllegalArgumentException("file not subpath of root");
        }
        return path.substring(prefix.length());
    }

    private static String pathOf(final File file) {
        return file.getAbsolutePath().replace('\\', '/');
    }

    // -- READING

    private static List<String> readLines(
            final InputStream input,
            final Charset charset){
        if(input==null) {
            return List.of();
        }
        var lines = new ArrayList<String>();
        try(Scanner scanner = new Scanner(input, charset.name())){
            scanner.useDelimiter("\\n");
            while(scanner.hasNext()) {
                var line = scanner.next();
                //line = line.replace("\r", ""); // preserve windows specific line terminal
                lines.add(line);
            }
        }
        return lines;
    }

    private static List<String> readLinesFromFile(
            final File file,
            final Charset charset) throws FileNotFoundException, IOException {
        try(var input = new FileInputStream(file)){
            return readLines(input, charset);
        }
    }

    // -- WRITING

    static void writeLinesToFile(
            final Iterable<String> lines,
            final File file,
            final Charset charset) throws FileNotFoundException, IOException {

        try(var bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset))) {
            for(var line : lines) {
                bw.append(line).append("\n");
            }
        }
    }

}

var rootPath = "" + System.getenv("ROOT_PATH_LEGACY");
if(rootPath.isBlank()
        || ! new File(rootPath).exists()) {
    System.err.println("env ROOT_PATH_LEGACY must point to an existing directory");
    // /exit 1      // syntax error

} else {

    var root = new File(rootPath);
    
    var renamer = RenameProject.renameBackToLegacy(root);
    System.out.printf("processing root %s%n", root.getAbsolutePath());
    
    renamer.renameAllFiles();
    renamer.rewriteAllFileContents();
    
    System.out.println("done.");
}    
    
/exit
