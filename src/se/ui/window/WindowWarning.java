package se.ui.window;

import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.event.HandCursorListener;
import arc.scene.ui.Image;
import arc.scene.ui.Tooltip;
import arc.scene.ui.Tooltip.Tooltips;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import arc.util.Time;

import mindustry.gen.Icon;

public class WindowWarning
{
    public static final WindowWarning FOCUSABLE = new WindowWarning("focusable", "Auto-unfocus disabled for this window.\nClick to the footer to unfocus window!", Color.yellow);
    public static final float SWITCH_LOOP_PERIOD = 60f;
    public static final float SWITCH_PERIOD;
    public Tooltip tooltip;
    public Color color;
    public String id;

    static
    {
        SWITCH_PERIOD = SWITCH_LOOP_PERIOD / 2;
    }

    public WindowWarning(String id, Tooltip tooltip, Color color)
    {
        this.tooltip = tooltip;
        this.color = color;
        this.id = id;
    }

    public WindowWarning(String id, Cons<Table> cons, Color color)
    {
        this(id, new Tooltip(cons), color);
    }

    public WindowWarning(String id, String text, Color color)
    {
        this(id, Tooltips.getInstance().create(text), color);
    }

    public WindowWarningCell build(Table table, Window window)
    {
        var result = new WindowWarningCell();
        result.owner = this;

        var cell = table.image(Icon.warning).update(i -> {
            if(Time.globalTime % SWITCH_LOOP_PERIOD > SWITCH_PERIOD)
            {
                i.color.set(color);
            }
            else
            {
                i.color.a(0);
            }
        }).scaling(Scaling.fit).size(20);
        
        result.cell = cell;
        Image image = cell.get();
        image.addListener(tooltip);
        result.image = image;

        if(window != null)
        {
            image.addListener(new HandCursorListener());
            image.clicked(() -> window.rmWarning(this));
        }

        return result;
    }

    public static class WindowWarningCell
    {
        public WindowWarning owner;
        public Cell<Image> cell;
        public Image image;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof WindowWarning w && this.id.equals(w.id);
    }
}
