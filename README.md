# Pretty Pipes WorldEdit Compat

Server-side NeoForge 1.21.1 add-on for Pretty Pipes 1.22.0 and WorldEdit 7.3.8.

## What it does

- Watches WorldEdit edit sessions.
- Records only `prettypipes:pipe` blocks touched by WorldEdit.
- Rebuilds Pretty Pipes network data after pasted/edited pipes are placed.
- Cleans Pretty Pipes network data after WorldEdit removes pipes, including `//undo` and `//set 0`.
- Rotates Pretty Pipes module direction-selector data only during confirmed `//paste` operations with deterministic WorldEdit clipboard rotations of 90, 180, or 270 degrees.
- Sends a private summary message only to the WorldEdit operator.

## Required mods

- Minecraft 1.21.1
- NeoForge 21.1.x
- Pretty Pipes 1.22.0+
- WorldEdit 7.3.8+

## Supported workflow

```mcfunction
//copy
//paste
```

```mcfunction
//copy
//rotate 90
//paste
```

```mcfunction
//copy
//rotate 180
//paste
```

```mcfunction
//copy
//rotate 270
//paste
```

Undo/delete cleanup is automatic:

```mcfunction
//undo
//set 0
```

## Build

Drag this source into a GitHub repository. The `src/stubs/java` source set is compile-only and is intentionally not packaged into the final jar; it just lets GitHub compile against the Pretty Pipes and WorldEdit APIs without bundling either mod. The included GitHub Actions workflow builds the jar automatically.

The jar name is:

```text
PrettyPipesWorldEditCompat-1.0.0-NeoForge-1.21.1.jar
```
