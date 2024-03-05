package se.prototypes.unit;

import me13.core.units.XeonUnits;

public class Types {
    public static void load() {
        XeonUnits.add(DataUnitEntity.class, DataUnitEntity::new);
        XeonUnits.setupID();
    }
}