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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Marks a class as an OLO (Open LLM Orchestrator) plugin and supplies metadata
 * for the UI designer and plug-and-play packaging.
 * <p>
 * Used to generate {@code plugin.yaml} and to build the .olo.zip package
 * (plugin.jar + plugin.yaml + icons + README + LICENSE + checksums).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface OloPlugin {

    /**
     * Unique plugin identifier (e.g. "com.openllm.plugin.vectordb").
     * Required. A class is considered a plugin when annotated with at least {@code id}.
     */
    String id();

    /**
     * Human-readable display name for the UI.
     * Default: simple class name when empty.
     */
    String name() default "";

    /**
     * Semantic version (e.g. "1.2.0").
     */
    String version() default "1.0.0";

    /**
     * Short description for tooltips and catalog.
     */
    String description() default "";

    /**
     * Optional author or vendor.
     */
    String author() default "";

    /**
     * SPDX license identifier (e.g. "Apache-2.0").
     */
    String license() default "Apache-2.0";

    /**
     * Plugin category / stage type for the pipeline designer
     * (e.g. MODEL, FILTER, VECTOR_STORE, TOOL, GUARDRAIL).
     * Default: "CUSTOM" when empty.
     */
    String category() default "CUSTOM";

    /**
     * Declared inputs for the UI (name, type, required, description).
     */
    Input[] inputs() default {};

    /**
     * Declared outputs for the UI (name, type, description).
     */
    Output[] outputs() default {};

    /**
     * Optional website or documentation URL.
     */
    String website() default "";

    /**
     * Optional override for small icon path (e.g. "icons/MyPlugin-icon-64.svg").
     * If empty, path is derived from plugin class name: icons/&lt;SimpleClassName&gt;-icon-64.svg.
     */
    String iconSmall() default "";

    /**
     * Optional override for large icon path (e.g. "icons/MyPlugin-icon-256.svg").
     * If empty, path is icons/&lt;SimpleClassName&gt;-icon-256.svg.
     */
    String iconLarge() default "";

    /**
     * Optional override for banner path (e.g. "icons/MyPlugin-banner.svg").
     * If empty, path is icons/&lt;SimpleClassName&gt;-banner.svg.
     */
    String banner() default "";

    /**
     * Optional (not mandatory). Sample input for standalone validation in the UI.
     * When non-empty: JSON object that the UI can use as {@code context.getOriginalInput()}
     * when running the plugin as an individual unit. Keys should match the plugin's expected
     * input names (see {@link #inputs()}). Omit or leave empty if no sample is provided.
     */
    String sampleInput() default "";

    /**
     * Optional (not mandatory). Short note for the validation UI when sample input is used.
     */
    String sampleInputDescription() default "";

    /**
     * Declares one input port for the plugin (for UI binding).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Input {
        String name();
        String type() default "string";
        boolean required() default false;
        String description() default "";
    }

    /**
     * Declares one output port for the plugin (for UI binding).
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Output {
        String name();
        String type() default "string";
        String description() default "";
    }
}
