# Bulkhead Engineering 2.1.0

Minecraft 1.20.1 / Forge 47.4.20 / Java 17.

## Door identity and Pick Block

Every animated door controller and child uses the same read-only DoorTarget resolver. Children validate their stored controller, declared local offset and rotated world position before resolving the owning block entity. Missing/unloaded controllers, self-links and invalid offsets return an empty item safely. No coordinates or displayed door names are hardcoded in production code.

Pick Block returns the owning block's registered placement item. Non-default styles use the DoorVariant item tag. Placement and inventory rendering read that tag, so copying a styled door preserves its appearance without copying movement, power, coordinates or other block-entity state. Conventional doors, trapdoors and lift landing doors already use their own registered block/item on every section; these are covered by additional tests.

Jade uses its usePickedResult integration for the door blocks and shared frame. WTHIT has an optional header/icon provider which replaces its object-name line with the resolved item's translated name. The mod label comes from Forge's renamed metadata. Both integrations work client-side without making either overlay a required runtime dependency.

Normal breaking/drop logic and door movement/redstone logic were not changed. The global redstoneOnly setting still defaults to true.

## Branding and namespace

The display name, creative tab, Gradle project/group/JAR name, Java package, registry namespaces, resources/data directories, translation keys, model references, recipes, tags, loot tables, sounds, network identifiers, tests and asset validator now use Bulkhead Engineering / bulkheadengineering.

New JAR: bulkhead-engineering-1.20.1-2.1.0.jar.
New server config: <world>/serverconfig/bulkheadengineering-server.toml.

This intentionally changes the unreleased registry identity. Old development worlds/inventories using the previous namespace are not automatically migrated. Keep those backups separate; use a new world or perform an explicit migration before loading old saves. Worlds created with the new namespace retain identity and styles across reopening.

HBM and Minecraft Transit Railway copyright, attribution and licence files are unchanged. The existing GitHub repository URL remains the source location.

## Files/classes with behavior changes

Paths below are relative to the source project's root. All existing Java files also moved from com/hbm_doors to com/bulkheadengineering with corresponding package/import changes.

- src/main/java/com/bulkheadengineering/DoorTarget.java — new ownership validation, normalized style encoding and complete-door item lookup.
- src/main/java/com/bulkheadengineering/DoorPartBlock.java — Forge getCloneItemStack delegates every child to the resolver.
- src/main/java/com/bulkheadengineering/AnimatedDoorBlock.java — controller clone-item handling and restoration of a picked style during placement.
- src/main/java/com/bulkheadengineering/DoorItemRenderer.java — renders the saved style in the picked placement item.
- src/main/java/com/bulkheadengineering/compat/JadeDoorsPlugin.java — optional Jade picked-result integration.
- src/main/java/com/bulkheadengineering/compat/WthitDoorsPlugin.java — optional WTHIT translated header and item icon provider.
- src/main/resources/waila_plugins.json — WTHIT plugin discovery.
- src/gametest/java/com/bulkheadengineering/DoorIdentityTests.java — exhaustive styles/sections, Creative/Survival, NBT reload, corrupt owners, single-drop and other-door/recipe/namespace checks.
- src/gametest/java/com/bulkheadengineering/IdentitySmoke.java — real client Pick Block, overlay, Mods menu and saved-world reopen tests.
- src/gametest/java/com/bulkheadengineering/JadeIdentityProbe.java — checks collected live Jade object-name text in the test client.
- build.gradle — new artifact/group/version, compile-only overlay APIs, optional overlay test runtimes, identity test and fresh server-test directory.
- settings.gradle, META-INF/mods.toml, pack.mcmeta, language and resource/data files — rebrand and namespace changes.
- tools/verify_assets.py, ASSET-MANIFEST.json and documentation — updated paths, validation and release instructions.

CHANGED-FILES-2.1.0.tsv gives the exact Git change list, including every renamed class, model, texture, sound, recipe, tag and loot table.

## Verification

The exhaustive server identity test made 7,304 controller/child picks across all 15 animated registry entries (14 types plus the Blast Door alias), all available styles, four orientations, both game modes and serialized/reloaded controller/child data. Corrupt-link checks and exactly-one-drop checks cover every animated entry.

Separate real client runs with Jade 11.13.3 and WTHIT 8.21.1 verified the Secure Access Door controller and frame in Creative and Survival. Both overlays visibly showed Secure Access Door and Bulkhead Engineering. Survival selected an existing matching item and created nothing when absent. The Forge Mods menu displayed Bulkhead Engineering. All 46 door model/style resources loaded. A saved test world was closed and reopened without replacing its door; its style, frame resolution, live Jade name and both-mode Pick Block checks passed again.

These live UI screenshots are representative checks; exhaustive every-part/every-style coverage is automated through the shared resolver/Forge clone-item path. No manual screenshot is claimed for every possible door section.

The final clean build passed all 17 server GameTests in a fresh namespace-only world. All 37 recipes were present in the server recipe manager. A client without either overlay loaded and completed lift travel, passenger arrival/dismount and escalator ascent under the new namespace. Offline model/texture/sound references and 37 recipe files passed validation. Production archive inspection confirmed only the new namespace, optional compatibility classes, matching source resources, unchanged attribution files, and no bundled test classes or third-party JARs. No missing-registry errors occurred in the fresh-world or new-namespace reopen tests.
