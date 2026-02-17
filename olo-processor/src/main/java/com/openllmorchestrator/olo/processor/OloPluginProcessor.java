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
package com.openllmorchestrator.olo.processor;

import com.openllmorchestrator.olo.OloPlugin;
import com.openllmorchestrator.olo.PackageFormat;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Annotation processor that generates {@code plugin.yaml} from {@link OloPlugin}.
 * Icon paths follow class-based naming: icons/&lt;SimpleClassName&gt;-icon-64.svg etc.
 * If multiple classes are annotated, output is a "plugins:" array; otherwise single "plugin:".
 * Default fallback paths (icons/default-icon-64.svg) are always emitted so UI can fall back when class-specific asset is missing.
 */
@SupportedAnnotationTypes("com.openllmorchestrator.olo.OloPlugin")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class OloPluginProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> types = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(OloPlugin.class)) {
            if (element instanceof TypeElement type) {
                if (type.getAnnotation(OloPlugin.class) != null) {
                    types.add(type);
                }
            }
        }
        if (types.isEmpty()) return false;
        String yaml = buildYaml(types);
        Element first = types.get(0);
        try {
            FileObject resource = processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "olo",
                    PackageFormat.PLUGIN_YAML,
                    first
            );
            try (Writer w = resource.openWriter()) {
                w.write(yaml);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to write plugin.yaml: " + e.getMessage(), first);
        }
        return false;
    }

    private static String buildYaml(List<TypeElement> types) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated from @OloPlugin - Open LLM Orchestrator plugin descriptor\n");
        sb.append("schemaVersion: \"1.0\"\n\n");
        boolean multiple = types.size() > 1;
        if (multiple) {
            sb.append("plugins:\n");
        }
        for (TypeElement type : types) {
            OloPlugin p = type.getAnnotation(OloPlugin.class);
            if (p == null) continue;
            String className = type.getQualifiedName().toString();
            String simpleName = type.getSimpleName().toString();
            if (multiple) {
                sb.append("  - plugin:\n");
                appendPluginBlock(sb, className, simpleName, p, "    ");
            } else {
                sb.append("plugin:\n");
                appendPluginBlock(sb, className, simpleName, p, "  ");
            }
        }
        return sb.toString();
    }

    private static void appendPluginBlock(StringBuilder sb, String className, String simpleName, OloPlugin p, String indent) {
        String in = indent;
        sb.append(in).append("id: ").append(escapeYaml(p.id())).append("\n");
        String name = nonEmpty(p.name()) ? p.name() : simpleName;
        sb.append(in).append("name: ").append(escapeYaml(name)).append("\n");
        sb.append(in).append("version: ").append(escapeYaml(p.version())).append("\n");
        sb.append(in).append("description: ").append(escapeYaml(p.description() != null ? p.description() : "")).append("\n");
        if (p.author() != null && !p.author().isEmpty()) {
            sb.append(in).append("author: ").append(escapeYaml(p.author())).append("\n");
        }
        sb.append(in).append("license: ").append(escapeYaml(p.license())).append("\n");
        String category = nonEmpty(p.category()) ? p.category() : "CUSTOM";
        sb.append(in).append("category: ").append(escapeYaml(category)).append("\n");
        sb.append(in).append("className: ").append(escapeYaml(className)).append("\n");
        if (p.website() != null && !p.website().isEmpty()) {
            sb.append(in).append("website: ").append(escapeYaml(p.website())).append("\n");
        }
        sb.append(in).append("inputs:\n");
        for (OloPlugin.Input input : p.inputs()) {
            sb.append(in).append("  - name: ").append(escapeYaml(input.name())).append("\n");
            sb.append(in).append("    type: ").append(escapeYaml(input.type())).append("\n");
            sb.append(in).append("    required: ").append(input.required()).append("\n");
            sb.append(in).append("    description: ").append(escapeYaml(input.description())).append("\n");
        }
        sb.append(in).append("outputs:\n");
        for (OloPlugin.Output out : p.outputs()) {
            sb.append(in).append("  - name: ").append(escapeYaml(out.name())).append("\n");
            sb.append(in).append("    type: ").append(escapeYaml(out.type())).append("\n");
            sb.append(in).append("    description: ").append(escapeYaml(out.description())).append("\n");
        }
        if (nonEmpty(p.sampleInput())) {
            sb.append(in).append("sampleInput: ").append(escapeYaml(p.sampleInput())).append("\n");
        }
        if (nonEmpty(p.sampleInputDescription())) {
            sb.append(in).append("sampleInputDescription: ").append(escapeYaml(p.sampleInputDescription())).append("\n");
        }
        String smallSvg = nonEmpty(p.iconSmall()) ? p.iconSmall() : PackageFormat.iconPathForClass(simpleName, PackageFormat.SUFFIX_ICON_64);
        String largeSvg = nonEmpty(p.iconLarge()) ? p.iconLarge() : PackageFormat.iconPathForClass(simpleName, PackageFormat.SUFFIX_ICON_256);
        String bannerSvg = nonEmpty(p.banner()) ? p.banner() : PackageFormat.iconPathForClass(simpleName, PackageFormat.SUFFIX_BANNER);
        sb.append(in).append("icons:\n");
        sb.append(in).append("  smallSvg: ").append(escapeYaml(smallSvg)).append("\n");
        sb.append(in).append("  largeSvg: ").append(escapeYaml(largeSvg)).append("\n");
        sb.append(in).append("  bannerSvg: ").append(escapeYaml(bannerSvg)).append("\n");
        sb.append(in).append("  defaultSmallSvg: ").append(PackageFormat.DEFAULT_ICON_64_SVG).append("\n");
        sb.append(in).append("  defaultLargeSvg: ").append(PackageFormat.DEFAULT_ICON_256_SVG).append("\n");
        sb.append(in).append("  defaultBannerSvg: ").append(PackageFormat.DEFAULT_BANNER_SVG).append("\n");
    }

    private static boolean nonEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static String escapeYaml(String s) {
        if (s == null) return "\"\"";
        if (s.contains("\n") || s.contains(":") || s.contains("#") || s.startsWith(" ") || s.contains("\"")) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        }
        return s;
    }
}
