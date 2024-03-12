package se.prototypes.unit;

import arc.Events;
import arc.func.Cons2;
import arc.func.Func;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.Strings;

import mindustry.content.UnitTypes;
import mindustry.type.UnitType;
import mindustry.type.unit.ErekirUnitType;
import mindustry.type.unit.NeoplasmUnitType;
import mindustry.type.unit.TankUnitType;
import mindustry.world.blocks.storage.CoreBlock;

import org.jetbrains.annotations.NotNull;

import se.SpaceExploration;
import se.prototypes.Manager;
import se.prototypes.unit.shadowiInstances.*;
import se.util.Tables;

import static mindustry.Vars.*;

//TODO make project hpl and omaloon loaders
public class UnitsShadows {
    public static final ObjectMap<Class<?>, Func<UnitType, ? extends UnitType>> classMap = new ObjectMap<>();
    public static final Seq<UnitTypesLoader> loaders = new Seq<>();

    public static void vanillaLoader() {
        classMap.put(NeoplasmUnitType.class, (t) -> new ShadowNeoplasmUnitType(t.name));
        classMap.put(ErekirUnitType.class, (t) -> new ShadowErekirUnitType(t.name));
        classMap.put(TankUnitType.class, (t) -> new ShadowTankUnitType(t.name));
        classMap.put(UnitType.class, (t) -> new ShadowUnitType(t.name));

        loaders.add(create("VanillaLoader", (type, copy) -> {
            if(type.isVanilla()) try {
                Reflect.set(UnitTypes.class, Strings.kebabToCamel(type.name), copy);
            } catch(Throwable e) {
                SpaceExploration.LOGGERE.log("Failed to set field of unit, {}", Tables.toString(e));
            }
        }));
    }

    public static UnitType fix(@NotNull UnitType type) {
        return content.unit(type.name);
    }

    public static void load() {
        Events.fire(new UnitsShadowsPreLoadEvent());

        SpaceExploration.LOGGER.log("Active loaders:");
        SpaceExploration.LOGGER.log("SpaceExplorationLoader");
        for(var loader : loaders) {
            SpaceExploration.LOGGER.log(loader.name());
        }

        content.units().copy().each(type -> {
            var classType = Tables.getTableType(type.getClass());
            if(!classMap.containsKey(classType)) return;
            var func = classMap.get(classType);

            //console fills with logs if not hidden work
            Tables.hiddenWork(() -> {
                UnitType copy = (UnitType) Manager.replaceWithSameName(type, () -> Tables.deepCopy(type, func::get, Seq.with()));
                loaders.each(loader -> loader.execute(type, copy));
            });
        });

        content.blocks().each(b -> {
            if(b instanceof CoreBlock block) {
                block.unitType = fix(block.unitType);
            }
        });

        Events.fire(new UnitsShadowsPostLoadEvent());
    }

    public static UnitTypesLoader create(String name, Cons2<UnitType, UnitType> executor) {
        return new UnitTypesLoader() {
            @Override
            public void execute(UnitType type, UnitType replacement) {
                executor.get(type, replacement);
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    public interface UnitTypesLoader {
        void execute(UnitType type, UnitType replacement);
        String name();
    }

    public static class UnitsShadowsPreLoadEvent {}
    public static class UnitsShadowsPostLoadEvent {}
}