package de.nightevolution.user;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public abstract class OnlineUser extends User implements CommandUser {

    /**
     * A cross-platform representation of a logged-in {@link User}.
     */
    public OnlineUser(@NotNull UUID uuid, @NotNull String username) {
        super(uuid, username);
    }

    /**
     * Get the adventure {@link Audience} for this player.
     *
     * @return the adventure {@link Audience} for this player
     */
    @NotNull
    public abstract Audience getAudience();

    /**
     * Returns if the player has the permission node.
     *
     * @param node The permission node string
     * @return {@code true} if the player has the node; {@code false} otherwise
     */
    public abstract boolean hasPermission(@NotNull String node);

    /**
     * Returns a {@link Map} of a player's permission nodes.
     *
     * @return a {@link Map} of all permissions this player has to their set values
     */
    @NotNull
    public abstract Map<String, Boolean> getPermissions();


    /**
     * Dispatch a MineDown-formatted chat message to this player.
     *
     * @param component the {@link Component} to send
     */
    public void sendMessage(@NotNull Component component) {
        getAudience().sendMessage(component);
    }

    /**
     * Send a plugin message to the user.
     *
     * @param channel channel to send it on
     * @param message byte array of message data
     */
    public abstract void sendPluginMessage(@NotNull String channel, byte[] message);


}