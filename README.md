# HADO — Anime Arsenal

A Fabric mod for **Minecraft 1.21.1 (Java 21)** that makes melee combat feel like a
key animation frame: ink-brush impact frames, freeze frames, camera trauma, speed
lines and title cards — wrapped around an arsenal of impossible weapons.

The design target was "looks expensive, costs almost nothing". Every visual is a
pre-baked texture drawn as a single stretched quad; there is no custom render
pipeline, no shader compilation, no framebuffer copying, and no allocation in the
render loop.

## Install

Two ways in. Either import the ready-made Prism/MultiMC instance in the
release zip, or drop two jars into a Fabric 1.21.1 instance by hand.

**What you need**
- Minecraft **1.21.1**
- **Fabric Loader** for 1.21.1 (built against 0.16.9; mod declares
  `fabricloader >= 0.16.0`)
- **Fabric API 0.116.17+1.21.1** (required; the mod depends on
  `fabric-api: "*"`)
- Java **21**
- `hado-1.0.1.jar` (the mod)
- `fabric-api-0.116.17+1.21.1.jar`

All of those are in the release zip:
- `client/mods/` — the two jars for a manual drop
- `prism-instance/` — a Prism/MultiMC importable instance
- `fabric-installer.jar` — helper if you need to install Fabric Loader
- `server-pack/` — a run-ready server (see below)

**Option A — Prism / MultiMC import**
1. Download `HADO-Modpack-1.21.1-v1.0.1.zip` from the GitHub release.
2. Import the zip (or the `prism-instance/` folder) into your launcher.
3. Launch the instance. Its `minecraft/mods/` already has both jars.

