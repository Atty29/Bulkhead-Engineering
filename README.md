# HBM Doors Standalone

Minecraft **1.20.1**, Forge **47.4.20**, Java **17**. Install the built JAR in the `mods` folder on both the client and server. HBM, Architectury, Cloth Config, Create and other third-party mods are not required.

Includes all 14 animated door/hatch declarations, the three conventional doors, and four door/hatch-named decorative blocks registered by upstream. The decorative blocks retain their upstream solid-block behavior.

The new **Modular Sliding Blast Door** from HBM 1.12 forms linked walls of seven-block-tall sections. See [PORT-112.md](PORT-112.md) for its pulse-redstone controls and crafting recipe.

Right-click an animated door or its frame to open/close it. Supply redstone at the controller or any structure part to control it. Sneak-right-click an idle door to cycle through its original models/skins. Placement requires the full structure footprint to be free. Removing any part removes the whole door.

All items are in the **HBM Doors** creative tab. Each has a distinct vanilla-material crafting recipe in `src/main/resources/data/hbm_doors/recipes`. The namespace is `hbm_doors`; this does not convert existing HBM worlds or inventories.

## Build and test

Set `JAVA_HOME` to a 64-bit JDK 17, then run:

```text
gradlew.bat build
gradlew.bat runGameTestServer
gradlew.bat runClient -PdoorSmokeTest
```

On Linux/macOS use `bash gradlew` instead. First builds require Internet access for Gradle, Forge and Minecraft dependencies. The production JAR is written to `build/libs/hbm-doors-1.20.1-1.1.0.jar`. Tests live in the separate `gametest` source set and are excluded from this JAR. The optional client smoke test renders an inventory gallery, writes `run/door-gallery.png`, then closes Minecraft.

See [DEPENDENCY-REPORT.md](DEPENDENCY-REPORT.md) for scope, changes, limitations and validation. [UPSTREAM.txt](UPSTREAM.txt) pins the source commit. [ASSET-MANIFEST.json](ASSET-MANIFEST.json) inventories packaged resources; [UPSTREAM-DEPENDENCIES.json](UPSTREAM-DEPENDENCIES.json) records the original Java import graph.

This is an independent extraction, not an official HBM release. Original code/assets remain credited to the HBM-Modernized contributors and their upstream authors. Distributed under the repository's GPLv3 license; see [LICENSE](LICENSE) and [NOTICE](NOTICE).

![Modular blast door closed, halfway open and open](docs/modular-blast-door-preview.png)
