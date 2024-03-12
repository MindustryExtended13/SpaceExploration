package se.prototypes.unit.shadowiInstances;

import me13.core.units.XeonUnitType;
import mindustry.gen.Unit;

//Used for non-playable units for example data units
public class NoShadowUnitType extends XeonUnitType {
    public NoShadowUnitType(String name) {
        super(name);
    }

    @Override
    public void drawShadow(Unit unit) {
    }

    @Override
    public void drawSoftShadow(float x, float y, float rotation, float alpha) {
    }
}