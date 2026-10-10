package github.erju312.psf.mixin;

import github.erju312.psf.PlayerSpawnFix;
import github.erju312.psf.config.SpawnFixConfig;
import github.erju312.psf.worldgen.SpawnStructureProtection;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Redirect(
        method = "tryGenerateStructure",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/StructureManager;setStartForStructure(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;Lnet/minecraft/world/level/chunk/StructureAccess;)V"
        )
    )
    private void psf$blockStructureNearSpawn(
        StructureManager manager,
        SectionPos sectionPos,
        Structure structure,
        StructureStart start,
        StructureAccess access
    ) {
        if (SpawnStructureProtection.shouldBlock(manager, structure, start)) {
            if (SpawnFixConfig.LOG_BLOCKED_STRUCTURES.get()) {
                PlayerSpawnFix.LOGGER.debug(
                    "Blocked structure {} at {} because its boundary is too close to world spawn",
                    manager.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(structure),
                    start.getChunkPos()
                );
            }
            return;
        }
        manager.setStartForStructure(sectionPos, structure, start, access);
    }
}
