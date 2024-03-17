package se;

import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Reflect;

import me13.core.units.XeonUnitType;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.game.EventType.*;
import mindustry.mod.Mod;
import mindustry.mod.Mods;

import mindustry.mod.Scripts;
import se.content.HiddenWorkContentLoader;
import se.content.SeContentLoader;
import se.content.VanillaMixin;
import se.event.ContentMixinsSetupEvent;
import se.prototypes.unit.Types;
import se.prototypes.unit.UnitsShadows;
import se.prototypes.unit.shadowiInstances.ShadowXeonUnitType;
import se.ui.window.WindowStack;
import se.ui.windows.Main;
import se.util.Tables;

import static mindustry.Vars.*;
import static se.SpaceExploration.*;

public @se.Mod class SpaceExplorationMod extends Mod {
    public SpaceExplorationMod() {
        if(mobile) {
            LOGGERW.log("Mod don't supported for mobile can call bugs!");
        }

        Events.run(ClientLoadEvent.class, () -> {
            HiddenWorkContentLoader hiddenContent = new HiddenWorkContentLoader();
            content = Tables.deepCopy(content, () -> hiddenContent, Seq.with());
            hiddenContent.setNoAddMode(true);
            se_content = new SeContentLoader();
            for(int i = 0; i < 7; i++) {
                LOGGER.log("Setting content mixins phase {}", i + 1);
                Events.fire(new ContentMixinsSetupEvent(i + 1));
            }
            se_content.loadFrom();
            hiddenContent.setNoAddMode(false);
            content = se_content;
            LOGGER.log("Re-Fixing content");
            se_content.each(se_content::fixFields);

            mainWindow = new Main();

            mainWindow.selected = Main.addButton("Crafting", Main.def, Main::buildCrafting);
            Main.addButton("Logistics", Main.def, (ignored) -> {});
            Main.addButton("Debugging", Main.def, (ignored) -> {});

            Main.selectedCategory = Main.transportation = Main.addCategory("Automation", Blocks.conveyor);
            Main.components = Main.addCategory("Components", Items.beryllium);
            Main.military = Main.addCategory("Military", Blocks.duo);
            Main.landfill = Main.addCategory("Landfills", Blocks.grass);

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

            Main.load();

            try {
                Scripts scripts = mods.getScripts();
                scripts.context.evaluateReader(scripts.scope, instance.root.child("scripts").child("main.js").reader(), "main.js", 0);
                LOGGER.log("Added constant variables to developer console.");
            } catch(Throwable e) {
                LOGGERE.log("{}", Tables.toString(e));
            }
        });

        Events.on(ContentMixinsSetupEvent.class, (e) -> {
            switch(e.phase()) {
                /*
                    1 -> pre pre initing
                    2 -> pre initing
                    3 -> post pre initing / pre initing
                    4 -> initing
                    5 -> post initing / pre post initing
                    6 -> post initing
                    7 -> post post initing
                 */

                case 2: {
                    UnitsShadows.classMap.put(XeonUnitType.class, (t) -> new ShadowXeonUnitType(t.name));
                    UnitsShadows.vanillaLoader();
                }

                case 4: {
                    UnitsShadows.load();
                    se_content.mixins.add(new VanillaMixin());
                }
            }
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