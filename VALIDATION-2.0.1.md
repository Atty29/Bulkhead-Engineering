# Lift recovery update — 2.0.1

Minecraft 1.20.1, Forge 47.4.20, Java 17. Replace the previous Bulkhead Engineering JAR on client and server; keep only one version installed.

## Recover an existing lift

Right-click its cabin wall/floor with Lift Wrench or Lift Refresher. If the cabin is invisible, use Lift Refresher normally on its connected track. This pauses the lift in place, releases riders onto its platform, and opens cabin settings without requiring an empty or idle lift. Apply settings, then request a floor to resume. Remove any blocks obstructing the cabin's travel.

Do not sneak-use the refresher unless intending to remove the cabin. To rebuild a changed route, remove the empty cabin at a landing, then create it again with the refresher. Normal refreshing now preserves the existing route and position instead of teleporting the cabin to its first stop.

## Changes

- Rendering bounds include the cabin's full height and dimensions; cabin renderer bypasses vanilla frustum rejection. This addresses the previous floor-only entity bounds. The user's exact Sodium/performance-mod combination was not available for testing, so resolution of that modpack's invisibility is not yet confirmed.
- Obstruction and missing-track checks no longer leave the cabin marked as moving. Riders are captured only after a movement step is unobstructed, and only near the cabin floor.
- Wrench/refresher can pause occupied, moving or blocked cabins for editing. Maintenance state persists across saves; a floor request resumes operation.
- Editing between floors retains the fractional route position. Landing doors do not open for a cabin paused between floors. Requesting its previous floor correctly returns it to that landing.
- Invisible collision entities now display the translated name 'Lift Cabin'.
- No new runtime dependencies. Existing door redstone-only configuration remains unchanged.

## Validation

- Forge production build succeeded.
- All 14 required server GameTests passed, including occupied-car recovery, paused save/load, fractional-position editing, return to the previous landing, full-height visibility bounds, obstruction recovery and existing door/lift/escalator cases.
- Isolated Forge client smoke test passed: cabin rendered from inside, player rode the lift, arrived and dismounted; escalator carried the player upstairs. Screenshot inspected. This client test did not include Sodium or third-party culling mods.
- Asset validator passed: model, texture and sound references and 37 recipes.
- Production archive verified against source resources; test harness and MTR runtime libraries excluded.
