package se.prototypes.unit;

import me13.core.units.XeonUnitType;

import mindustry.type.UnitType;

public class Hidden {
    public static UnitType data;

    public static void load() {
        data = new XeonUnitType("data") {{
            constructor = DataUnitEntity::new;
            health = Float.POSITIVE_INFINITY;
            hidden = true;
            hitSize = 0f;
        }};
    }
}
