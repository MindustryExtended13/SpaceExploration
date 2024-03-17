package se.ui.layout;

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
        add(new Table(o -> {
            o.image().color(color).grow();
        }));

        add(new Table(o -> {
            o.add(new Image()).size(32f).scaling(Scaling.fit).update(img -> {
                if(slot.item != null) {
                    img.setDrawable(slot.item.get().uiIcon);
                }
            });
        }).visible(() -> slot.item != null));

        add(new Table(t -> {
            t.left().bottom();
            UIUtil.label(t, () -> slot.count >= 1000 ? UI.formatAmount(slot.count) : slot.count + "").style(Styles.outlineLabel).fontScale(0.7f);
            t.pack();
        }).visible(() -> slot.item != null && slot.count != 1));
    }
}