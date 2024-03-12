package se.prototypes.unit;

import mindustry.type.UnitType;

import se.prototypes.unit.shadowiInstances.NoShadowUnitType;

public class Hidden {
    public static UnitType data;

    public static void load() {
        data = new NoShadowUnitType("data") {{
            constructor = DataUnitEntity::new;
            health = Float.POSITIVE_INFINITY;
            hidden = true;
            hitSize = 0f;
        }};
    }
}
