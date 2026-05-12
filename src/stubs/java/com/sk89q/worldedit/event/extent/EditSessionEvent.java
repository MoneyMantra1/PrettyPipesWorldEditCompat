package com.sk89q.worldedit.event.extent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.World;

public class EditSessionEvent {
    public EditSession.Stage getStage() { throw new AssertionError("stub"); }
    public World getWorld() { throw new AssertionError("stub"); }
    public Actor getActor() { throw new AssertionError("stub"); }
    public Extent getExtent() { throw new AssertionError("stub"); }
    public void setExtent(Extent extent) { throw new AssertionError("stub"); }
}
