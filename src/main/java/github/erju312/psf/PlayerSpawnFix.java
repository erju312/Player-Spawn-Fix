package github.erju312.psf;

import com.mojang.logging.LogUtils;
import github.erju312.psf.config.SpawnFixConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(PlayerSpawnFix.MOD_ID)
public final class PlayerSpawnFix {
    public static final String MOD_ID = "psf";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PlayerSpawnFix(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SpawnFixConfig.SPEC);
    }
}
