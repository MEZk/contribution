////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.site;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.maven.jxr.JavaCodeTransform;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;

/**
 * Constructor for cross reference HTMLs
 * from java source files. Wrapper around
 * maven-jxr functional.
 *
 * @author attatrol
 *
 */
class XrefGenerator {

    /**
     * Encoding used for input and output files.
     */
    public static final String ENCODING = "UTF-8";

    /**
     * File extension used for reports.
     */
    private static final String FILE_EXTENSION = ".html";

    /**
     * File counter only to be used with {@code shortFileNames} option in
     * {@link #getDestinationPath(String, boolean)}.
     */
    private static int simpleFileNameCounter;

    /**
     * Maven-jxr package manager.
     */
    private static PackageManager pacman;

    /**
     * Maven-jxr XREF file generator.
     */
    private static JavaCodeTransform codeTransform;

    static {
        pacman = new PackageManager(new JxrDummyLog(),
                new FileManager());
        codeTransform = new JavaCodeTransform(pacman);
    }

    /**
     * Path to the sources, used to shorten paths.
     */
    private Path relativizationPath;

    /**
     * Destination folder for XREF files.
     */
    private Path destinationPath;

    /**
     * Path to the site.
     */
    private Path sitePath;

    /**
     * The only constructor.
     *
     * @param relativizationPath
     *        path to the sources, used to shorten paths.
     * @param destinationPath
     *        destination folder for XREF files.
     * @param sitePath
     *        path to the site.
     */
    XrefGenerator(Path relativizationPath,
            Path destinationPath, Path sitePath) {
        this.relativizationPath = relativizationPath;
        this.destinationPath = destinationPath;
        this.sitePath = sitePath;
    }

    /**
     * Generates XREF file from source file.
     *
     * @param name
     *        path to the source file.
     * @param shortFilePaths
     *           {@code true} if only short file names should be used with no path.
     * @return relative path to the resulting file.
     */
    public final String generateXref(String name, boolean shortFilePaths) {
        final File sourceFile = new File(name);
        final Path dest = getDestinationPath(name, shortFilePaths);
        String result;
        try {
            codeTransform.transform(sourceFile.getAbsolutePath(),
                dest.toString(), Locale.ENGLISH,
                ENCODING, ENCODING, "", "", "");
            result = sitePath.relativize(dest).toString();
        }
        // -@cs[IllegalCatch] We need to catch all exception from JXR
        catch (Exception ex) {
            result = null;
        }
        return result;
    }

    /**
     * Generates full path to the destination of XREF file.
     *
     * @param name
     *        java source file path.
     * @param shortFilePaths
     *           {@code true} if only short file names should be used with no path.
     * @return full path to the destination of XREF file.
     */
    private Path getDestinationPath(String name, boolean shortFilePaths) {
        final Path destPath;

        if (shortFilePaths) {
            simpleFileNameCounter++;
            destPath = Paths
                    .get(destinationPath + "/File" + simpleFileNameCounter + FILE_EXTENSION);
        }
        else {
            final String newName = name + FILE_EXTENSION;
            final Path sourcePath = Paths.get(newName);
            if (relativizationPath == null) {
                destPath = destinationPath
                    .resolve(sourcePath.subpath(0, sourcePath.getNameCount()));
            }
            else {
                destPath = destinationPath
                    .resolve(relativizationPath.relativize(sourcePath));
            }
        }
        return destPath;
    }
}
