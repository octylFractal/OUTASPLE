package net.octyl.mc.outasple;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public final class ServerState extends SavedData {

    public static ServerState getFor(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            ServerState::fromNbt, ServerState::new, "outasple.ServerState"
        );
    }

    private static final String SKIP_TICK_DELAY = "skipTickDelay";

    private static ServerState fromNbt(CompoundTag tag) {
        ServerState state = new ServerState();
        state.skipTickDelay = tag.getBoolean(SKIP_TICK_DELAY);
        return state;
    }

    private boolean skipTickDelay = false;

    public void setSkipTickDelay(boolean skipTickDelay) {
        this.skipTickDelay = skipTickDelay;
    }

    public boolean isSkipTickDelay() {
        return skipTickDelay;
    }

    @Override
    @NotNull
    public CompoundTag save(CompoundTag compoundTag) {
        // Save nothing, this is all transient state
        return compoundTag;
    }
}
