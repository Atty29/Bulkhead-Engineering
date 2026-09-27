# Invisible lift cabin fix — 2.0.2

Replace the old Bulkhead Engineering JAR with bulkhead-engineering-1.20.1-2.0.2.jar and restart Minecraft. Existing cabins and settings are retained; rebuilding lifts is unnecessary. Keep only one Bulkhead Engineering version installed. The 2.0.1 editing/recovery fixes remain included.

## Confirmed cause and change

The supplied SoA 4-0.1.1 CurseForge export lists Minecraft 1.20.1 / Forge 47.4.20, Embeddium 0.3.31 (file 5681725), Oculus 1.8.0 (file 6020952), and Entity Culling 1.10.5 (file 8287086).

The cabin adapter constructed ModelPart objects with empty cube/child collections, then added geometry afterward. Oculus's optimized entity renderer snapshots those collections during construction, leaving its cached cabin geometry empty. This is separate from the visibility bounds issue addressed in 2.0.1.

The adapter now builds its complete geometry and child hierarchy before constructing each ModelPart, caching the completed parts for reuse. Pivot, rotation, texture coordinates, dimensions and animation are preserved. No dependency on Oculus or Embeddium is added to the production mod.

## Reproduction and validation

- Embeddium alone rendered the old cabin. Adding the exact Oculus version reproduced the invisible cabin while landing doors remained visible, even with shader packs disabled.
- With the fix, the same Oculus/Embeddium test rendered the cabin. Before/after screenshots were inspected.
- A new client regression check verifies that optimized renderer caches contain both parent and child geometry. This check passed with Oculus/Embeddium.
- The client smoke test passed lift riding, arrival/dismount and escalator travel after the fix.
- Optional reproduction command: `gradlew runClient -PmobilitySmokeTest -PembeddiumCompatTest`. Optional dependencies are only enabled by the latter flag and are never bundled in the production JAR.
- The full SoA pack was not launched. Its Entity Culling version has nested libraries that failed name remapping in the development launcher; those were excluded from the isolated reproduction. This was a test-environment startup issue, not evidence of a pack fault. No shader pack was enabled during the successful before/after test.

Original MTR geometry, assets and licensing are unchanged. The existing global redstone-only door setting is unchanged.

Production build succeeded; all 14 server GameTests passed. Asset validation passed for models, textures, sounds and 37 recipes. Production JAR resources were checked against source; test harnesses and optional rendering mods are excluded.
