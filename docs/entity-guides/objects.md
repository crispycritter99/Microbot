# Object Gotchas

## 1. Ask a tile object for an action it actually has

`Rs2TileObjectModel.click(action)` and `query().interact(action)` refuse an action the object does not have: they return `false` and send nothing. `click()` (or a null or blank action) sends the first option, and with an item or spell selected any action uses it on the object. The deprecated `Rs2GameObject.interact` / `clickObject` still sends the first option for a missing action and logs a warning, so code that worked there can stop working when migrated.

**Why this matters:** both click paths used to send the first option whenever the action was missing, and still returned `true`. Asking a ground-floor Lumbridge staircase for `Bottom-floor` sent `Climb-up`. Dozens of callers depend on that by accident: walker transport rows say `Jump to` where the object has `Jump-to`; bank chests offer `Use`, not `Bank`; the Moons of Peril supply crate offers `Take-from`, not `Take from`. A clue step that tried `Search` then `Investigate` never got past `Search`, because the wrong option "succeeded".

**Pattern to follow:**

```java
// Wrong: the action is a guess, and a bank chest only offers Use / Collect
bank.click("Bank");

// Right: resolve the action from the object, then click it
String action = bank.hasAction("Bank") ? "Bank" : "Use";
bank.click(action);
```

Check names against the game cache (or `observe --nearby` in the CLI) before hardcoding them. Case and colour tags don't matter, hyphens and spaces do.

**Where this applies:** `Rs2TileObjectModel.click`, every `Rs2TileObjectQueryable.interact` overload, and any migration from `Rs2GameObject` to the query API (Rs2GameObject retirement M3/M4).

**Defensive check:** `Rs2TileObjectModelClickActionTest` covers refusal, first option, colour tags, impostors and the selected-widget path.

## 2. Preserve submenu parent slots and child indices when migrating object clicks

Use `Rs2TileObjectModel.click(parentAction, childAction)` for object submenus. Resolve the parent from the visible composition's action slots, even when its index exceeds `EntityOps.getNumOps()`, then inspect `getSubOps(parentIndex)`. Preserve child index zero and encode `(objectId & 0xFFFF) | (childIndex << 16)` with the parent's menu opcode. A missing parent or child must return false without clicking the first option.

**Why this matters:** fairy-ring Favourites can occupy slot 3 while the live parent reports only three ops. Losing that slot or falling back to a normal click selects the wrong action during transport migration.

**Pattern to follow:** `ring.click("Favourites", destinationCode)`; if it returns false, open Configure instead.

**Where this applies:** `Rs2TileObjectModel`, legacy `Rs2GameObject.clickObject` wrappers and fairy-ring transports.

**Defensive check:** `ObjectSubmenuActionTest` covers index encoding; `Rs2TileObjectModelClickActionTest` checks dispatch, worldview, selected widgets and refusal.
