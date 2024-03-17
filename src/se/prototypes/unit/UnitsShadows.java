package se.prototypes.unit;

import arc.func.Func;
import arc.struct.ObjectMap;
import arc.struct.Seq;

import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.mod.Mods;
import mindustry.type.UnitType;
import mindustry.type.unit.ErekirUnitType;
import mindustry.type.unit.NeoplasmUnitType;
import mindustry.type.unit.TankUnitType;

import se.content.SeContentMixin;
import se.prototypes.unit.shadowiInstances.*;
import se.util.Tables;

import static se.SpaceExploration.se_content;

//TODO make project hpl and omaloon loaders
@SuppressWarnings("unchecked")
public class UnitsShadows {
    public static final ObjectMap<Class<?>, Func<UnitType, ? extends UnitType>> classMap = new ObjectMap<>();

    public static void vanillaLoader() {
        classMap.put(NeoplasmUnitType.class, (t) -> new ShadowNeoplasmUnitType(t.name));
        classMap.put(ErekirUnitType.class, (t) -> new ShadowErekirUnitType(t.name));
        classMap.put(TankUnitType.class, (t) -> new ShadowTankUnitType(t.name));
        classMap.put(UnitType.class, (t) -> new ShadowUnitType(t.name));
    }

    public static void load() {
        se_content.mixins.add(new SeContentMixin() {
            @Override
            public <T extends MappableContent> T mappableTransformation(T content, Mods.LoadedMod ignored) {
                if(content != null && content.getContentType() == ContentType.unit) {
                    var classType = Tables.getTableType(content);
                    if(classMap.containsKey(classType)) {
                        se_content.setNoAddMode(true);
                        UnitType[] instance = new UnitType[1];
                        //console fills with logs if not hidden work
                        Tables.hiddenWork(() -> {
                            instance[0] = Tables.deepCopy((UnitType) content, classMap.get(classType)::get, Seq.with());
                        });
                        se_content.setNoAddMode(false);
                        return (T) instance[0];
                    }
                }
                return content;
            }
        });
    }
}