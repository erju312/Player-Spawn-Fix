package github.erju312.psf.worldgen;

import github.erju312.psf.config.SpawnFixConfig;
import github.erju312.psf.mixin.accessor.StructureManagerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class SpawnStructureProtection {
    private SpawnStructureProtection() {
    }

    public static boolean shouldBlock(StructureManager manager, Structure structure, StructureStart start) {
        if (!SpawnFixConfig.STRUCTURE_PROTECTION_ENABLED.get()) {
            return false;
        }

        ServerLevel level = serverLevel(manager);
        if (level == null || level.dimension() != ServerLevel.OVERWORLD) {
            return false;
        }

        Registry<Structure> registry = manager.registryAccess().registryOrThrow(Registries.STRUCTURE);
        String structureId = registry.getKey(structure).toString();
        if (SpawnFixConfig.ALLOWED_STRUCTURES.get().contains(structureId)) {
            return false;
        }

        boolean isVillage = registry.getResourceKey(structure)
            .flatMap(registry::getHolder)
            .map(holder -> holder.is(StructureTags.VILLAGE))
            .orElse(false);
        if (SpawnFixConfig.ALLOW_VILLAGES.get() && isVillage) {
            return false;
        }

        int configuredClearance = SpawnFixConfig.STRUCTURE_CLEARANCE.get();
        int randomSpawnRadius = SpawnFixConfig.INCLUDE_SPAWN_RADIUS.get()
            ? Math.max(0, level.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS))
            : 0;
        long requiredDistance = (long) configuredClearance + randomSpawnRadius;
        if (requiredDistance <= 0) {
            return false;
        }

        BlockPos spawn = level.getSharedSpawnPos();
        BoundingBox box = start.getBoundingBox();
        long dx = axisDistance(spawn.getX(), box.minX(), box.maxX());
        long dz = axisDistance(spawn.getZ(), box.minZ(), box.maxZ());
        return dx * dx + dz * dz <= requiredDistance * requiredDistance;
    }

    private static ServerLevel serverLevel(StructureManager manager) {
        LevelAccessor accessor = ((StructureManagerAccessor) manager).psf$getLevel();
        if (accessor instanceof ServerLevel level) {
            return level;
        }
        if (accessor instanceof WorldGenRegion region) {
            return region.getLevel();
        }
        return null;
    }

    private static long axisDistance(int coordinate, int min, int max) {
        if (coordinate < min) {
            return (long) min - coordinate;
        }
        if (coordinate > max) {
            return (long) coordinate - max;
        }
        return 0L;
    }
}
