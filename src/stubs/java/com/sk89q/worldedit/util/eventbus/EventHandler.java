package com.sk89q.worldedit.util.eventbus;

public abstract class EventHandler implements Comparable<EventHandler> {
    public enum Priority {
        VERY_EARLY,
        EARLY,
        NORMAL,
        LATE,
        VERY_LATE
    }
}
