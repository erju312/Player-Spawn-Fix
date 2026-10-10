package github.erju312.psf.mixin;

import github.erju312.psf.config.SpawnFixConfig;
import github.erju312.psf.event.SpawnPositionHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "setInitialSpawn", at = @At("RETURN"))
    private static void psf$restoreBonusChest(
        ServerLevel level,
        ServerLevelData levelData,
        boolean generateBonusChest,
        boolean debug,
        CallbackInfo callbackInfo
    ) {
        if (!SpawnPositionHandler.consumeCustomSpawnPending()
            || !generateBonusChest
            || !SpawnFixConfig.PRESERVE_BONUS_CHEST.get()) {
            return;
        }

        level.registryAccess()
            .registry(Registries.CONFIGURED_FEATURE)
            .flatMap(registry -> registry.getHolder(MiscOverworldFeatures.BONUS_CHEST))
            .ifPresent(holder -> holder.value().place(
                level,
                level.getChunkSource().getGenerator(),
                level.random,
                levelData.getSpawnPos()
            ));
    }
}
