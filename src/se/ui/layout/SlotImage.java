package se.ui.layout;

import arc.func.Prov;
import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;

import mindustry.core.UI;
import mindustry.ui.Styles;

import se.prototypes.slot.Slot;
import se.ui.UIUtil;

public class SlotImage extends Stack {
    public SlotImage(Slot slot, Color color) {
        this(slot, () -> color, false);
    }

    public SlotImage(Slot slot, Prov<Color> color, boolean alwaysShowNumber) {
        add(new Table(o -> {
            o.image().grow().update(i -> {
                if(color.get() != null) {
                    i.setColor(color.get());
                }
            });
        }));

        add(new Table(o -> {
            o.add(new Image()).size(32f).scaling(Scaling.fit).update(img -> {
                if(slot.stack.item != null) {
                    img.setDrawable(slot.stack.item.get().uiIcon);
                }
            });
        }).visible(() -> slot.stack.item != null));

        add(new Table(t -> {
            t.left().bottom();
            UIUtil.label(t, () -> slot.stack.count >= 1000 ? UI.formatAmount(slot.stack.intCount()) : slot.stack.intCount() + "")
                    .style(Styles.outlineLabel).fontScale(0.7f);
            t.pack();
        }).visible(() -> slot.stack.item != null && (alwaysShowNumber || slot.stack.intCount() != 1)));
    }
}