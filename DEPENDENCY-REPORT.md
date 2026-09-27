## 2.0.0 lifts and escalators

[MTR-PORT.md](MTR-PORT.md) documents the added MTR assets, lift/escalator systems, runtime replacements, setup instructions, and behavioral differences. No additional runtime mod is required. See [VALIDATION-2.0.0.md](VALIDATION-2.0.0.md) for current checks.

## 1.3.0 global controls

[REDSTONE-ONLY.md](REDSTONE-ONLY.md) documents the new global server configuration. Redstone-only mode defaults to enabled for every HBM door/hatch entry. No dependencies or assets were added. Earlier descriptions of hand controls apply with `redstoneOnly = false`.

## 1.2.0 corrections

The current build replaces the four decorative placeholders with functional entries and corrects the renderer and collision defects described in [FIXES-1.2.0.md](FIXES-1.2.0.md). The extraction inventory below describes the original 1.0 baseline; its placeholder behavior and older validation counts are superseded by that report. No additional runtime dependencies were added.

Version 1.1.0 additionally ports the vertical modular 1.12 blast door; see [PORT-112.md](PORT-112.md). The original extraction report below describes the 1.0.0 base.

# Door extraction report

Source: Raptor324/HBM-Modernized, commit `0365cef5956016913095eeeb9366f26c601ca9a0` (cloned 2026-09-26). Upstream is now a multi-loader/multi-version project; its Forge target explicitly specifies Minecraft 1.20.1 and Forge 47.4.20. This extraction uses those versions, Java 17 and ForgeGradle 6.0.54 / Gradle 8.8.

## Included content

| Category | Registry IDs |
|---|---|
| Animated doors | `large_vehicle_door`, `round_airlock_door`, `fire_door`, `sliding_blast_door`, `sliding_seal_door`, `secure_access_door`, `qe_sliding_door`, `qe_containment_door`, `water_door`, `vault_door`, `cargo_door` |
| Animated hatches | `silo_hatch`, `silo_hatch_large` |
| Conventional doors | `door_bunker`, `door_office`, `metal_door` |
| Upstream decorative placeholders | `blast_door`, `fusion_hatch`, `seal_hatch`, `trapdoor_steel` |

The four placeholders are plain cubes upstream; this extraction does not claim that they open. `transition_seal` is a separate structural seal, not a registered door/hatch, and is excluded.

## Dependency decisions

| Upstream subsystem | Standalone treatment |
|---|---|
| `DoorDecl`, `DoorDeclRegistry`, animation interface, model-selection value types | Retained under `com.bulkheadengineering.legacy`; resource namespace changed to `bulkheadengineering`. Removed the unused selection overload requiring the full client model registry. |
| `DoorBlock`, `DoorBlockEntity`, `DoorBlockItem` | Replaced with door-specific Forge blocks, controller entity and item. Original timing, procedural transforms, click suppression during motion, redstone edge semantics and cargo sound callbacks are reused. |
| Universal machine parts, multiblock helpers and controller interfaces | Replaced by `DoorPartBlock`/`DoorPartEntity` and footprint/shape rotation code. No machinery, energy, fluids or conveyors are registered. |
| Door model loaders/baked models, VBO renderer, mesh caches, shader batching/culling | Replaced with a small client-only OBJ/COLLADA renderer. Original OBJ geometry, material texture assignments, hierarchy transforms and COLLADA curves are used. Original item display transforms are retained. |
| COLLADA support | Retained `DaeModel`, `DaeNode`, `DaeMesh`, `DaeAnimation`, `DaeCurve`, `DaeTransform`, format exception; added a minimal Minecraft resource resolver. |
| Model/skin registry, screwdriver UI and custom selection packet | Original configuration files are compiled into a small server-safe variant table. Sneak-right-click cycles styles; ordinary block-entity packets synchronize state. |
| Sounds/client sound bootstrap | Door-only sound registry and tickable movement loops; original sound files and declaration-specific start/end sounds retained. |
| Main registry, block/item/entity registration, creative tabs | Replaced with Forge deferred registries containing only the listed items and an invisible door-part block. |
| Recipes and generated data | Twenty distinct vanilla-only recipes replace HBM-material recipes. Conventional door models/blockstates, loot tables and tags are supplied explicitly. Only the lower half of a conventional door drops its item. |
| Create contraption state, movement/interaction hooks and networking | Removed. Doors operate as stationary structures; no Create integration is claimed. |
| Architectury/platform hooks, Cloth Config, mixins, irradiation, machines, weapons, world generation | Removed; none is a runtime dependency. |

Assets include the complete door model/texture directories, referenced movement sounds, and the specific standard/decorative door textures. A generated manifest gives file-level hashes. The import graph documents the original coupling through large shared registries; it is not a list of runtime dependencies of this mod.

## Simplifications and differences

- The original screwdriver customization screen is replaced by sneak-right-click cycling; models/skins are preserved but the UI differs.
- Collision uses the declaration's closed shapes throughout motion and its open shapes only when fully open. HBM's dynamic outline/collision refinements are not reproduced. This conservative choice can temporarily block passage during animation.
- Shader-specific clipping planes, baked chunk geometry, instancing, special occlusion culling and external shader compatibility layers are omitted. Sliding/retracting panels can extend into surrounding blocks while animating. Lighting and performance can differ from HBM's renderer.
- No Create-moving-contraption support or HBM remote-control/lock integrations. Normal manual and redstone operation remain available.
- Crafting uses vanilla materials and is not balanced to HBM progression. Door hardness/blast resistance use upstream's 10/1000 values.
- This is a new namespace, not an automatic migration for existing HBM blocks, saves or selection NBT. Skin indices are specific to this release.

## Validation

The Forge build and production reobfuscation succeeded. Dedicated GameTests passed for all 13 animated types in four orientations (52 lifecycle combinations), block-entity NBT round trips, multiblock cleanup, redstone opening/closing and removal through a child part. These run with Forge and this mod only.

All referenced animated model and texture paths were audited. A real Minecraft client loaded all 45 model/skin variants and rendered all 13 default animated-door inventory models; its saved gallery was visually inspected. Full in-world visual/animation comparison, multiplayer gameplay, shader packs, and long-duration world/chunk-unload testing have not been performed. Final build evidence is recorded in `VALIDATION.txt`.
