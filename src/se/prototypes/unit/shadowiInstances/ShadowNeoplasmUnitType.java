package se.prototypes.unit.shadowiInstances;

import mindustry.gen.Unit;
import mindustry.type.unit.NeoplasmUnitType;

import se.graphics.ComplexShadows;

public class ShadowNeoplasmUnitType extends NeoplasmUnitType {
    public ShadowNeoplasmUnitType(String name) {
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