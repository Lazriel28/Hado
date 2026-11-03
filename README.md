# HADO — Anime Arsenal

A Fabric mod for **Minecraft 1.21.1 (Java 21)** that makes melee combat feel like a
key animation frame: ink-brush impact frames, freeze frames, camera trauma, speed
lines and title cards — wrapped around an arsenal of impossible weapons.

The design target was "looks expensive, costs almost nothing". Every visual is a
pre-baked texture drawn as a single stretched quad; there is no custom render
pipeline, no shader compilation, no framebuffer copying, and no allocation in the
render loop.

## Install

1. Install **Fabric Loader 0.16+** for Minecraft **1.21.1**.
2. Install **Fabric API** (`0.116.17+1.21.1` or newer).
3. Drop `build/libs/hado-1.0.1.jar` into `.minecraft/mods`.
4. Multiplayer: install on **both** the server and every client.

A complete pre-verified distribution is one folder up: `../HADO-Modpack-1.21.1-v1.0.1.zip`
with a client mods folder, a Prism-importable instance and a run-ready
server pack (`server-pack/start.bat`). The server pack was booted
end-to-end with these exact three jars.

## Build

```bash
# from this directory, with JDK 21 on PATH (or JAVA_HOME set)
./gradlew build          # -> build/libs/hado-1.0.0.jar
```

Regenerate the art (the textures are generated, not hand-drawn):

```bash
python ../tools/gen_assets.py
```

## The arsenal

| Item | Role |
| --- | --- |
| **Gale Fang** (katana) | Right-click **Gale Step**: a dash-cut through everything in a cone. Long slash frame + freeze. |
| **Stellar Cannon** | Marks the point under your crosshair, telegraphs for 8 ticks, then drops a beam on it. Full white-out. |
| **Kurotama** | Void bomb with a 34-tick fuse. Pulls targets inward, then consumes them. Inverted plate, heavy vignette. |
| **Senbon Storm** | Five-needle hitscan barrage. Speed lines only — deliberately *no* freeze so barrages stay smooth. |
| **Meteor Drill** | Pickaxe that punches; right-click overclocks the drive for Haste III. |
| **Absolute Barrier** | Short perfect-guard window: Resistance III + Absorption, hex-grid impact frame. |
| **Starfall Remote** | Cycles visual intensity 0–3. |
| **Gensō Scroll** | Cycles the palette: shonen ink / cursed energy / orbital. |
| **Soul Core, Impact Core** | Crafting materials. |

All ten are craftable; see `src/main/resources/data/hado/recipe/`.

Since v1.0.1 there is a survival acquisition path beyond crafting:
`HadoLoot` seeds the arsenal into **14** vanilla chest tables via the
Fabric loot API — cores are common (dungeons, mineshafts, buried
treasure), weapons are rare (the katana is ~2–3% in stronghold
corridor / ancient city), and the cosmetics (style scroll, starfall
remote) only appear in endgame chests (ancient city, end city,
woodland mansion). This is code, not datapack JSON, so it tracks any
realm you name it against.

## How the anime look is built

Five baked impact plates (`textures/fx/impact/frame_*.png`) plus a handful of
plates (speed lines, ink slash, star flare, shock ring, hex grid, grain,
vignette). At draw time a plate is tinted per palette, so three distinct visual
identities share **one** texture set.

Per hit, up to seven layers are composites, each **one draw call**:

1. impact plate (strobing between two frames — the actual "impact frame")
2. speed lines (rotated, scaled)
3. ink slash / hex grid / shock ring depending on kind
4. star flare at the projected impact point
5. vignette
6. film grain
7. white flash + title card

### Why it stays fast

- **Nothing is drawn per pixel.** No post-processing shaders, no full-screen
  shader pass, no per-frame texture uploads.
- **Nothing allocates per frame.** The FX instances live in a fixed 16-slot pool
  that is recycled; projection scratch vectors and the shake output array are
  static.
