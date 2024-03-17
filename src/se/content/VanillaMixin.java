package se.content;

import arc.util.Strings;

import mindustry.content.*;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mindustry.mod.Mods;

import se.SpaceExploration;
import se.util.Tables;

public class VanillaMixin implements SeContentMixin {
    @Override
    public void overrideContentSetter(Content content, Mods.LoadedMod ignored) {
        if(content != null && content.isVanilla() && content instanceof MappableContent m) {
            Class<?> cl = switch(content.getContentType()) {
                case item -> Items.class;
                case block -> Blocks.class;
                case bullet -> Bullets.class;
                case liquid -> Liquids.class;
                case status -> StatusEffects.class;
                case unit -> UnitTypes.class;
                case weather -> Weathers.class;
                case sector -> SectorPresets.class;
                case planet -> Planets.class;
                case team -> TeamEntries.class;
                default -> null;
            };

            if(cl != null) Tables.deepEdit(cl, (field, ignored2) -> {try {
                var value = field.get(null);
                if(value instanceof MappableContent m2 && m2.name.equals(m.name) && m != m2) {
                    field.set(null, m);
                }
            } catch(Throwable ignored3) {
                SpaceExploration.LOGGERE.log("Error SE5208: {}", Strings.kebabToCamel(m.name));
            }});
        }
    }
}