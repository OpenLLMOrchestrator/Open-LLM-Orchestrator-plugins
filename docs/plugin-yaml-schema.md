# plugin.yaml schema (OLO plugin descriptor)

Generated from `@OloPlugin` by the annotation processor. Used by the UI designer to display the plugin as a plug-and-play component.

## UI drag-and-drop

Each `.olo` package (and the generated `plugin.yaml` inside it) bundles everything needed to show the plugin as a drag-and-drop component in the pipeline UI:

| Data | Use in UI |
|------|------------|
| `id`, `name`, `version`, `description` | Catalog card and tooltip |
| `capability` | List of capabilities (e.g. [MODEL], [CACHING], [ACCESS, MODEL]); palette grouping and pipeline stage type |
| `className` | Runtime: load plugin class from `plugin.jar` |
| `inputs` / `outputs` | Ports to draw and connect (name, type, required, description) |
| `icons.smallSvg`, `icons.largeSvg`, `icons.bannerSvg` | Card/thumbnail and detail view; fallback to `default*` if missing |

The `.olo` archive contains `plugin.yaml`, `plugin.jar`, and `icons/` (including default SVGs), so the UI can resolve icon paths relative to the package root. For multi-plugin JARs, `plugin.yaml` uses a `plugins:` array so each entry is one drag-and-drop component.

### Validation as individual unit (planned UI)

To let users **validate a plugin in isolation** before using it in a pipeline:

1. **sampleInput** (optional): JSON object that the UI uses as the plugin’s input when running a standalone test. The runner builds a `PluginContext` whose `getOriginalInput()` returns this map, instantiates the plugin class from **className**, calls `execute(context)`, and displays the result.
2. **sampleInputDescription** (optional): Short note shown in the validation form (e.g. “Provide a question; optional context and modelId.”).

Plugins that set `sampleInput` (and optionally `sampleInputDescription`) in `@OloPlugin` will have these fields in `plugin.yaml`; the UI can offer a “Validate” or “Try it” action that runs the plugin with the sample input and shows the output so the plugin can be verified as a working unit.

## Schema version: 1.0

```yaml
# plugin.yaml
schemaVersion: "1.0"

plugin:
  id: string              # Unique plugin id (e.g. com.openllm.plugin.vectordb)
  name: string             # Display name
  version: string         # Semver (e.g. 1.2.0)
  description: string     # Short description
  author: string          # Optional
  license: string         # SPDX id (e.g. Apache-2.0)
  capability: string[]     # e.g. [ "MODEL" ] or [ "ACCESS", "MODEL" ]
  className: string       # FQCN of the plugin class
  website: string         # Optional URL
  inputs:                 # For UI binding
    - name: string
      type: string        # string | number | boolean | array | object
      required: boolean
      description: string
  outputs:
    - name: string
      type: string
      description: string
  # Optional (not mandatory) – for UI "Validate as individual unit"
  sampleInput: string          # If set: JSON object for context.getOriginalInput() in standalone run
  sampleInputDescription: string   # If set: short note for the validation form
  icons:
    smallSvg: string      # Class-based: icons/<SimpleClassName>-icon-64.svg (or override from @OloPlugin.iconSmall())
    largeSvg: string      # icons/<SimpleClassName>-icon-256.svg
    bannerSvg: string     # icons/<SimpleClassName>-banner.svg
    defaultSmallSvg: string   # Fallback when class-specific missing: icons/default-icon-64.svg
    defaultLargeSvg: string   # icons/default-icon-256.svg
    defaultBannerSvg: string  # icons/default-banner.svg
```

When a project has **multiple** annotated plugin classes, the root key is `plugins:` (array) instead of `plugin:`:

```yaml
schemaVersion: "1.0"
plugins:
  - plugin:
      id: ...
      className: ...
      icons: { smallSvg: icons/FirstPlugin-icon-64.svg, ... }
  - plugin:
      id: ...
      className: ...
      icons: { smallSvg: icons/SecondPlugin-icon-64.svg, ... }
```

## Serialization

- Format: YAML (default). Can be converted to JSON for APIs.
- File name: `plugin.yaml` at the root of the `.olo.zip`.

## Icon naming (per plugin class)

- **Convention**: One set of icons per plugin **class**. File names follow `<SimpleClassName>-icon-64.svg`, `<SimpleClassName>-icon-256.svg`, `<SimpleClassName>-banner.svg` (e.g. `Llama32ChatPlugin-icon-64.svg`).
- **Override**: Optional `@OloPlugin` attributes `iconSmall()`, `iconLarge()`, `banner()` can override paths.
- **Default**: If a class-specific asset is not present in the package, the UI uses `icons/default-icon-64.svg`, `icons/default-icon-256.svg`, `icons/default-banner.svg`. These defaults are always bundled in the .olo so the plugin works with minimal or no icon configuration.

## .olo package layout

```
my-plugin-1.2.0.olo.zip
├── plugin.yaml
├── plugin.jar
├── icons/
│   ├── <ClassName>-icon-64.svg    # Per-class (e.g. Llama32ChatPlugin-icon-64.svg)
│   ├── <ClassName>-icon-256.svg
│   ├── <ClassName>-banner.svg
│   ├── default-icon-64.svg        # Always present as fallback
│   ├── default-icon-256.svg
│   └── default-banner.svg
├── README.md
├── LICENSE
└── checksums.sha256
```

- `checksums.sha256`: One line per file: `SHA256_HEX  path` (path relative to zip root). Covers all other files; used to verify integrity.
