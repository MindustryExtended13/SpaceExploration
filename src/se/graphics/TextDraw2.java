package se.graphics;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.math.geom.Vec2;
import arc.util.pooling.Pools;

import mindustry.ui.Fonts;

import org.jetbrains.annotations.NotNull;

public class TextDraw2
{
    public static GlyphLayout LAYOUT = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
    public static final float TEXT_SCALE_CONSTANT = 0.33333334F;
    public static float TEXT_SCALE = TEXT_SCALE_CONSTANT;
    public static Font FONT = Fonts.outline;

    public static @NotNull Vec2 text(float x, float y, float maxWidth, Color color, Object obj)
    {
        boolean ints = FONT.usesIntegerPositions();
        FONT.setUseIntegerPositions(false);
        String text = String.valueOf(obj);

        if(maxWidth <= 0.0F)
        {
            FONT.getData().setScale(TEXT_SCALE);
            LAYOUT.setText(FONT, text);
        }
        else
        {
            FONT.getData().setScale(1.0F);
            LAYOUT.setText(FONT, text);
            FONT.getData().setScale(Math.min(TEXT_SCALE, maxWidth / LAYOUT.width));
            LAYOUT.setText(FONT, text);
        }

        FONT.setColor(color);
        FONT.draw(text, x, y + LAYOUT.height / 2.0F, 1);
        Vec2 out = new Vec2(LAYOUT.width, LAYOUT.height);
        FONT.setUseIntegerPositions(ints);
        FONT.setColor(Color.white);
        FONT.getData().setScale(1.0F);
        Draw.reset();
        Pools.free(LAYOUT);
        return out;
    }

    public static @NotNull Vec2 size(Object obj, float maxWidth)
    {
        boolean ints = FONT.usesIntegerPositions();
        FONT.setUseIntegerPositions(false);
        String text = String.valueOf(obj);

        if(maxWidth <= 0.0F)
        {
            FONT.getData().setScale(TEXT_SCALE);
            LAYOUT.setText(FONT, text);
        }
        else
        {
            FONT.getData().setScale(1.0F);
            LAYOUT.setText(FONT, text);
            FONT.getData().setScale(Math.min(TEXT_SCALE, maxWidth / LAYOUT.width));
            LAYOUT.setText(FONT, text);
        }

        Vec2 out = new Vec2(LAYOUT.width, LAYOUT.height);
        FONT.setUseIntegerPositions(ints);
        FONT.getData().setScale(1.0F);
        Draw.reset();
        Pools.free(LAYOUT);
        return out;
    }
}
