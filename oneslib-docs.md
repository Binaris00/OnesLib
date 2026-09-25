# Ones Lib — Technical notes and use cases

Lightweight guide for Streavent projects built on **Ones Lib**. Read it on demand when the project depends on
Ones Lib (or the user says it's a Ones Lib project) and you need context on how these features are built,
or to follow the Init section below. Do not pre-load it.

## General context

- Ones Lib is a **standalone library** (not Techy): playable morphs (**Ones**) on top of Walkers + Remorphed,
  with keybind abilities, effect utilities and GameTests.
- What it replaces from Techy: morph registration/lifecycle, the ability lifecycle (activation, cooldown,
  duration, sync), animation state sync, and the server effect utilities (particles/sounds/titles/area/raycast).
- What it does **not** provide (use your own or Techy): advancement ownership, HUD/icons, morph UI,
  missions, timers/bossbars, bots, JSON morph definitions.
- Ones and abilities are defined **in code**, not JSON.
- Work is done on a very tight schedule: prioritize the feature working over refactoring.
- Prefer simple, well-known solutions over inventing new architecture.

---

## Init (Ones Lib setup)

### 1. Dependency

**Option A — published (Cloudsmith):**

```gradle
repositories {
    maven { url 'https://dl.cloudsmith.io/public/binaris/oneslib/maven/' }
}

dependencies {
    implementation fg.deobf('com.binaris.oneslib:oneslib:1.0.0')
}
```

**Option B — jar in the project's `libs/` folder (recommended while developing Ones Lib):**

Copy the **reobfuscated** jar (`OnesLib/oneslib/build/libs/oneslib-1.0.0.jar`, produced by `:oneslib:jar` /
`:oneslib:build`) into the consumer's `libs/` folder:

```gradle
repositories {
    flatDir {
        dir 'libs'
    }
}

def oneslibVersion = '1.0.0'

tasks.register('updateOnesLib', Copy) {
    from file("${rootDir}/../OnesLib/oneslib/build/libs/oneslib-${oneslibVersion}.jar")
    into file("${rootDir}/libs")
}

dependencies {
    implementation fg.deobf("blank:oneslib:${oneslibVersion}")
}
```

Update the jar after changing Ones Lib:

```bash
cd OnesLib && ./gradlew :oneslib:jar
cd ../Godzillamod && ./gradlew updateOnesLib
```

**Always required (both options):**

```gradle
dependencies {
    implementation fg.deobf("software.bernie.geckolib:geckolib-forge-1.20.1:4.4.4")
    implementation fg.deobf("dev.tocraft:walkers-forge:1.20.1-4.4.3")
    implementation fg.deobf("dev.tocraft:remorphed-forge:1.20.1-3.6.2")
    implementation fg.deobf("dev.tocraft:craftedcore-forge:1.20.1-4.2.3")

    // Dev runs only: GeckoLib embeds mclib via jarJar, but ForgeGradle does not extract it in dev.
    runtimeOnly 'com.eliotlash.mclib:mclib:20'
}

repositories {
    maven { url = 'https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/' }
    maven { url = 'https://maven.tocraft.dev/public/' }
}
```

And declare the dependency in `mods.toml`:

```toml
[[dependencies."${mod_id}"]]
modId = "oneslib"
mandatory = true
versionRange = "[1.0.0,)"
ordering = "AFTER"
side = "BOTH"
```

### 2. Register your Ones

`Ones.register` must run during **mod construction** (it registers the `EntityType`s):

```java
@Mod(MyMod.MOD_ID)
public final class MyMod {

    public MyMod(FMLJavaModLoadingContext context) {
        MyOnes.register();
    }
}
```

### 3. That's it

- No mixins needed in the consumer: the library ships its own (step height).
- Client renderers are auto-registered from the One's assets.
- If a One's assets are missing, the renderer logs an error instead of crashing; `/ones verify` reports it.

---

## Ones (morphs)

### How it works

1. **Registration** — `Ones.register("id", builder -> ...)` creates the One and auto-registers its `EntityType`.
2. **Morph** — `OnesApi.morph(player, "id")` creates the shape entity, sets it as the player's Walkers shape,
   applies attributes/effects/flight and activates passives.
3. **Rendering** — Walkers renders the shape; the library registers a GeckoLib renderer using the One's assets.
4. **Demorph** — `OnesApi.demorph(player)` clears the shape, cancels abilities, restores attributes/effects/flight.
5. **Respawn** — when a morphed player respawns (death or End return), the library re-applies the morph state
   (attributes, effects, flight) and re-activates passive abilities on the fresh entity, so passive effects do
   not disappear after death.
6. **External morphs** — if the player morphs through Remorphed/Walkers UI, the library detects the shape change
   on tick and binds abilities/attributes automatically.

### Registration

```java
Ones.register("evil_hulk", one -> one
    .entity(EvilHulkOne::new)                 // or .entityType(() -> EntityType.ZOMBIE)
    .hitbox(0.8F, 2.2F)
    .category(MobCategory.CREATURE)
    .assets("evil_hulk")                      // oneslib: namespace
    // .assets(MyMod.id("evil_hulk"))         // or your own namespace
    .attributes(attributes -> attributes
        .health(120.0F)
        .speed(0.24F)
        .damage(12.0F)
        .knockback(1.5F)
        .reach(1.2F)                          // multiplier over vanilla reach
        .stepHeight(0.6F)
        .ignoreFallDamage(true)
        .flySeconds(8)                        // -1 = infinite, 0 = no flight
        .effect(MobEffects.DAMAGE_RESISTANCE, -1, 0))
    .visual(visual -> visual
        .modelScale(1.35F)
        .guiScale(0.5F)                       // scale in inventory/morph GUIs (1.0 = auto from hitbox)
        .firstPersonHand(false)
        .showNameTag(true)
        .cameraOffset(0.0D, 0.3D, 0.0D))
    .parts(parts -> parts.add("head", 0.6F, 0.6F, 0.0F, 1.8F, 0.0F, false))
    .npc(npc -> npc.attacksPlayers(true).movesAround(true).looksAtPlayer(true).speed(0.3F))
    .stateChannel(StateChannel.ITEM_SLOT)
    .stateSlot(EquipmentSlot.FEET)
    .ability(new EvilHulkGolpe())
    .ability(new EvilHulkGrito())
    .build());                                // .build() is optional
```

- Ids must be unique `snake_case`; duplicate One ids or duplicate ability ids fail at registration with a clear error.
- Ability ids must be unique **across all Ones** (two abilities of the same One can't share a keybind either).

### Entity

```java
public class EvilHulkOne extends OneEntity {
    public EvilHulkOne(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerCustomControllers(AnimatableManager.ControllerRegistrar controllers) {
        // optional extra GeckoLib controllers
    }
}
```

If you can't extend `OneEntity` (another base or a vanilla entity), implement `OneMorph`:

```java
public class ForeignOne extends PathfinderMob implements GeoEntity, OneMorph {
    // oneOwner() is used by the state channel; oneAnimation() only needed for SYNCED_DATA
}
```

With the default `ITEM_SLOT` channel, implementing `OneMorph` requires **zero** extra methods.

### Assets

Default paths use the One's asset id:

```
assets/<namespace>/geo/entity/<asset>.geo.json
assets/<namespace>/textures/entity/<asset>.png
assets/<namespace>/animations/entity/<asset>.animation.json
```

Animation names used by the library base controller: `idle`, `walk`. Abilities reference the rest by name.

Optional base-controller animations (per One, via `.animations(...)`):

```java
.animations(animations -> animations
    .crouch("Crouch")            // played (looped) while the owner player sneaks
    .attack("Attack", 18))       // played (looped) for N ticks after the owner attacks
```

If a One does not declare them, the base controller keeps using `idle`/`walk` only.

### State channel (item-hack)

Walkers' shape is a detached entity: only its NBT snapshot is synced (every tick), and the item channel is the
proven transport. `OneState` abstracts it:

- `ITEM_SLOT` (default): an invisible token (`oneslib:state_token`) is placed in a configurable slot of the
  **player** (default `FEET`). The previous item in that slot is stored and **restored** when the ability ends
  or the player demorphs. Walkers copies the player's equipment to the shape when rendering, so the shape's
  animation controller reads it.
- `SYNCED_DATA`: uses `OneMorph.oneAnimation()` (Walkers syncs the shape NBT every tick, so this also works).
- `PACKET`: reserved, not implemented.

```java
.stateChannel(StateChannel.ITEM_SLOT)
.stateSlot(EquipmentSlot.FEET)
```

Note: `Walkers.CONFIG.shapesEquipArmor` (default `true`) must stay enabled for `ITEM_SLOT` rendering.

### Flight

The active One owns flight: an One with `flySeconds != 0` grants it, any One without flight revokes it.
On demorph, the state from **before the first morph** of the chain is restored, so no residual flight after
morph/demorph cycles. Creative/spectator are never touched.

### Querying and runtime API

```java
OnesApi.morph(player, "evil_hulk");
OnesApi.demorph(player);
OnesApi.currentOne(player);                    // Optional<One>
OnesApi.isMorphedAs(player, "evil_hulk");
OnesApi.tick(player);                          // used by the server tick and by GameTests

Ones.registry().all();                         // Collection<One>
Ones.registry().byId("evil_hulk");             // Optional<One>
Ones.currentOne(player);                       // @Nullable One
Ones.id("evil_hulk");                          // oneslib: ResourceLocation
```

---

## Ability system

### Lifecycle

1. **Activation** — keybind, item, or `OnesApi.activate`. The engine checks the One, cooldown and predicate,
   then calls `onStart`.
2. **Ticking** — every server tick while active: `onTick` plus scheduled actions (`schedule` / `scheduleEvery`).
3. **End** — duration, toggle, `context.end()`, demorph, or `OnesApi.deactivate`; calls `onEnd` and starts the
   cooldown (except on `MORPH_LOST`).

### Creating an ability

```java
public final class EvilHulkGolpe extends LifetimeAbility {

    public EvilHulkGolpe() {
        super("evil_hulk_golpe", 31, settings -> settings
            .name("Golpe Brutal")
            .cooldown(80)                          // int or IntSupplier (config)
            .keybind(GLFW.GLFW_KEY_G)
            .sound(SoundEvents.IRON_GOLEM_ATTACK)
            .keybindOnly(false)
            .cooldownMessage(true)
            .activation(player -> true));
    }

    @Override
    public boolean canActivate(AbilityContext context) {
        return true;
    }

    @Override
    public void onStart(AbilityContext context) {
        context.animate("attack3");
    }

    @Override
    public void onTick(AbilityContext context) {
        if (context.tick() == 10) {
            context.area(5.0D).damage(10.0F).knockback(1.5D).hit();
        }
    }

    @Override
    public void onEnd(AbilityContext context, EndReason reason) {
        context.stopAnimation();
    }
}
```

### Base classes

| Class | Constructor | Behavior |
|---|---|---|
| `OneAbility` | `(id, settings -> ...)` | direct/custom lifecycle |
| `InstantAbility` | `(id[, settings -> ...])` | ends the same tick, starts cooldown |
| `LifetimeAbility` | `(id, durationTicks[, settings -> ...])` | auto-ends after the duration |
| `ToggleAbility` | `(id[, settings -> ...])` | second activation ends it (`TOGGLED_OFF`) |
| `PassiveAbility` | `(id, settings -> ...)` | active while morphed; effects cleaned on demorph |
| `ThrowAbility` | `(id, lifetimeTicks, visualItem[, settings -> ...])` | manually driven projectile |
| `SummonAbility` | `(id, durationTicks, settings -> ...)` | summons owner-bound entities, discards on end |

`ThrowAbility` hooks: `createProjectile`, `initialVelocity`, `gravity`, `trailParticle`, `canHit`,
`onProjectileTick`, `onHitEntity`, `onHitBlock`.
`SummonAbility` hooks: `createSummons`, `onSummonTick`.

### Context utilities

```java
context.player() / level() / one() / ability() / shape()
context.tick() / progress() / isActive() / end()

// Animation
context.animate("attack");                          // one-shot (auto-clears after 20 ticks)
context.animate("fly", Animation.LoopType.LOOP);    // loop
context.animate("beam", Animation.LoopType.PLAY_ONCE, 40);
context.stopAnimation();

// Particles (player-centered; the shape is detached and does not move)
context.particles(ParticleTypes.FLAME, 10);
context.particles(ParticleTypes.FLAME, 10, 0.5D, 0.05D);
context.particlesAt(ParticleTypes.FLAME, pos, 10, 0.2D, 0.05D);
context.trail(ParticleTypes.FLAME, 0.3D);
context.beam(ParticleTypes.DRAGON_BREATH, 32.0D);   // eye -> impact line
context.ring(ParticleTypes.END_ROD, 2.5D);

// Feedback
context.sound(SoundEvents.GENERIC_EXPLODE);
context.sound(SoundEvents.GENERIC_EXPLODE, 2.0F, 1.0F);
context.title(Component.literal("Boss"), Component.literal("appears"));
context.actionBar(Component.literal("Charging!"));
context.effect(MobEffects.DAMAGE_BOOST, 200, 1);
context.removeEffect(MobEffects.DAMAGE_BOOST);

// Combat / targeting
context.area(5.0D).filter(entity -> entity instanceof Monster).damage(10.0F).knockback(1.5D).hit();
context.raycast(32.0D);
context.raycast(32.0D, entity -> entity instanceof Monster);
context.raycastAll(32.0D);                          // sorted by distance
context.raycastAll(32.0D, 8);                       // limited
context.raycastEnd(32.0D);                          // block impact position

// Timing
context.schedule(40, ctx -> ctx.sound(SoundEvents.WARDEN_SONIC_BOOM));   // delay in ticks
context.scheduleEvery(5, ctx -> ctx.particles(ParticleTypes.FLAME, 3));

// Misc
context.fly(true);
```

### Keybinds

- The library registers one `KeyMapping` per ability (category `key.categories.oneslib.abilities`).
- The same key is allowed on abilities of **different** Ones; resolution uses the player's current One.
- Two abilities of the **same** One sharing a key fails at registration.
- Per ability you can opt into a client HUD with `.keybindUi(KeybindUi.BOXES)` (default `NONE`): while morphed,
  the bound key is drawn inside a square with the ability name below it, four per row, above the hotbar.
  Only abilities with a real keybind (`keybind != GLFW_KEY_UNKNOWN`) are shown. The HUD reads the live binding
  (rebinds apply instantly).

### Ability items

```java
ItemRegistry.register("evil_hulk_golpe_item",
    () -> new AbilityItem("evil_hulk_golpe", new Item.Properties().stacksTo(1)));
```

### Runtime API

```java
OnesApi.activate(player, "evil_hulk_golpe");
OnesApi.activate(player, "evil_hulk_golpe", true);   // fromItem
OnesApi.deactivate(player, "evil_hulk_golpe");
OnesApi.deactivateAll(player);
OnesApi.isActive(player, "evil_hulk_golpe");
OnesApi.isOnCooldown(player, "evil_hulk_golpe");
OnesApi.cooldownLeft(player, "evil_hulk_golpe");
OnesApi.cooldown(player, "evil_hulk_golpe", 100);
OnesApi.clearCooldown(player, "evil_hulk_golpe");
```

### Events

| Event | Side | When |
|---|---|---|
| `OneMorphEvent` | Server | Player morphed (`player`, `one`) |
| `OneDemorphEvent` | Server | Player demorphed (`player`, `previousOne`) |

---

## Commands

- `/ones morph <one>` (permission level 2)
- `/ones demorph`
- `/ones list`
- `/ones verify` — reports missing assets, null entity types, PACKET channel usage

`OnesVerification.verify()` is also usable from code (it backs the command).

---

## Testing

```bash
./gradlew :oneslib:test                 # unit tests (JUnit 5) + GeckoLib asset parsing
./gradlew :testmod:runGameTestServer    # GameTests (17)
./gradlew :testmod:runClient -Pclienttest="WorldName"   # client self-test in that world
```

- The `testmod` subproject is the reference implementation: real assets from past projects
  (Tails, Evil Hulk, Siren Head, Baby Dragon, Giant Ogre, Imp) and GameTests for morph/demorph,
  attributes, passives, cooldowns, flight, state channel, projectiles, external entities and targeting.
- `OnesApi.tick(player)` makes abilities deterministic in tests (no waiting for the server loop).
- The client self-test (`ClientSelfTest`) morphs each One, activates abilities and verifies that the
  animation reaches the client, that the previous item is restored and that flight is revoked.

---

## Configuration conventions

Everything tweakable must live in a config file, never hardcoded. Ones Lib has no config system of its own:
use Forge `ModConfigSpec` in the consumer mod.

### What can be config-driven

- **Runtime values** (damage, radii, particle counts, effect durations, messages): read the config directly
  inside the ability, so it hot-reloads.
- **Cooldowns and durations**: `AbilitySettings` accepts `IntSupplier`, so a config value can be passed at
  registration and resolved at runtime.
- **Morph attributes**: configs load after mod construction, so `OneBuilder` values are baked at registration.
  Keep sane defaults in code and, if a project needs them configurable, adjust them from a `OneMorphEvent`
  listener.

```java
public final class EvilHulkGolpe extends LifetimeAbility {

    public EvilHulkGolpe() {
        super("evil_hulk_golpe", 31, settings -> settings
            .cooldown(MyConfig.GOLPE_COOLDOWN)     // IntSupplier
            .duration(MyConfig.GOLPE_DURATION));   // IntSupplier
    }

    @Override
    public void onTick(AbilityContext context) {
        if (context.tick() == MyConfig.GOLPE_HIT_TICK.get()) {
            context.area(MyConfig.GOLPE_RADIUS.get())
                .damage(MyConfig.GOLPE_DAMAGE.get().floatValue())
                .hit();
        }
    }
}
```

```java
@SubscribeEvent
public static void onMorph(OneMorphEvent event) {
    if (!event.one().id().equals("evil_hulk")) {
        return;
    }
    // adjust attributes from config here (defaults are already applied by the library)
}
```

### Rules

- No magic numbers in code: every tweakable value reads from config with a default equal to the planned value.
- Every entry gets a comment explaining what it does, range, unit (ticks vs seconds) and what it affects.
- Organize by categories per system: `abilities`, `morphs`, `entities`, `effects`, ...
- Gameplay values go in a server/common config; purely visual/client things (particles, render distance) in
  the client config.
- Support reload where possible; if a value can't hot-reload, say so in its comment and note it in QA.

---

## Quick reference

**Package root:** `com.binaris.oneslib`

| Utility | Package | Use case |
|---|---|---|
| `Ones` | root | One registration + registry/current One queries |
| `OneBuilder` / `OneData` | `api` | One definition (attributes, visual, parts, NPC, channel) |
| `OneEntity` / `OneMorph` | `common.entity` / `api` | Base entity / interface for external bases |
| `OneState` | `common.state` | Animation state channel (item-hack) |
| `OnesApi` | `api` | Morph/demorph + ability runtime |
| `OneAbility` + `impl/*` | `api` / `api.impl` | Ability lifecycle and base classes |
| `AbilitySettings` | `api` | Cooldown, duration, keybind, sound, predicate |
| `AbilityContext` | `api` | Animations, particles, targeting, timing, flight |
| `AbilityItem` | `api.item` | Item that activates an ability |
| `OneMorphEvent` / `OneDemorphEvent` | `api.event` | Morph lifecycle hooks |
| `OnesVerification` | `common` | Registry/asset diagnostics (`/ones verify`) |
| `Particles` / `Sounds` / `Titles` / `Targeting` / `Area` | `common.util` | Low-level effect utilities |

**Server manager:** `ServerOneManager` (`server.morph`) and `AbilityEngine` (`server.ability`) hold the runtime
state; use `OnesApi` instead of calling them directly.
