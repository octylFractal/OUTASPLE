package net.octyl.mc.outasple;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod initializer for OutaSple.
 *
 * <p>
 * When this baby hits 88 ticks per second, you're gonna see some serious shit.
 * </p>
 */
public class OutaSple implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("outasple");

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (ServerState.getFor(server.overworld()).isSkipTickDelay()) {
                // Undo added delay
                server.nextTickTime -= 50L;
            }
        });

        LOGGER.info("[OUTASPLE] We're going back... TO THE BED.");
    }
}
