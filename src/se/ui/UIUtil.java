package se.ui;

import arc.func.Prov;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;

import org.jetbrains.annotations.Contract;

public class UIUtil {
    @Contract("null, _ -> null")
    public static Cell<Label> label(Table table, Prov<CharSequence> prov) {
        if(table == null) {
            return null;
        }

        return table.add("").update(mabel -> {
            if(prov == null) {
                mabel.setText("null");
            } else {
                mabel.setText(prov.get());
            }
        });
    }
}
