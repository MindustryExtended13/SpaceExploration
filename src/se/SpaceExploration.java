package se;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;

import me13.core.logger.ILogger;
import me13.core.logger.LogBinder;
import me13.core.logger.LoggerFactory;

import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Mods;
import mindustry.type.Item;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import se.prototypes.slot.Inventory;
import se.prototypes.unit.DataUnitEntity;
import se.prototypes.unit.Hidden;
import se.ui.windows.Main;

public class SpaceExploration {
    public static final ILogger BASE_LOGGER;
    public static final LogBinder LOGGERW;
    public static final LogBinder LOGGERE;
    public static final LogBinder LOGGER;
    public static final String MOD_NAME;
    public static final String MOD_ID;

    public static Inventory campaignInventory;
    public static Inventory currentInventory;
    public static Mods.LoadedMod instance;
    public static Main mainWindow;

    static {
        MOD_ID = "se";
        MOD_NAME = "SpaceExploration";
        BASE_LOGGER = LoggerFactory.build();

        LOGGERE = BASE_LOGGER.atError().setPrefix(MOD_NAME);
        LOGGERW = BASE_LOGGER.atWarn().setPrefix(MOD_NAME);
        LOGGER = BASE_LOGGER.atInfo().setPrefix(MOD_NAME);
    }

    @Contract("_ -> new")
    public static @NotNull TextureRegionDrawable getDrawableFor(@NotNull Item item) {
        return new TextureRegionDrawable(item.uiIcon);
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull TextureRegion subImage(TextureRegion original, int x, int y, int w, int h) {
        return new TextureRegion(original, x, y, w, h);
    }

    public static TextureRegion get(String prefix) {
        return Core.atlas.find(MOD_ID + "-" + prefix);
    }

    public static @NotNull Inventory createDefault() {
        var inv = new Inventory();
        inv.expand(35);
        return inv;
    }

    public static Inventory getInventory() {
        return getInventoryFor(Vars.player);
    }

    public static Inventory getInventoryFor(Player player) {
        if(Vars.net.server()) {
            return getEntity().getInventory(DataUnitEntity.LOCAL_PREFIX);
        }

        if(Vars.net.client()) {
            return currentInventory;
        }

        return getEntity().getInventory(player);
    }

    static DataUnitEntity x;
    @Contract(pure = true)
    public static DataUnitEntity getEntity() {
        return x;
    }

    public static boolean inited() {
        return x != null || Vars.net.active();
    }

    public static void load() {
        Events.run(EventType.WorldLoadEndEvent.class, () -> Core.app.post(() -> {
            if(Vars.net.client()) {
                return;
            }

            x = null;

            Groups.unit.each(unit -> {
                if(unit instanceof DataUnitEntity entity) {
                    x = entity;
                }
            });

            if(x == null) {
                LOGGERW.log("Data entity not found! Creating new one...");
                x = (DataUnitEntity) Hidden.data.spawn(Team.sharded, -666, -666);
                //If something do wrong
                Groups.unit.add(x);
            }
        }));
    }
}