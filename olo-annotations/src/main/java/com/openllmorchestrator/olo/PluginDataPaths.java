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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Common default data directory for all plugins in a container or host.
 * <p>
 * One environment variable defines the root; each plugin has a dedicated subfolder
 * so that uploads, RAG files, templates, etc. can be resolved at a known location
 * and the runtime can ensure file availability there.
 * <p>
 * <b>Environment variable:</b> {@value #ENV_PLUGIN_DATA_DIR}
 * <ul>
 *   <li>Default: {@value #DEFAULT_BASE_DIR} (relative to process working directory)</li>
 *   <li>In a container: set to e.g. {@code /data/olo} so all plugin data lives under that path</li>
 * </ul>
 * <p>
 * <b>Plugin subfolder:</b> {@code <baseDir>/<pluginId>} (pluginId from {@link OloPlugin#id()}.
 * For example, plugin {@code com.openllm.plugin.vectordb} uses {@code <base>/com.openllm.plugin.vectordb/}.
 * Relative paths in plugin config (e.g. "uploads", "templates/response.txt") are resolved against
 * the plugin's subfolder so that files are guaranteed to be available at that location when the
 * runtime places uploads or templates there.
 */
public final class PluginDataPaths {

    /**
     * Environment variable for the root data directory shared by all plugins.
     * Set in container to e.g. {@code /data/olo}; default {@value #DEFAULT_BASE_DIR}.
     */
    public static final String ENV_PLUGIN_DATA_DIR = "OLO_PLUGIN_DATA_DIR";

    /**
     * Default base directory when the env var is not set (relative to user.dir).
     */
    public static final String DEFAULT_BASE_DIR = "olo-data";

    private static Path baseDir;

    /**
     * Returns the root data directory (resolved from env or default).
     * Not normalized to absolute until first use so that relative default works against current working directory.
     */
    public static Path getBaseDir() {
        if (baseDir == null) {
            String env = System.getenv(ENV_PLUGIN_DATA_DIR);
            if (env != null && !env.isBlank()) {
                baseDir = Paths.get(env.trim()).normalize();
            } else {
                baseDir = Paths.get(DEFAULT_BASE_DIR).normalize();
            }
        }
        return baseDir;
    }

    /**
     * Returns the dedicated subfolder for the given plugin id (e.g. from {@link OloPlugin#id()}).
     * The path is safe: pluginId is sanitized to prevent directory traversal.
     *
     * @param pluginId plugin id (e.g. "com.openllm.plugin.vectordb")
     * @return path to the plugin's data subfolder (may not exist yet)
     */
    public static Path getPluginDir(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException("pluginId must be non-blank");
        }
        String safe = sanitizePluginId(pluginId);
        return getBaseDir().resolve(safe);
    }

    /**
     * Ensures the plugin's data directory exists (creates it if necessary) and returns it.
     * Use this when the plugin needs to read or write files in its folder (uploads, templates, etc.).
     *
     * @param pluginId plugin id
     * @return path to the plugin's data subfolder (existing directory)
     * @throws IOException if the directory could not be created
     */
    public static Path ensurePluginDirExists(String pluginId) throws IOException {
        Path dir = getPluginDir(pluginId);
        Files.createDirectories(dir);
        return dir;
    }

    /**
     * Resolves a path relative to the plugin's data directory.
     * Use for relative config like "uploads" or "templates/response.txt".
     * Does not create directories; use {@link #ensurePluginDirExists(String)} first if needed.
     *
     * @param pluginId plugin id
     * @param relativePath path relative to the plugin dir (e.g. "uploads", "templates/response.mustache")
     * @return resolved path (may not exist)
     */
    public static Path resolve(String pluginId, String relativePath) {
        Path pluginDir = getPluginDir(pluginId);
        if (relativePath == null || relativePath.isBlank()) {
            return pluginDir;
        }
        String safe = relativePath.replace('\\', '/').trim();
        if (safe.isEmpty() || safe.equals(".")) {
            return pluginDir;
        }
        if (safe.startsWith("/") || safe.contains("..")) {
            throw new IllegalArgumentException("relativePath must be relative and must not contain '..'");
        }
        return pluginDir.resolve(safe).normalize();
    }

    /**
     * Resets the cached base dir (for tests or when env is changed at runtime).
     */
    public static void reset() {
        baseDir = null;
    }

    private static String sanitizePluginId(String pluginId) {
        String s = pluginId.trim().replace('\\', '/');
        if (s.contains("..") || s.startsWith("/")) {
            throw new IllegalArgumentException("pluginId must not contain '..' or start with '/'");
        }
        return s;
    }

    private PluginDataPaths() {}
}
