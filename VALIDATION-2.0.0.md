# Version 2.0.0 validation

Environment: Minecraft 1.20.1, Forge 47.4.20, 64-bit Java 17.0.20.1, Gradle 8.8, Windows 11. Validation date: 2026-09-27.

## Automated server checks

`gradlew.bat runGameTestServer` passes all **13 required tests**:

- Existing HBM door lifecycle and save state across 60 animated-door/facing combinations.
- Existing redstone/part removal, open-door targeting and passage, functional hatches, modular blast-door timing/collision, and the global redstone-only rule for all 21 HBM door/hatch entries.
- Lift route creation, floor bounds, names/descriptions, travel, mid-trip save/reload, arrival and redstone return calls.
- Landing-door opening/collision, departure closure, obstruction pause/resume, and multiblock cleanup.
- Cabin-wall collision, carrying a living passenger, arrival/dismount and removal of temporary collision entities.
- Horizontal and corner routes, rejecting branches, and three-wide landing-door cleanup in all four orientations.
- Escalator four-block placement/removal, forward/reverse/stop cycling, and physically carrying a living entity upstairs.

## Real client checks

`gradlew.bat runClient -PmobilitySmokeTest` creates its own disposable flat test world and exits automatically. It verifies that the actual client player mounts a travelling lift, reaches the destination and dismounts, then rides a six-section escalator uphill without movement input. The final escalator position was `(7.0, 202.9375, -4.09467)`, from `(7.0, 199.94, 0.5)`.

Nine screenshots cover the cabin at ground/moving/upper positions, escalator, setup screen, riding/arrival views, and all 16 mobility inventory items. The imported block/item textures load with no missing-texture warnings. Screenshots were visually inspected; the test player has night vision to make the model details visible in the isolated test platform. The final server tests also check the subsequently added floor-description synchronization.

## Packaging and limits

`gradlew.bat build` produces the reobfuscated production JAR. `python tools/verify_assets.py` checks model/texture/sound references and all 37 recipe files. The JAR is checked for version 2.0.0 metadata, MTR attribution, lift resources, and exclusion of the test harness and MTR runtime libraries.

These checks cover a clean Forge development client and dedicated GameTest server. They do not establish compatibility with every third-party mod, real multi-client network latency, every cabin size/offset combination, or long routes crossing unloaded chunks. The behavioral differences from MTR are listed in `MTR-PORT.md`.
