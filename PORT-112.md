# HBM 1.12 modular sliding blast door

Version 1.1.0 adds **Modular Sliding Blast Door** (`bulkheadengineering:modular_blast_door`), matching the tall vertical door in the reference screenshot. The source calls this block `blast_door`; its separate `sliding_blast_door` is a different, side-opening design already represented by the existing port's variants.

Place one section on the floor with seven clear blocks of height. Each section is one block wide, with a five-block-high opening between the base and top beam. Place sections next to one another to make a wider door. Right-click a section to toggle the connected wall. Each rising redstone edge at the base or top toggles it; removing power leaves its current state unchanged. Motion takes 100 ticks (five seconds at 20 TPS). Inputs while moving are ignored.

The crafting recipe is seven iron blocks, one piston and one redstone dust:

```text
Iron block | Iron block | Iron block
Iron block | Piston     | Iron block
Iron block | Redstone   | Iron block
```

## Retained and adapted

- Original base, top beam, striped moving edge and sliding-panel OBJ geometry, UVs, textures and reactor movement/end sounds.
- Adjacent horizontal sections open/close together. Iterative traversal replaces recursive propagation, and only already-loaded chunks are traversed.
- Original five-second travel and staged collision clearing/restoration every 20 ticks. Top and base stay solid.
- Forge 1.20.1 registration, block entities, rendering, state synchronization and saved animation progress use the standalone mod's infrastructure.
- The original source's reversed animation/state wiring is corrected so OPEN consistently means a clear passage. All sections move toward the same target state.
- The existing `blast_door` decorative cube is retained. No existing registry ID is replaced or existing world block silently expanded into a seven-block structure.

## Differences

HBM's radiation simulation, key/pin locks, lockpicking, detonator interface and control-panel events are omitted. Manual and pulse-redstone controls are available. The open passage retains invisible, non-colliding structural parts, so other blocks cannot be placed inside its reserved footprint. Breaking a part removes only that column; adjoining columns remain.

This addition does not add the separate 1.12 keypad/control system or its side-opening keypad door. Full multiplayer and extended survival-play testing have not been performed. Original authors and pinned commit are recorded in `UPSTREAM-112.txt`; upstream license copies are under `licenses/`.

## Tests

The source includes dedicated Forge GameTests for the existing doors, the new door in all four orientations, three-column linked walls, every opening/closing collision threshold, mid-motion NBT, removal isolation and pulse-redstone control at both ends. The optional client smoke test checks all 46 variants and renders a three-section wall closed, halfway open and open. Actual execution results are recorded in `VALIDATION.txt`.
