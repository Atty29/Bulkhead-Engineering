# Bulkhead Engineering 1.3.0 — global redstone-only control

Enabled by default. Replace the previous mod JAR on both client and server. No door replacement is required for this update.

All 21 HBM door/hatch entries block manual opening and closing. This includes animated doors and their invisible structure parts, both halves of the three conventional doors, all three small hatches, and the Blast Door alias. Existing redstone behavior is preserved. Sneak-click model/skin selection remains available. Other mods and vanilla doors are unaffected.

Configuration: `<world>/serverconfig/bulkheadengineering-server.toml` (singleplayer worlds are in `saves/<world>`).

```toml
redstoneOnly = true
```

Change to `false` to restore hand controls. Edit with the world/server stopped, then restart. Forge owns and synchronizes this server configuration; clients cannot bypass it with their own setting. The default is `true` when no existing configuration overrides it.

Most doors open while powered and close when power is removed. Modular blast doors retain their rising-pulse toggle operation. Enabling the setting does not forcibly close doors that are already open.

No runtime dependencies were added. The update adds a Forge SERVER configuration and gates block-use handlers; it does not alter models, animation timings, collision, recipes, or saved block/entity IDs.

Validation: production build plus seven required Forge GameTests. The new regression covers all 21 entries with the setting both enabled and disabled, both interaction hands, upper/frame forwarding, manual opening and closing lockout, redstone opening and closing, and modular falling-edge/pulse behavior. Existing lifecycle, collision, interaction, and modular tests remain in the suite.
