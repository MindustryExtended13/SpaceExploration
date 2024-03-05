package se.prototypes.item;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;

import mindustry.game.EventType;
import mindustry.type.Item;

import se.SpaceExploration;

public class Hidden {
    public static TextureRegion mapping;
    public static Item plus, minus;

    public static void load() {
        plus = new Item("plus", Color.white);
        minus = new Item("minus", Color.white);

        Events.run(EventType.ClientLoadEvent.class, () -> {
            mapping = SpaceExploration.get("hidden");

            setup(plus, 0, 0);
            setup(minus, 1, 0);
        });
    }

    public static void setup(Item item, int x, int y) {
        item.hidden = true;
        item.buildable = true;
        item.uiIcon = item.fullIcon = SpaceExploration.subImage(mapping, x * 32, y * 32, 32, 32);
    }
}