# Terraria Source Audit

This note maps the Terraria 1.4.4.9 source files on disk to the systems already present in this mod so later migrations stay grounded in the original logic instead of drifting into ad hoc behavior.

## Source Entry Points

- Housing flood fill and room validation:
  `C:\Users\Administrator\Desktop\terraria-1449-source-code-main\Terraria\WorldGen.cs:2595-2724`
- Furniture requirement checks for housing:
  `C:\Users\Administrator\Desktop\terraria-1449-source-code-main\Terraria\WorldGen.cs:2180-2224`
- Town NPC spawn prioritization and relocation:
  `C:\Users\Administrator\Desktop\terraria-1449-source-code-main\Terraria\WorldGen.cs:1707-2126`
- Town NPC home assignment, homeless state and house syncing:
  `C:\Users\Administrator\Desktop\terraria-1449-source-code-main\Terraria\NPC.cs`
  Key fields and methods:
  `homeless`, `homeTileX`, `homeTileY`, `UpdateHomeTileState(...)`, `QuickFindHome(...)`
- Boss spawn entry and progression flags:
  `C:\Users\Administrator\Desktop\terraria-1449-source-code-main\Terraria\NPC.cs`
  Key fields and methods:
  `downedBoss1`, `downedBoss2`, `downedBoss3`, `SpawnOnPlayer(...)`, `NewNPC(...)`
- Corruption / Crimson selection and world conversion:
  `C:\Users\Administrator\Desktop\terraria-1449-source-code-main\Terraria\WorldGen.cs`
  Key fields and methods:
  `crimson`, `Main.hardMode`, `Convert(...)`, `GetBiomeInfluence(...)`

## What The Current Mod Already Has

- Town NPC identity, names and spawn rules:
  `src/main/java/bhw/voident/xyz/terrariafabric/npc/definition/`
- Town NPC persistence and room ownership:
  `src/main/java/bhw/voident/xyz/terrariafabric/npc/state/NpcWorldState.java`
  `src/main/java/bhw/voident/xyz/terrariafabric/npc/home/HousingRegistry.java`
- Room detection and auto assignment:
  `src/main/java/bhw/voident/xyz/terrariafabric/npc/home/HouseDetector.java`
  `src/main/java/bhw/voident/xyz/terrariafabric/npc/spawn/NpcResidenceManager.java`
- No real world infection framework yet.
- No boss progression framework yet.

## Key Gaps Against Terraria

- `HouseDetector` already uses BFS, but Terraria also enforces:
  start tile not solid
  world-edge exclusion
  hard room size cap
  hole-in-wall detection based on nearby wall/solid coverage
  furniture categories driven by tile sets rather than string matching
- Town NPC unlock logic in Terraria is centralized in `CheckSpecialTownNPCSpawningConditions(...)`.
  The current mod has per-NPC Java overrides, but not a reusable progression condition layer yet.
- Terraria treats world evil choice, hardmode and boss kills as world state.
  The current mod had no dedicated saved state for those systems before this audit.

## Grounded Migration Order

1. Town NPC framework
   Reason:
   the mod already has Guide, Merchant, housing registry and NPC persistence, so this is the shortest path from current code to Terraria-like behavior.
2. World evil and infection framework
   Reason:
   corruption/crimson spread depends on a chosen evil type, hardmode state and conversion rules, which should live in saved world data first.
3. Boss framework
   Reason:
   bosses need persistent progression flags, summon rules and later unlock hooks for NPCs and world changes.

## Foundation Added In This Turn

- `src/main/java/bhw/voident/xyz/terrariafabric/world/evil/EvilBiomeType.java`
- `src/main/java/bhw/voident/xyz/terrariafabric/world/evil/WorldEvilState.java`
- `src/main/java/bhw/voident/xyz/terrariafabric/boss/state/BossProgressionState.java`

These files do not implement spread or bosses yet. They establish the persistent world state that Terraria uses implicitly through `WorldGen.crimson`, `Main.hardMode` and `NPC.downedBoss*`.
