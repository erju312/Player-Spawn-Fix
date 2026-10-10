package github.erju312.psf.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class SpawnFixConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SPAWN_FIX_ENABLED;
    public static final ModConfigSpec.IntValue MAX_SEARCH_RADIUS;
    public static final ModConfigSpec.IntValue SEARCH_STEP;
    public static final ModConfigSpec.BooleanValue AVOID_LOGS;
    public static final ModConfigSpec.BooleanValue AVOID_LEAVES;
    public static final ModConfigSpec.BooleanValue REQUIRE_STURDY_GROUND;
    public static final ModConfigSpec.IntValue REQUIRED_HEADROOM;
    public static final ModConfigSpec.BooleanValue AVOID_FLUIDS;
    public static final ModConfigSpec.BooleanValue AVOID_DANGEROUS_BLOCKS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DANGEROUS_BLOCKS;
    public static final ModConfigSpec.BooleanValue AVOID_HARSH_BIOMES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HARSH_BIOMES;

    public static final ModConfigSpec.BooleanValue STRUCTURE_PROTECTION_ENABLED;
    public static final ModConfigSpec.IntValue STRUCTURE_CLEARANCE;
    public static final ModConfigSpec.BooleanValue INCLUDE_SPAWN_RADIUS;
    public static final ModConfigSpec.BooleanValue ALLOW_VILLAGES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_STRUCTURES;
    public static final ModConfigSpec.BooleanValue LOG_BLOCKED_STRUCTURES;

    public static final ModConfigSpec.BooleanValue PRESERVE_BONUS_CHEST;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Initial world-spawn selection settings.").push("spawn");

        SPAWN_FIX_ENABLED = BUILDER
            .comment("Master switch for custom initial spawn selection. Structure protection has its own switch.")
            .define("enabled", true);

        MAX_SEARCH_RADIUS = BUILDER
            .comment("Maximum distance in blocks searched around the vanilla spawn candidate.")
            .defineInRange("maxSearchRadius", 4096, 0, 16384);

        SEARCH_STEP = BUILDER
            .comment("Horizontal spacing in blocks between tested spawn candidates. Smaller values are more thorough but slower.")
            .defineInRange("searchStep", 16, 1, 64);

        AVOID_LOGS = BUILDER
            .comment("Reject a spawn when the supporting block is in the Minecraft logs tag.")
            .define("avoidLogs", true);

        AVOID_LEAVES = BUILDER
            .comment("Ignore leaf canopies when finding the surface and reject leaf support blocks.")
            .define("avoidLeaves", true);

        REQUIRE_STURDY_GROUND = BUILDER
            .comment("Require the supporting block to have a sturdy upward face.")
            .define("requireSturdyGround", true);

        REQUIRED_HEADROOM = BUILDER
            .comment("Number of collision-free blocks required from the player's feet upward.")
            .defineInRange("requiredHeadroom", 2, 1, 4);

        AVOID_FLUIDS = BUILDER
            .comment("Reject candidates containing fluid in the required headroom.")
            .define("avoidFluids", true);

        AVOID_DANGEROUS_BLOCKS = BUILDER
            .comment("Reject candidates touching a block listed in dangerousBlocks.")
            .define("avoidDangerousBlocks", true);

        DANGEROUS_BLOCKS = BUILDER
            .comment("Block IDs considered unsafe below or inside the player spawn space.")
            .defineListAllowEmpty("dangerousBlocks", List.of(
                "minecraft:cactus",
                "minecraft:magma_block",
                "minecraft:campfire",
                "minecraft:soul_campfire",
                "minecraft:fire",
                "minecraft:soul_fire",
                "minecraft:sweet_berry_bush",
                "minecraft:wither_rose",
                "minecraft:powder_snow",
                "minecraft:pointed_dripstone"
            ), () -> "minecraft:magma_block", SpawnFixConfig::isValidResourceLocation);

        AVOID_HARSH_BIOMES = BUILDER
            .comment("Reject biomes listed in harshBiomes.")
            .define("avoidHarshBiomes", true);

        HARSH_BIOMES = BUILDER
            .comment("Biome IDs that cannot be selected for the initial world spawn when avoidHarshBiomes is enabled.")
            .defineListAllowEmpty("harshBiomes", List.of(
                "minecraft:desert",
                "minecraft:badlands",
                "minecraft:eroded_badlands",
                "minecraft:wooded_badlands",
                "minecraft:snowy_plains",
                "minecraft:ice_spikes",
                "minecraft:frozen_ocean",
                "minecraft:deep_frozen_ocean"
            ), () -> "minecraft:desert", SpawnFixConfig::isValidResourceLocation);

        BUILDER.pop();
        BUILDER.comment("World-spawn structure protection settings.").push("structures");

        STRUCTURE_PROTECTION_ENABLED = BUILDER
            .comment("Master switch for blocking structure starts near world spawn.")
            .define("enabled", true);

        STRUCTURE_CLEARANCE = BUILDER
            .comment("Required horizontal distance in blocks from a possible player spawn to a structure boundary.")
            .defineInRange("clearance", 45, 0, 512);

        INCLUDE_SPAWN_RADIUS = BUILDER
            .comment("Add the vanilla spawnRadius gamerule to clearance so randomized player spawns retain the configured distance.")
            .define("includeVanillaSpawnRadius", true);

        ALLOW_VILLAGES = BUILDER
            .comment("Allow structures in the Minecraft village tag inside the protected area.")
            .define("allowVillages", true);

        ALLOWED_STRUCTURES = BUILDER
            .comment("Additional structure IDs allowed inside the protected area.")
            .defineListAllowEmpty("allowedStructures", List.of(), () -> "minecraft:village_plains", SpawnFixConfig::isValidResourceLocation);

        LOG_BLOCKED_STRUCTURES = BUILDER
            .comment("Write a debug log entry whenever a structure start is blocked.")
            .define("logBlockedStructures", false);

        BUILDER.pop();
        BUILDER.comment("Compatibility behaviour.").push("compatibility");

        PRESERVE_BONUS_CHEST = BUILDER
            .comment("Generate the vanilla bonus chest at the selected spawn when the world option enables it.")
            .define("preserveBonusChest", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private SpawnFixConfig() {
    }

    private static boolean isValidResourceLocation(Object value) {
        return value instanceof String id && id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+");
    }
}
