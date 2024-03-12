package se;

import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.util.Reflect;

import me13.core.units.XeonUnitType;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;
import mindustry.mod.Mods;

import se.prototypes.unit.Types;
import se.prototypes.unit.UnitsShadows;
import se.prototypes.unit.shadowiInstances.ShadowXeonUnitType;
import se.ui.window.WindowStack;
import se.ui.windows.Main;

import static mindustry.Vars.*;
import static se.SpaceExploration.*;

public class SpaceExplorationMod extends Mod {
    public SpaceExplorationMod() {
        if(mobile) {
            LOGGERW.log("Mod don't supported for mobile can call bugs!");
        }

        Events.run(ClientLoadEvent.class, () -> {
            mainWindow = new Main();

            mainWindow.selected = Main.addButton("Crafting", Main.def, (ignored) -> {});
            Main.addButton("Logistics", Main.def, (ignored) -> {});
            Main.addButton("Debugging", Main.def, (ignored) -> {});

            Main.addCategory("Transportation", Blocks.conveyor);
            Main.addCategory("Machines", Blocks.graphitePress);
            Main.addCategory("Components", Items.beryllium);
            Main.addCategory("Military", Blocks.spectre);

            Core.app.addListener(mainWindow);
            WindowStack.push(mainWindow);
            mainWindow.open();

            Events.run(Trigger.update, () -> {
                if(state.isCampaign() && net.server()) { //mod don't support server-campaign
                    throw new IllegalStateException("SE + server AND campaign = NO! but SE + server XOR campaign = OK!");
                }

                var t = ((Table) Reflect.get(ui.hudfrag.blockfrag, "toggler"));
                if(t != null && t.visible) {
                    t.visible(() -> false);
                    t.updateVisibility();
                }
            });

            Core.app.post(() -> {
                //Fixes some visual bugs
                mainWindow.rollUp();
            });

            UnitsShadows.load();
        });

        Events.run(UnitsShadows.UnitsShadowsPreLoadEvent.class, () -> {
            UnitsShadows.classMap.put(XeonUnitType.class, (t) -> new ShadowXeonUnitType(t.name));
            UnitsShadows.vanillaLoader();
        });

        LOGGER.log("Loaded mod constructor!");
    }

    @Override
    public void init() {
        Mods.LoadedMod instance = mods.getMod(MOD_ID);

        if(instance == null) {
            instance = mods.getMod(getClass());
        }

        if(instance == null) {
            throw new NullPointerException("instance not found!");
        }

        //instance is always non-null value
        SpaceExploration.instance = instance;
        SpaceExploration.load();

        if(maxSchematicSize < 128) {
            maxSchematicSize = 128;
        }

        ServerIntegration.load();
    }

    @Override
    public void loadContent() {
        Types.load();

        se.prototypes.item.Hidden.load();
        se.prototypes.unit.Hidden.load();
    }
}