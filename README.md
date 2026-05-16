# PrettyPipes+

Server-side NeoForge 1.21.1 add-on for Pretty Pipes 1.22.0+ and WorldEdit 7.3.8+.

## What it does

- Repairs Pretty Pipes network data after WorldEdit edits touch `prettypipes:pipe` blocks.
- Cleans Pretty Pipes network data after WorldEdit removes pipes, including `//undo` and `//set 0`.
- Rotates Pretty Pipes module direction-selector data only during confirmed `//paste` operations with deterministic WorldEdit clipboard rotations of 90, 180, or 270 degrees.
- Adds an OP-only `/pipe` command tree.
- Adds persistent pressurizer speed tuning without changing FE consumption.
- Adds a server-side Pipe Filter Wand for quickly copying framed items into Pretty Pipes filter modules.
- Adds editable chat messages and colors through `config/prettypipesplus.properties`.

## Commands

```mcfunction
/pipe speed
/pipe speed <0.1-10>
/pipe messages
/pipe messages on
/pipe messages off
/pipe reload
/pipe tool <player>
/pipe tool <player> <amount>
```

## Configuration

The config file is created at:

```text
config/prettypipesplus.properties
```

After editing message text, colors, the pressurizer multiplier, or WorldEdit summary settings, run:

```mcfunction
/pipe reload
```

## Required mods

- Minecraft 1.21.1
- NeoForge 21.1.x
- Pretty Pipes 1.22.0+
- WorldEdit 7.3.8+

## Supported WorldEdit workflow

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
PrettyPipes+-1.0.0-NeoForge-1.21.1.jar
```
