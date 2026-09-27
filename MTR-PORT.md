# MTR lifts and escalators — standalone port, version 2.0.2

For Minecraft 1.20.1 / Forge 47.4.20 / Java 17. Install the same Bulkhead Engineering JAR on the server and every client. Neither MTR nor HBM is required. Existing HBM door IDs and the default global `redstoneOnly = true` setting are retained. Lift call buttons remain usable; lift landing doors open automatically only when their linked cabin docks.

## Included

- Escalator steps and handrails, with automatic landing/slope/transition geometry and the original animated forward/reverse/stopped textures.
- Vertical, horizontal, diagonal/corner, and floor lift tracks.
- A configurable cabin using MTR's original `ModelLift1` geometry and texture: width/depth 2–16, height 2–16, direction, offsets, and one or two entrances.
- Two- and three-block-wide landing doors, both panel styles in both widths, call buttons, refresher, connector, remover, and a lift wrench replacing MTR's shared brush.
- Named floors, floor descriptions, arrival bell, floor-selection/setup screens, queued calls, redstone calls, multi-lift call-button links, and lockable controls.
- Server-owned lift motion, automatic passenger pickup/dismount, cabin collision, obstacle stops, and persisted routes/calls/cabin settings.
- 16 new obtainable items with vanilla-material recipes; 13 registered block types and two entity types. Collision entities are temporary and recreated after loading.

## Build a lift

1. Place **Lift Floor Track** at the cabin-floor height of every landing. Join these markers with **Vertical Lift Track**. Keep all tracks facing the same way, with at least two floor markers. A simple first build has markers six blocks apart, joined by five vertical tracks.
2. Leave a clear shaft for the entire cabin. The default cabin is 3 × 3 × 3. Its entrance faces opposite the track's facing; the cabin sits in front of the track. Keep redstone wiring outside the cabin's path.
3. Right-click a track with **Lift Refresher** to create the cabin and open setup. Adjust dimensions, orientation and offsets as needed. Right-click the cabin with **Lift Wrench** to reopen setup. Using the refresher on an existing lift pauses it in place and reopens setup, even if occupied or obstructed. Riders are released onto its platform. Calling a floor resumes operation. To rebuild a changed track route, remove the empty cabin at a landing and create it again. Sneak-use the refresher removes the cabin while retaining the tracks.
4. Use **Lift Wrench** on a floor marker to set its number/name, description, and arrival bell. Do this before building walls that conceal the marker.
5. Place a **Lift Call Buttons** block at each landing. With **Lift Link Connector**, first click that landing's floor marker, then click the buttons. The same two-step procedure links landing doors and floor panels. Linking any part links the entire door/panel. A button can link up to 16 markers; when multiple lifts are linked, it calls the nearest candidate, preferring idle cabins.
6. Click a linked call button to summon the lift. Inside, right-click the cabin wall/floor to choose a destination. Linked panels also open floor selection. The cabin closes, travels, then opens and releases riders. The landing door opens only while the cabin is docked at its linked floor.
7. Use **Lift Link Remover** to select a marker and remove its link from a fixture. Sneak-use it on a fixture to clear that fixture's links after selecting a marker. The wrench locks/unlocks manual button/panel operation; redstone calls still work.

A rising redstone signal next to a floor marker or linked fixture requests that floor. Constant power does not repeatedly enqueue calls. Calls run in queue order. Landing doors cannot be forced open by hand.

Horizontal tracks connect sideways relative to their facing. A diagonal track is a corner connecting one vertical direction to one horizontal direction: clicking its upper half selects upward, lower half downward; sneaking chooses the right-hand connection, otherwise the left. Floor tracks can connect vertically or sideways. Routes must be one unbranched, non-looping path, with at most 512 track blocks, and their chunks must be loaded. This mod does not force-load a route.

## Build an escalator

Each **Escalator** item places a pair of steps with their two handrails, a 2 × 2 block footprint. Place successive sections facing uphill: two at the bottom height, then sections one block higher per block forward, then a final flat section at the top. Neighbors automatically choose landing, transition, and slope models. Flat connected runs can act as moving walkways.

Right-click a connected escalator with **Lift Wrench** to cycle **forward → reverse → stopped → forward**. The change propagates through connected sections sharing the facing. Stand on the steps to ride; sneaking suppresses their push. Breaking any step or handrail removes its four-block section.

## Dependencies and deliberate differences from MTR

Source: Minecraft Transit Railway commit `739dba4488fd17aaee9a601276387e315876f39e` (MIT, Copyright 2022 Jonathan Ho). `UPSTREAM-MTR.txt` pins it; `licenses/MTR-MIT.txt` preserves the license. `MTR-DEPENDENCIES.json` records the inspected Java roots/helpers and their imports. Original cabin authoring data is included at `docs/lift_1.bbmodel`.

| Original dependency | Standalone replacement |
| --- | --- |
| Mapping wrappers and cross-loader registries | Forge 1.20.1 registries, blocks, items and block entities |
| Transport Simulation Core lift data/operations and shared client data | Bounded track discovery and a saved server lift entity |
| MTR railway packets and simulator synchronization | A lift-only Forge channel with server distance/permission checks |
| Shared renderer, model adapters and resource loader | Original cabin geometry through a small Forge model adapter; original block models/textures |
| Railway vehicle riding/movement code | Automatic temporary vanilla riding at each passenger's relative position |
| MTR brush and configuration/selection screens | Lift Wrench and dedicated Forge screens |
| Dynamic railway display/announcement system | Basic floor text, movement indicator, floor selection and optional vanilla bell |

This is a **functional adaptation of the built-in lift/escalator set, not a byte-for-byte MTR subsystem**. It intentionally differs in these areas:

- Passengers retain their relative positions while travelling and automatically dismount on arrival; MTR's free walking inside a moving vehicle is not reproduced. Vanilla sneak-dismount remains available.
- Motion uses a fixed 0.08 blocks/tick and a simple queued dispatcher, rather than MTR's acceleration and directional scheduling. Up/down call-button areas both summon the lift to that floor.
- The original built-in cabin model is retained. MTR railway resource-pack style presets, dashboard integration, scripted vehicle content, custom announcements, animated display text, and link-line overlays are not imported. Standard resource packs can replace the packaged textures.
- Displays use Minecraft's font and simplified status text. The wrench currently uses the vanilla iron-hoe icon. Waterlogging behavior from MTR's common block base is not implemented.
- Cabin walls use thin rectangular collision panels. Landing-door collision clears after opening rather than continuously tracing each moving leaf. Escalator movement uses Forge/vanilla physics.
- Recipes are standalone vanilla-material recipes. Existing MTR world data, block IDs and saved lifts are not converted. No railway systems, trains, stations, fares, web server, or shared simulator libraries are packaged.

## Validation

See `VALIDATION-2.0.2.md` for the final build/test results and their limits. Test harnesses are in the separate `gametest` source set and are excluded from the shipped JAR.
