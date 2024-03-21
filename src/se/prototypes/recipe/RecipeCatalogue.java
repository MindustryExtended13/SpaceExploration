package se.prototypes.recipe;

import arc.Core;
import arc.Events;
import arc.struct.ObjectMap;

import mindustry.Vars;

import se.event.RecipeCatalogueLoadingEvent;
import se.util.Tables;

public class RecipeCatalogue {
    public static final ObjectMap<String, Recipe> all = new ObjectMap<>();

    public static void create() {
        Events.fire(new RecipeCatalogueLoadingEvent(1));
        Events.fire(new RecipeCatalogueLoadingEvent(2));

        var map = Core.bundle.getProperties();
        Vars.content.blocks().each(b -> {
            var x = "recipe.block-" + b.name;
            if(!map.containsKey(x + ".name")) {
                map.put(x + ".name", b.localizedName);
            }
            if(!map.containsKey(x + ".desc")) {
                map.put(x + ".desc", b.description);
            }

            if(Tables.contains(all, (k, v) -> v.out.contains(s -> s.item != null && s.item.get() == b))) {
                return;
            }

            all.put("block-" + b.name, Recipe.create(b));
        });
        Core.bundle.setProperties(map);

        Events.fire(new RecipeCatalogueLoadingEvent(3));
    }
}