package se.prototypes.unit.shadowiInstances;

import me13.core.units.XeonUnitType;

import mindustry.gen.Unit;

import se.graphics.ComplexShadows;

public class ShadowXeonUnitType extends XeonUnitType {
    public ShadowXeonUnitType(String name) {
        super(name);
    }

    @Override
    public void drawShadow(Unit unit) {
        ComplexShadows.drawUnitShadow(this, unit);
    }

    @Override
    public void drawSoftShadow(float x, float y, float rotation, float ignored) {
        ComplexShadows.drawUnitSoftShadow(this, x, y, rotation);
    }
}