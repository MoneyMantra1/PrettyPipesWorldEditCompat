package com.moneymantra.ppwecompat;

import com.mojang.logging.LogUtils;
import com.sk89q.worldedit.WorldEdit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PpweCompatMod.MOD_ID)
public final class PpweCompatMod {
    public static final String MOD_ID = "ppwecompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PpweCompatMod(IEventBus modBus) {
        WorldEdit.getInstance().getEventBus().register(new WorldEditHooks());
        NeoForge.EVENT_BUS.addListener(PrettyPipeEditTracker::onServerTick);
        LOGGER.info("Pretty Pipes WorldEdit Compat loaded");
    }
}
