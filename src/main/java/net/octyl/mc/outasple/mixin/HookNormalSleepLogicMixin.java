package net.octyl.mc.outasple.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.octyl.mc.outasple.ServerState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Supplier;

/**
 * Responsible for replacing the normal sleep skip logic.
 */
@Mixin(ServerLevel.class)
public abstract class HookNormalSleepLogicMixin extends Level {
    @Shadow
    @NotNull
    public abstract MinecraftServer getServer();

    @Shadow
    protected abstract void wakeUpAllPlayers();

    @Shadow
    @NotNull
    public abstract List<ServerPlayer> players();

    @Shadow
    protected abstract void resetWeatherCycle();

    protected HookNormalSleepLogicMixin(
        WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess,
        Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide,
        boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates
    ) {
        super(
            levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug,
            biomeZoomSeed, maxChainedNeighborUpdates
        );
    }

    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"
        )
    )
    private boolean onCheckForNightSkip(
        SleepStatus instance, int requiredSleepPercentage
    ) {
        if (dimension() != Level.OVERWORLD) {
            // Use vanilla logic.
            return instance.areEnoughSleeping(requiredSleepPercentage);
        }
        // Make the check ourselves
        boolean enough = instance.areEnoughSleeping(requiredSleepPercentage) &&
            instance.areEnoughDeepSleeping(requiredSleepPercentage, this.players());
        // Check for cancellation regardless of whether the checks work, to avoid issues with early wakeups.
        ServerState state = ServerState.getFor((ServerLevel) (Object) this);
        if (state.isSkipTickDelay() && (isDay() || !enough)) {
            stopTheDeLorean(state);
            return false;
        }
        if (!enough) {
            // Inform vanilla logic of conditional result.
            return false;
        }
        if (!this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            // Use vanilla logic.
            return true;
        }
        // If we haven't started skipping yet, start now.
        if (!state.isSkipTickDelay()) {
            startTheDeLorean(state);
        }
        // Abort vanilla logic.
        return false;
    }

    @Unique
    private void startTheDeLorean(ServerState state) {
        getServer().getPlayerList().broadcastSystemMessage(
            Component.literal("[OUTASPLE] Accelerating night...").withStyle(ChatFormatting.AQUA), false
        );
        state.setSkipTickDelay(true);
    }

    @Unique
    private void stopTheDeLorean(ServerState state) {
        var message = isDay()
            ? Component.literal("[OUTASPLE] Sleep time's over!").withStyle(ChatFormatting.AQUA)
            : Component.literal("[OUTASPLE] Stopping acceleration, players woke up early.")
            .withStyle(ChatFormatting.RED);
        getServer().getPlayerList().broadcastSystemMessage(
            message, false
        );
        state.setSkipTickDelay(false);
        // Wake everyone up!
        wakeUpAllPlayers();
        if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE) && this.isRaining()) {
            this.resetWeatherCycle();
        }
    }
}
