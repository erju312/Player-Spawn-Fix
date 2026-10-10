Player Spawn Fix improves the initial Overworld spawn and keeps unwanted structures away from the spawn area.

## Features

When a world creates its initial Overworld spawn, the mod searches nearby terrain for a safer position. It can reject positions that have:

- Leaves or logs under the player
- Unstable ground
- Insufficient headroom
- Fluids in the player space
- Configured dangerous blocks
- Configured harsh biomes

If no suitable position is found within the search area, vanilla spawn selection continues normally.

This feature only changes the initial shared world spawn. It does not choose a separate spawn for every new player and does not affect beds, respawn anchors, or normal respawning after death.

The mod checks the actual bounding box of a structure when its start is generated. If the structure boundary is too close to the world spawn, the structure start is cancelled directly.

- The world spawn is not moved
- Blocked structures are not relocated
- Villages can be allowed inside the protected area
- Additional structures can be added to an allowlist
- Structure generation outside the protected area remains unchanged
- Existing structures are not removed
- Protection only applies to the Overworld

The configured clearance can optionally include the vanilla `spawnRadius` game rule, leaving room for vanilla's randomized player-spawn offset. Biome filtering can be enabled or disabled independently, and the list of avoided biomes is fully configurable. The vanilla bonus chest can still generate at the selected spawn when the world option is enabled.

## Configuration

The configuration file is `config/psf-common.toml`.

Spawn selection options:

- Enable or disable custom spawn selection
- Change the maximum search radius
- Change the distance between tested positions
- Allow or reject logs and leaves
- Require sturdy ground
- Set the required headroom
- Allow or reject fluids
- Enable or disable dangerous-block checks
- Edit the dangerous-block list
- Enable or disable harsh-biome checks
- Edit the harsh-biome list

Structure protection options:

- Enable or disable structure protection
- Change the minimum distance from the spawn to a structure boundary
- Include or ignore the vanilla `spawnRadius`
- Allow or block villages
- Add individual structures to the allowlist
- Enable debug logging for blocked structure starts

Compatibility options:

- Enable or disable bonus chest preservation
