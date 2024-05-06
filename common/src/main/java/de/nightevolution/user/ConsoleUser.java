package de.nightevolution.user;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public final class ConsoleUser implements CommandUser {

    @NotNull
    private final Audience audience;

    public ConsoleUser(@NotNull Audience console) {
        this.audience = console;
    }

    /**
     * Get an Audience object in order to send messages to the console.
     *
     * @return an Audience object representing the console.
     */
    @NotNull
    @Override
    public Audience getAudience() {
        return audience;
    }

    /**
     * Checks, if console has given permission.
     *
     * @param permission String to check.
     * @return always true.
     */
    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

}
