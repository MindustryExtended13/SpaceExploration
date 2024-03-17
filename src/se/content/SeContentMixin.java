package se.content;

import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mindustry.mod.Mods.LoadedMod;

public interface SeContentMixin {
    default <T extends MappableContent> T mappableTransformation(T content, LoadedMod ignored) {
        return content;
    }

    default <T extends Content> T contentTransformation(T content, LoadedMod ignored) {
        return content;
    }

    default String transformName(String name, String ignored, LoadedMod ignored2) {
        return name;
    }

    default void overrideContentSetter(Content ignored, LoadedMod ignored3) {
    }
}