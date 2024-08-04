package de.nightevolution.realisticplantgrowth.user;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface CommandUser {
    @NotNull
    Audience getAudience();

    /**
     * Checks if CommandUser has the given permission String
     *
     * @param permission String to check
     * @return true, if CommandUser has given permission.
     * false, otherwise.
     */
    boolean hasPermission(@NotNull String permission);

    /**
     * Sends a message to the selected CommandUser.
     *
     * @param component a message Component.
     * @see Component
     */
    default void sendMessage(@NotNull Component component) {
        getAudience().sendMessage(component);
    }

}
