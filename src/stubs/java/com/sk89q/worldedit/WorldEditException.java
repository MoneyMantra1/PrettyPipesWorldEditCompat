package com.sk89q.worldedit;

public class WorldEditException extends Exception {
    public WorldEditException() {}
    public WorldEditException(String message) { super(message); }
    public WorldEditException(String message, Throwable cause) { super(message, cause); }
}
