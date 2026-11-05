<p align="center"><img src=".github/assets/banner.png" alt="KeybindKeeper" width="100%"></p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?style=flat-square" alt="Minecraft 1.21.1">
  <img src="https://img.shields.io/badge/Loader-NeoForge-F0323F?style=flat-square" alt="NeoForge">
  <img src="https://img.shields.io/badge/Side-Client%20only-4C8BF5?style=flat-square" alt="Side">
  <img src="https://img.shields.io/badge/License-LGPL--3.0--only-555555?style=flat-square" alt="LGPL-3.0-only">
</p>

**Keybind Keeper** keeps your keybinds safe, for Minecraft 1.21.1 with NeoForge.

## Why

When a mod is missing for even one session (you removed it to test something, or it failed to load), vanilla Minecraft quietly **deletes all of that mod's keybinds**. When the mod comes back, you have to set them up again.

## What it does

- **Keeps keybinds for missing mods**, so they come back exactly as they were when the mod does.
- Adds **"Reset Options..."** to the Controls screen, with two choices:
  - reset to the **modpack's defaults**, or
  - reset to **true vanilla defaults**.

Client-side only. Nothing to set up.

## Feedback, bugs and feature ideas welcome

This mod is actively developed, and your input shapes what comes next. Found a bug, have an idea for a feature, or think something could work better? **Please share it** in **[Issues](https://github.com/SteelArchadian/KeybindKeeper/issues)**. Every report and suggestion gets read, and feature requests are genuinely encouraged: good ideas get built.

## Building

```
./gradlew build
```

The jar is written to `build/libs/`.

## Credits and license

Licensed under the GNU Lesser General Public License v3.0 only: see [LICENSE](LICENSE) (with the GPL text it builds on in [COPYING](COPYING)). Made with AI assistance.
