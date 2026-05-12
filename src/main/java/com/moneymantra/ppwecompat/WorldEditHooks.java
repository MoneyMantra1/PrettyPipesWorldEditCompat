package com.moneymantra.ppwecompat;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.neoforge.NeoForgeWorld;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;

public final class WorldEditHooks {
    @Subscribe(priority = EventHandler.Priority.VERY_EARLY)
    public void onWorldEditCommand(CommandEvent event) {
        WorldEditCommandState.observeCommand(event.getActor(), event.getArguments());
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        if (event.getStage() != EditSession.Stage.BEFORE_CHANGE) {
            return;
        }
        if (!(event.getWorld() instanceof NeoForgeWorld neoForgeWorld)) {
            return;
        }
        if (event.getActor() == null) {
            return;
        }

        WorldEditCommandState.PasteContext pasteContext = WorldEditCommandState.getPasteContext(event.getActor());
        event.setExtent(new TrackingExtent(event.getExtent(), neoForgeWorld.getWorld(), event.getActor(), pasteContext));
    }
}
