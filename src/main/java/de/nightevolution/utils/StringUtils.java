package de.nightevolution.utils;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: Add PAPI support
public class StringUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    /**
     * Hex Support for Strings
     * @param unparsedString {@link String} to translate.
     * @return Color code formatted {@link String}.
     */
    @NotNull
    public static String translateColor(@NotNull String unparsedString) {
        /*
        Matcher matcher = HEX_PATTERN.matcher(unparsedString);
        StringBuilder builder = new StringBuilder();

        while (matcher.find())
            matcher.appendReplacement(builder, ChatColor.of("#" + matcher.group(1)).toString());
       return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(builder).toString());
         */
        return ChatColor.translateAlternateColorCodes('&', unparsedString);
    }

}