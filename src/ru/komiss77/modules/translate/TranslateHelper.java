package ru.komiss77.modules.translate;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.entity.Player;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Created by Meow J on 6/20/2015.
 * <p>
 * Language helper
 *
 * @author Meow J
 */
    @Deprecated
public class TranslateHelper {

    /**
     * Return the language of the player
     *
     * @param player The player to be analyzed
     * @return the language of the player(in Java locale format)
     */
    @Deprecated
    public static EnumLang getPlayerLanguage(Player player) {
        return EnumLang.get(player.locale().toString());
    }

    /**
     * Return the locale of the player
     *
     * @param player The player to be analyzed
     * @return the locale of the player
     */
    @Deprecated
    public static Locale getPlayerLocale(Player player) {
        return Locale.forLanguageTag(player.locale().toString().replace('_', '-'));
    }


    /**
     * Copied from <a href="https://github.com/SpigotMC/BungeeCord/blob/master/chat/src/main/java/net/md_5/bungee/api/chat/">link</a>
     * <p>
     * Copyright (c) 2012, md_5. All rights reserved.
     * <p>
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions are met:
     * <p>
     * Redistributions of source code must retain the above copyright notice, this
     * list of conditions and the following disclaimer.
     * <p>
     * Redistributions in binary form must reproduce the above copyright notice,
     * this list of conditions and the following disclaimer in the documentation
     * and/or other materials provided with the distribution.
     * <p>
     * The name of the author may not be used to endorse or promote products derived
     * from this software without specific prior written permission.
     * <p>
     * You may not use the software for commercial software hosting services without
     * written permission from the author.
     * <p>
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
     * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
     * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
     * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
     * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
     * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
     * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
     * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
     * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
     * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
     * POSSIBILITY OF SUCH DAMAGE.
     */
    @Deprecated
    public static void toLegacyText(StringBuilder builder, TranslatableComponent translatable, EnumLang lang) {
        String trans = Translate.translateToLocal(translatable.getTranslate(), translatable.getTranslate(), lang);

        Matcher matcher = translatable.getFormat().matcher(trans);
        int position = 0;
        int i = 0;
        while (matcher.find(position)) {
            int pos = matcher.start();
            if (pos != position) {
                addFormat(translatable, builder);
                builder.append(trans, position, pos);
            }
            position = matcher.end();

            String formatCode = matcher.group(2);
            switch (formatCode.charAt(0)) {
                case 's':
                case 'd':
                    String withIndex = matcher.group(1);
                    toLegacyText(builder, translatable.getWith().get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++), lang);
                    break;
                case '%':
                    addFormat(translatable, builder);
                    builder.append('%');
                    break;
            }
        }
        if (trans.length() != position) {
            addFormat(translatable, builder);
            builder.append(trans.substring(position));
        }
        toLegacyText(builder, (BaseComponent) translatable, lang);
    }

    @Deprecated
    public static void toLegacyText(StringBuilder builder, BaseComponent baseComponent, EnumLang locale) {
        if (baseComponent.getExtra() != null) {
            for (BaseComponent e : baseComponent.getExtra()) {
                if (e instanceof TranslatableComponent) {
                    toLegacyText(builder, (TranslatableComponent) e, locale);
                } else {
                    builder.append(e.toLegacyText());
                }
            }
        }
    }

    @Deprecated
    private static void addFormat(TranslatableComponent translatable, StringBuilder builder) {
        builder.append(translatable.getColor());
        if (translatable.isBold()) {
            builder.append(ChatColor.BOLD);
        }

        if (translatable.isItalic()) {
            builder.append(ChatColor.ITALIC);
        }

        if (translatable.isUnderlined()) {
            builder.append(ChatColor.UNDERLINE);
        }

        if (translatable.isStrikethrough()) {
            builder.append(ChatColor.STRIKETHROUGH);
        }

        if (translatable.isObfuscated()) {
            builder.append(ChatColor.MAGIC);
        }
    }

    @Deprecated
    public static void toPlainText(StringBuilder builder, TranslatableComponent translatable, EnumLang locale) {
        String trans = Translate.translateToLocal(translatable.getTranslate(), translatable.getTranslate(), locale);

        Matcher matcher = translatable.getFormat().matcher(trans);
        int position = 0;
        int i = 0;
        while (matcher.find(position)) {
            int pos = matcher.start();
            if (pos != position) {
                builder.append(trans, position, pos);
            }
            position = matcher.end();

            String formatCode = matcher.group(2);
            switch (formatCode.charAt(0)) {
                case 's':
                case 'd':
                    String withIndex = matcher.group(1);
                    toPlainText(builder, translatable.getWith().get(withIndex != null ? Integer.parseInt(withIndex) - 1 : i++), locale);
                    break;
                case '%':
                    builder.append('%');
                    break;
            }
        }
        if (trans.length() != position) {
            builder.append(trans.substring(position));
        }

        toPlainText(builder, (BaseComponent) translatable, locale);
    }

    @Deprecated
    public static void toPlainText(StringBuilder builder, BaseComponent baseComponent, EnumLang locale) {
        if (baseComponent.getExtra() != null) {
            for (BaseComponent e : baseComponent.getExtra()) {
                if (e instanceof TranslatableComponent) {
                    toPlainText(builder, (TranslatableComponent) e, locale);
                } else {
                    builder.append(e.toPlainText());
                }
            }
        }
    }

    @Deprecated
    public static String toLegacyText(BaseComponent component, EnumLang locale) {
        StringBuilder builder = new StringBuilder();
        if (component instanceof TranslatableComponent) {
            toLegacyText(builder, (TranslatableComponent) component, locale);
        } else {
            toLegacyText(builder, component, locale);
        }
        return builder.toString();
    }


    @Deprecated
    public static String toPlainText(BaseComponent component, EnumLang locale) {
        StringBuilder builder = new StringBuilder();
        if (component instanceof TranslatableComponent) {
            toPlainText(builder, (TranslatableComponent) component, locale);
        } else {
            toPlainText(builder, component, locale);
        }
        return builder.toString();
    }
}
