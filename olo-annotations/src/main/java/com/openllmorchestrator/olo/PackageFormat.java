/*
 * Copyright 2026 Open LLM Orchestrator contributors.
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
package com.openllmorchestrator.olo;

/**
 * Constants for the OLO plugin package format (.olo.zip).
 * <p>
 * Layout:
 * <pre>
 * my-plugin-1.2.0.olo.zip
 * ├── plugin.yaml
 * ├── plugin.jar
 * ├── icons/
 * │   ├── icon-64.png
 * │   ├── icon-256.png
 * │   └── banner.png
 * ├── README.md
 * ├── LICENSE
 * └── checksums.sha256
 * </pre>
 */
public final class PackageFormat {

    /** File extension for OLO plugin archives. */
    public static final String OLO_EXTENSION = "olo";

    /** Root descriptor file name. */
    public static final String PLUGIN_YAML = "plugin.yaml";

    /** JAR file name inside the package. */
    public static final String PLUGIN_JAR = "plugin.jar";

    /** Icons directory name. */
    public static final String ICONS_DIR = "icons";

    /** Small icon (64x64). */
    public static final String ICON_64 = ICONS_DIR + "/icon-64.png";

    /** Large icon (256x256). */
    public static final String ICON_256 = ICONS_DIR + "/icon-256.png";

    /** Banner image. */
    public static final String BANNER = ICONS_DIR + "/banner.png";

    /** Small icon SVG (for UI display when PNG not present). */
    public static final String ICON_64_SVG = ICONS_DIR + "/icon-64.svg";

    /** Large icon SVG. */
    public static final String ICON_256_SVG = ICONS_DIR + "/icon-256.svg";

    /** Banner SVG. */
    public static final String BANNER_SVG = ICONS_DIR + "/banner.svg";

    /** Default small icon (bundled when class-specific icon not defined). */
    public static final String DEFAULT_ICON_64_SVG = ICONS_DIR + "/default-icon-64.svg";

    /** Default large icon. */
    public static final String DEFAULT_ICON_256_SVG = ICONS_DIR + "/default-icon-256.svg";

    /** Default banner. */
    public static final String DEFAULT_BANNER_SVG = ICONS_DIR + "/default-banner.svg";

    /** Suffix for class-based small icon: {@code <SimpleClassName>-icon-64.svg}. */
    public static final String SUFFIX_ICON_64 = "-icon-64.svg";

    /** Suffix for class-based large icon. */
    public static final String SUFFIX_ICON_256 = "-icon-256.svg";

    /** Suffix for class-based banner. */
    public static final String SUFFIX_BANNER = "-banner.svg";

    /**
     * Icon path for a plugin class (convention: icons/&lt;simpleClassName&gt;-icon-64.svg).
     */
    public static String iconPathForClass(String simpleClassName, String suffix) {
        return ICONS_DIR + "/" + simpleClassName + suffix;
    }

    /** README in the package. */
    public static final String README = "README.md";

    /** License file in the package. */
    public static final String LICENSE = "LICENSE";

    /** Checksum file (SHA-256 of all other files). */
    public static final String CHECKSUMS = "checksums.sha256";

    private PackageFormat() {}
}
