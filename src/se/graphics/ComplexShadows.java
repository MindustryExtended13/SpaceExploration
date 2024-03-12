package se.graphics;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.math.Mathf;
import arc.struct.Seq;

import mindustry.game.EventType.Trigger;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;

import static arc.Core.graphics;

public class ComplexShadows {
    public static final FrameBuffer shadowBuffer = new FrameBuffer();
    public static final Seq<Runnable> runs = new Seq<>();

    public static void drawUnitShadow(UnitType type, Unit unit) {
        ComplexShadows.drawShadow(() -> {
            float e = Mathf.clamp(unit.elevation, type.shadowElevation, 1) * type.shadowElevationScl * (1 - unit.drownTime);
            Draw.rect(type.shadowRegion, unit.x - 12 * e, unit.y - 13 * e, unit.rotation - 90);
        });
    }

    public static void drawUnitSoftShadow(UnitType type, float x, float y, float rotation) {
        ComplexShadows.drawShadow(() -> {
            float rad = 1.6F;
            float size = (float) Math.max(type.region.width, type.region.height) * type.region.scl();
            Draw.rect(type.softShadowRegion, x, y, size * rad * Draw.xscl, size * rad * Draw.yscl, rotation - 90);
        });
    }

    public static void drawShadow(Runnable run) {
        if(run != null) runs.add(run);
    }

    static  {
        Events.run(Trigger.draw, () -> {
            shadowBuffer.resize(graphics.getWidth(), graphics.getHeight());
            Seq<Runnable> buffer = runs.copy();
            runs.clear();

            Draw.draw(Layer.blockProp + 1, () -> {
                Draw.flush();
                shadowBuffer.begin(Color.clear);
                for(Runnable run : buffer) {
                    Draw.color();
                    run.run();
                    Draw.color();
                }
                shadowBuffer.end();
                Draw.color(Pal.shadow, Pal.shadow.a);
                BufferedDraw.drawBuffer(shadowBuffer);
                Draw.flush();
                Draw.color();
            });
        });
    }
}