**Option B — manual drop**
1. Install Fabric Loader for Minecraft 1.21.1 (run Fabric Installer if
   you don\'t have it).
2. Put `fabric-api-0.116.17+1.21.1.jar` into the instance\'s `mods/` folder.
3. Put `hado-1.0.1.jar` into the same `mods/` folder.
4. Launch the 1.21.1 Fabric profile.

**Where `mods/` lives** depends on the launcher. Vanilla/Prism-style
instances usually have a `mods/` folder inside the instance directory;
the vanilla launcher\'s default `.minecraft` instance uses
`.minecraft/mods/`.

**Server**
The release zip contains `server-pack/`: run `server-pack/start.bat`
(Windows) or `server-pack/start.sh` (Linux/macOS). First boot downloads
Minecraft 1.21.1 + libraries (~1 min online, then offline). It ships with
EULA accepted, 8 players, online-mode=false, level-name=world, and both
jars already in `server-pack/mods/`.

### How to confirm it loaded
- In the latest log, look for the mod list entry `hado 1.0.1` and the
  line `[HADO] Arsenal forged: 10 items in 14 vaults.`
- In creative, open the **HADO: Arsenal** tab; all 10 items should be there.
- In survival, the same items can also roll in 14 vanilla chest tables
  (cores common, weapons rare, cosmetics only in endgame chests).
- In creative/server console, `/give @s hado:kaze_katana` should give the
  item if the registry is up. If it says the item is unknown, the mod did
  not load on that side.

### If items still don\'t show
1. Confirm you are in a **1.21.1** Fabric instance, not another version.
2. Confirm both jars are really in the instance\'s `mods/` folder.
3. Confirm Fabric API loaded (the log should list Fabric API modules).
4. Search the log for `hado` errors: missing texture/model → asset issue;
   mixin/loader errors → loader or API mismatch; no `hado` line at all →
   the jar wasn\'t picked up (wrong folder, wrong name, cached profile).
5. Restart the instance after adding jars; some launchers cache the mod
   list until a restart.
6. If the instance is on a much newer Fabric Loader than 0.16.9, try a
   1.21.1 Fabric profile on a 0.16.x-era loader. The mod declares
   `fabricloader >= 0.16.0`, so newer usually works, but mismatch is a
   common "runs but shows nothing" cause.

### Client visuals note
The visual effects (impact frames, hit-stop, camera shake, FX overlay,
FX tuner screen) are **client-side only**. The "Arsenal forged" line and
the creative tab prove the mod loaded, but do not prove the overlay
renders. To check the visuals, launch a client and open the FX tuner:
hold the **Starfall Remote**, sneak, and right-click. Config is written to
`config/hado.json` on first launch.

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

Verified here, end-to-end on the released stack:

- The release zip was extracted and the **server pack** was booted with
  bundled JDK 21.0.13 against **Fabric API 0.116.17+1.21.1** and
  `hado-1.0.1.jar` in `server-pack/mods/`.
- `logs/latest.log` shows `hado 1.0.1` in the mod list and
  `[HADO] Arsenal forged: 10 items in 14 vaults.`, and the data pack
  `hado` was loaded automatically.
- Build succeeds locally: `./gradlew build` with Java 21, Loom 1.7.4,
  Fabric API 0.116.17+1.21.1.

Not verified here: a real **client** was not launched from this
environment, so the visual overlay (impact frames, hit-stop, camera shake,
FX tuner screen) has not been confirmed on screen. The `Arsenal forged` line
and the creative tab prove the mod loaded, but not that the client overlay
renders. To check the visuals you need to launch a client and use the FX
tuner (hold Starfall Remote, sneak + right-click) or `/hado demo`.

The mixins target signatures read directly from the mapped 1.21.1 jar
(`Camera.update`, `GameRenderer.render` and its `renderWorld` call site);
those have not been observed applying at runtime here.

## Marker system

A lightweight saved-marker feature plus a new reverse remote. It is **bounded on
purpose**: one active marker per player per session, no marker history, no
cross-player markers, no chunk snapshots, no block-region history, no new world
data structures.

### Reverse Remote

- Item: `hado:marker_remote`, name **Reverse Remote**.
- Model: `item/generated`, texture `hado:item/marker_remote`.
- Recipe: `data/hado/recipe/marker_remote.json` — shaped, uses the same soft
  cornerstone pieces as the rest of the mod.
- Role: the entry point to the marker system.
- Right-click **without sneaking**: opens the **Marker Screen**.
- Sneak + right-click: opens the existing **FX tuner** through the same path the
  Starfall Remote uses today ("PANEL").
- Cooldown-gated: if the marker is already set, a plain right-click tries to
  restore to it; if no marker is set, it opens the marker screen and lets you set
  one.

### Marker Screen

- Opens as a Fabric `Screen` through the existing remote-open-screen path.
- Buttons: **Set Marker**, **Clear Marker**, **Restore to Marker**, **Close**.
- Status line: shows **no marker set**, or the marker target as
  `marker at x, y, z`.
- Status text is kept as a small client-side cue; the marker position itself is
  stored as three doubles plus a short last-event note — no snapshot.

### What Set / Clear / Restore do

- **Set Marker**: records the holder's current position as the marker target and
  a short event note, then shows a "marker set" cue.
- **Clear Marker**: clears the holder's active marker.
- **Restore to Marker**: cues a rewind-style feel (RETURN FX), plays the small
  last-event note back through the client overlay, and teleports the holder to the
  marker target if they are not already there. It does **not** undo the rest of the
  world.

### Limits

- One marker per player, per session. No history.
- No cross-player markers.
- Restore is a bounded cue: teleport-to-marker + rewind-style overlay + small
  last-event note. It is **not** a full world rollback and does **not** touch chunk
  history, block-region state, or other players.
- HADO-specific attack rollbacks are out of scope for this slice; the record stores
  a short note, not a full HADO attack snapshot.

## New items

The marker pass adds 13 items: the Reverse Remote and 12 marker/coordination/
combat-adjacent tools. Each has a distinct class under `dev.hado.item`, is
registered in `HadoItems` and `HadoContent`, has a recipe, a loot row, a lang
entry, an item model, and an `FxKind` it triggers. New FX kinds are added to
`FxKind` and reused through the existing overlay — there is no separate
item-motion animation pipeline.

New FX kinds:

- `SLASHBACK` — counter-slash feel: returned slash plate + decisive hit-stop.
- `RETURN` — rewind pulse: soft rings + trailing afterimage spark.
- `BEACON` — beacon pulse: expanding ring toward a marker target point.
- `TETHER` — thread/tether pull: paired ring + inward pull feel.
- `CHIME` — chime ring: short sustained ring, no freeze.
- `GLOW` — lantern glow: soft expanding glow.
- `TAIL` — kite tail line: trailing line pulse.
- `SWEEP` — map sweep: flat plate sweep.
- `STAMP` — anchor stamp: short ground imprint + short freeze.
- `REVEAL` — scroll reveal: page-turn title card + short soft ring.
- `LENS` — lens flare: sharp lens flare + brief flash.

### Reverse Remote

The hub of the marker system. Right-click to open the marker screen, sneak +
right-click for the tuner. Cues a `TITLE` + `MARKER` control message when it
opens the screen, and a `REWIND` / `RETURN` cue when restoring.

### Marker Beacon

Places a beacon marker where you look and remembers the point. Pulses a
`BEACON` ring toward the target and plays a beacon-activate tone.

### Swift Edge

A quick forward dash-cut with a slashed-back ring. Cues `SLASHBACK`.

### Recurve Map

Draws a tether to the struck point and rings a return. Cues `TETHER` and
`RETURN`; if nothing was struck it cues a miss note instead.

### Drift Compass

Tunes in nearby threats and rings when something drifts into range. Cues `GLOW`
and a small `BLOCK_ANVIL_LAND` thud when it reacts.

### Loom Thread

A quiet tether that stitches between positions. Use it twice to place, again to
clear the marker. Cues `TETHER`.

### Chime Bell

Clears the nearest threat in a clean ring and clears the marker. Cues `CHIME`,
then `CLEAR` for any player hit; it also thuds like glass breaking.

### Glimmer Lantern

A held glow that pulses a halo toward the point you look at. Cues `GLOW`.

### Homing Kite

Flies a tethered kite toward the last marker, or taunts the air if none is set.
If a marker is set it finds the nearest one and cues a short `TAIL` line plus a
`TETHER` pull back toward it; if none is set it taunts the air with a `TAIL`
line.

### Anchor Stamp

Stamps the current spot as a marker anchor and thuds it down. Cues `STAMP` and a
short `ANVIL_LAND`.

### Counter Scribe

Reads the last event note and writes a small tracked annotation into the marker.
If the marker is set it cues `REVEAL` and a note-block pling; if not it cues the
same and a short `REVEAL` line.

### Mirror Lens

Flips the last event note in the current marker and plays a lens glint. Cues
`LENS` and a small glass-break tone if the marker is set.

### Marker Pen

A drawn FX line along the looked-at path. Cues `SWEEP` and a light wool-break
tone.

## Verification status

Verified here, end-to-end on the released stack:

- The release zip was extracted and the **server pack** was booted with
  bundled JDK 21.0.13 against **Fabric API 0.116.17+1.21.1** and
  `hado-1.0.1.jar` in `server-pack/mods/`.
- `logs/latest.log` shows `hado 1.0.1` in the mod list and
  `[HADO] Arsenal forged: 10 items in 14 vaults.`, and the data pack
  `hado` was loaded automatically.
- Build succeeds locally: `./gradlew build` with Java 21, Loom 1.7.4,
  Fabric API 0.116.17+1.21.1.

Not verified here: a real **client** was not launched from this
environment, so the visual overlay (impact frames, hit-stop, camera shake,
FX tuner screen, marker screen) has not been confirmed on screen. The
`Arsenal forged` line and the creative tab prove the mod loaded, but not
that the client overlay renders. To check the visuals you need to launch a
client and use the FX tuner (hold Starfall Remote, sneak + right-click) or
`/hado demo`.

The mixins target signatures read directly from the mapped 1.21.1 jar
(`Camera.update`, `GameRenderer.render` and its `renderWorld` call site);
those have not been observed applying at runtime here.

## Layout

```
src/main/java/dev/hado/
  Hado, HadoContent, HadoNetwork, HadoTasks, HadoLoot, HadoFx, HadoItems
                                        registry, packets, fuse scheduler, FX helper, item handles
  item/                                      ten items + 13 marker items, server-authoritative
  fx/                                        FxKind / FxStyle vocabulary
  net/FxPayload                              the single S2C packet
  client/fx/                                 engine, overlay, shake, hit-stop, quality
  client/gui/HadoPanelScreen, HadoMarkerScreen
                                        live tuner + marker screen
  client/HadoClientCommands                  /hado demo | panel | reload
  client/ClientScheduler                     client tick choreography
  client/config/HadoConfig                   JSON config
  marker/                                     marker record + storage + remote + screen + control
  mixin/                                     camera shake + hit-stop
src/main/resources/assets/hado/textures/     generated art
```