- **Adaptive quality.** `QualityGovernor` tracks a smoothed frame time and drops
  layers from the outside in when the frame budget tightens: grain first, then
  speed lines and flares, then secondary ink passes. The impact plate itself is
  the last thing to go.
- **One packet per hit.** A hit sends a single small S2C payload to players
  within 96 blocks. The client does no simulation of its own.
- **Freezes are render-only.** `HitStop` stops the world *draw*, not the tick —
  physics, networking and input keep running, so a freeze can never desync.
- **Delayed effects are not entities.** Bomb fuses are tick-scheduled runnables,
  so a pending bomb costs a list slot instead of a loaded chunk.

Total shipped art: **26 textures, under 1 MB**.

## Tuning it live

```
/hado            open the tuner
/hado demo       play every effect in sequence (~5.5 s)
/hado panel      open the tuner
/hado reload     re-read config/hado.json
```

These are **client commands**, so they work in singleplayer, on a modded server
and on a vanilla server alike — no permissions, no packets.

`/hado demo` plays every effect kind in order — title card, impact, slash,
barrage, drill, parry, implosion, orbital — and rotates the palette on each run,
so three runs show all three visual identities.

The tuner (also reachable with **sneak + right-click** while holding the Starfall
Remote) is a deliberately transparent panel: it does **not** pause the game and
draws no full-screen dimming, because its whole job is to let you watch the
effects change while you drag the sliders. It has live sliders for intensity and
shake, toggles for hit-stop, grain and the master switch, quality and palette
cyclers, a frame-time readout, and a **Test hit** / **Run demo** / **All effects**
stress button. Values are written to `hado.json` when you close the panel.

The panel is also the one exception to the rule that open screens suppress
shake and hit-stop (`ScreenGate`), so previews inside it behave exactly as they
do in combat.

## Configuration

`config/hado.json`, created on first launch:

```json
{
  "enabled": true,
  "intensity": 2,
  "hitStop": true,
  "shake": 1.0,
  "styleOverride": -1,
  "qualityCap": 3,
  "grain": true
}
```

Editing the file by hand works too — `/hado reload` picks it up without a
restart, and so does the **Reload hado.json** button in the panel.

`intensity: 0` gives a restrained look; `3` is maximum. Set `hitStop: false` if
freeze frames bother you, or `enabled: false` to disable the whole overlay and
get vanilla visuals with the same weapons.

> **Photosensitivity:** this mod flashes the screen, including strobe-like
> frame alternation. Set `intensity` to `0` or `enabled` to `false` if that is a
> problem for you.

## Verification status

Verified:

- `./gradlew build` succeeds (Java 21, Loom 1.7.4, **Fabric API 0.116.17+1.21.1**).
- The shipped **server pack** boots 1.21.1 end-to-end over the Fabric
  server launcher (`fabric-server.jar`): `Done (26.870s)!`, zero
  errors, with the log line `[HADO] Arsenal forged: 10 items in 14
  vaults.` proving the mod, the items and the chest-loot registration
  all load on that stack.

Not verified here: the client was never launched, so the visual result, the
hit-stop freeze and the camera shake have not been confirmed on screen. The
mixins target signatures that were read directly from the mapped 1.21.1 jar
(`Camera.update(BlockView, Entity, ZZF)`, `GameRenderer.render(RenderTickCounter, Z)`
and its `renderWorld(RenderTickCounter)` call site), but they have not been
observed applying at runtime.

## Layout

```
src/main/java/dev/hado/
  Hado, HadoContent, HadoNetwork, HadoTasks   registry, packets, fuse scheduler
  item/                                      ten items, server-authoritative
  fx/                                        FxKind / FxStyle vocabulary
  net/FxPayload                              the single S2C packet
  client/fx/                                 engine, overlay, shake, hit-stop, quality
  client/gui/HadoPanelScreen                 live tuner
  client/HadoClientCommands                  /hado demo | panel | reload
  client/ClientScheduler                     client tick choreography
  client/config/HadoConfig                   JSON config
  mixin/                                     camera shake + hit-stop
src/main/resources/assets/hado/textures/     generated art
```
