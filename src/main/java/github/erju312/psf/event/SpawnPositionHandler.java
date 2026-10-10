package github.erju312.psf.event;

import github.erju312.psf.PlayerSpawnFix;
import github.erju312.psf.config.SpawnFixConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = PlayerSpawnFix.MOD_ID)
public final class SpawnPositionHandler {
    private static final ThreadLocal<Boolean> CUSTOM_SPAWN_PENDING = ThreadLocal.withInitial(() -> false);

    private SpawnPositionHandler() {
    }

    @SubscribeEvent
    public static void onCreateSpawnPosition(LevelEvent.CreateSpawnPosition event) {
        LevelAccessor accessor = event.getLevel();
        if (!SpawnFixConfig.SPAWN_FIX_ENABLED.get()
            || !(accessor instanceof ServerLevel level)
            || level.dimension() != ServerLevel.OVERWORLD) {
            return;
        }

        ChunkPos vanillaSpawnChunk = new ChunkPos(level.getChunkSource().randomState().sampler().findSpawnPosition());
        BlockPos vanillaCandidate = vanillaSpawnChunk.getWorldPosition().offset(8, 0, 8);
        event.getSettings().setSpawn(vanillaCandidate.atY(level.getSeaLevel()), 0.0F);
        BlockPos safeSpawn = findSafeSpawn(level, vanillaCandidate);
        if (safeSpawn == null) {
            PlayerSpawnFix.LOGGER.warn(
                "Could not find a safe initial spawn within {} blocks; vanilla spawn selection will be used",
                SpawnFixConfig.MAX_SEARCH_RADIUS.get()
            );
            return;
        }

        event.getSettings().setSpawn(safeSpawn, 0.0F);
        CUSTOM_SPAWN_PENDING.set(true);
        event.setCanceled(true);
        PlayerSpawnFix.LOGGER.info(
            "Set initial spawn to {}; nearby non-village structure starts will be blocked within {} blocks",
            safeSpawn,
            SpawnFixConfig.STRUCTURE_CLEARANCE.get()
        );
    }

    public static boolean consumeCustomSpawnPending() {
        boolean pending = CUSTOM_SPAWN_PENDING.get();
        CUSTOM_SPAWN_PENDING.remove();
        return pending;
    }

    static BlockPos findSafeSpawn(ServerLevel level, BlockPos center) {
        int maxRadius = SpawnFixConfig.MAX_SEARCH_RADIUS.get();
        int sampleStep = SpawnFixConfig.SEARCH_STEP.get();
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();

        for (int radius = 0; radius <= maxRadius; radius += sampleStep) {
            if (radius == 0) {
                BlockPos candidate = surfacePosition(level, center.getX(), center.getZ(), sample);
                if (isSafe(level, candidate)) {
                    return candidate;
                }
                continue;
            }

            for (int offset = -radius; offset <= radius; offset += sampleStep) {
                BlockPos candidate = surfacePosition(level, center.getX() + offset, center.getZ() - radius, sample);
                if (isSafe(level, candidate)) return candidate;

                candidate = surfacePosition(level, center.getX() + offset, center.getZ() + radius, sample);
                if (isSafe(level, candidate)) return candidate;
            }
            for (int offset = -radius + sampleStep; offset < radius; offset += sampleStep) {
                BlockPos candidate = surfacePosition(level, center.getX() - radius, center.getZ() + offset, sample);
                if (isSafe(level, candidate)) return candidate;

                candidate = surfacePosition(level, center.getX() + radius, center.getZ() + offset, sample);
                if (isSafe(level, candidate)) return candidate;
            }
        }
        return null;
    }

    private static BlockPos surfacePosition(ServerLevel level, int x, int z, BlockPos.MutableBlockPos sample) {
        // Make the candidate visible before generating its chunk. Structure-start interception
        // therefore protects the location even during initial spawn discovery.
        level.setDefaultSpawnPos(new BlockPos(x, level.getSeaLevel(), z), 0.0F);
        sample.set(x, 0, z);
        Heightmap.Types heightmap = SpawnFixConfig.AVOID_LEAVES.get()
            ? Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
            : Heightmap.Types.MOTION_BLOCKING;
        level.getChunk(x >> 4, z >> 4);
        return level.getHeightmapPos(heightmap, sample).immutable();
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet) {
        int requiredHeadroom = SpawnFixConfig.REQUIRED_HEADROOM.get();
        if (feet.getY() <= level.getMinBuildHeight() || feet.getY() + requiredHeadroom - 1 >= level.getMaxBuildHeight()) {
            return false;
        }

        BlockState ground = level.getBlockState(feet.below());
        if ((SpawnFixConfig.AVOID_LOGS.get() && ground.is(BlockTags.LOGS))
            || (SpawnFixConfig.AVOID_LEAVES.get() && ground.is(BlockTags.LEAVES))
            || (SpawnFixConfig.REQUIRE_STURDY_GROUND.get()
                && !ground.isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP))
            || isConfiguredDangerous(level, ground)) {
            return false;
        }

        for (int offsetY = 0; offsetY < requiredHeadroom; offsetY++) {
            BlockPos bodyPos = feet.above(offsetY);
            BlockState bodyState = level.getBlockState(bodyPos);
            if (!bodyState.getCollisionShape(level, bodyPos).isEmpty()
                || (SpawnFixConfig.AVOID_FLUIDS.get() && !level.getFluidState(bodyPos).isEmpty())
                || isConfiguredDangerous(level, bodyState)) {
                return false;
            }
        }

        if (!SpawnFixConfig.AVOID_HARSH_BIOMES.get()) {
            return true;
        }

        String biomeId = level.registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getKey(level.getBiome(feet).value())
            .toString();
        return !SpawnFixConfig.HARSH_BIOMES.get().contains(biomeId);
    }

    private static boolean isConfiguredDangerous(ServerLevel level, BlockState state) {
        if (!SpawnFixConfig.AVOID_DANGEROUS_BLOCKS.get()) {
            return false;
        }
        String blockId = level.registryAccess()
            .registryOrThrow(Registries.BLOCK)
            .getKey(state.getBlock())
            .toString();
        return SpawnFixConfig.DANGEROUS_BLOCKS.get().contains(blockId);
    }

}
