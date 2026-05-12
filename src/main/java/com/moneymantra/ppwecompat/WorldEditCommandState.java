package com.moneymantra.ppwecompat;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.LocalSession;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class WorldEditCommandState {
    private static final long PASTE_CONTEXT_TTL_MILLIS = 10_000L;
    private static final Map<UUID, PasteContext> PASTE_CONTEXTS = new ConcurrentHashMap<>();

    private WorldEditCommandState() {
    }

    static void observeCommand(Actor actor, String rawArguments) {
        if (actor == null || rawArguments == null) {
            return;
        }

        UUID actorId = actor.getUniqueId();
        if (actorId == null) {
            return;
        }

        String normalized = rawArguments.trim().toLowerCase(Locale.ROOT);
        if (!isPasteCommand(normalized)) {
            PASTE_CONTEXTS.remove(actorId);
            return;
        }

        Rotation rotation = Rotation.NONE;
        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
            ClipboardHolder holder = session.getClipboard();
            rotation = Rotation.fromClipboardTransform(holder.getTransform());
        } catch (Throwable t) {
            PpweCompatMod.LOGGER.debug("Could not read WorldEdit clipboard transform for {}", actor.getName(), t);
        }

        PASTE_CONTEXTS.put(actorId, new PasteContext(rotation, System.currentTimeMillis()));
    }

    static PasteContext getPasteContext(Actor actor) {
        if (actor == null) {
            return PasteContext.notPaste();
        }
        return getPasteContext(actor.getUniqueId());
    }

    static PasteContext getPasteContext(UUID actorId) {
        if (actorId == null) {
            return PasteContext.notPaste();
        }
        PasteContext context = PASTE_CONTEXTS.get(actorId);
        if (context == null) {
            return PasteContext.notPaste();
        }
        if (System.currentTimeMillis() - context.createdAtMillis() > PASTE_CONTEXT_TTL_MILLIS) {
            PASTE_CONTEXTS.remove(actorId, context);
            return PasteContext.notPaste();
        }
        return context;
    }

    private static boolean isPasteCommand(String normalized) {
        // CommandEvent arguments include the leading slash, for example "//paste -a".
        return normalized.equals("//paste")
            || normalized.startsWith("//paste ")
            || normalized.equals("/paste")
            || normalized.startsWith("/paste ");
    }

    record PasteContext(boolean paste, Rotation rotation, long createdAtMillis) {
        static PasteContext notPaste() {
            return new PasteContext(false, Rotation.NONE, 0L);
        }

        PasteContext(Rotation rotation, long createdAtMillis) {
            this(true, rotation, createdAtMillis);
        }
    }
}
