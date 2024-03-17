package se.prototypes.slot;

import arc.util.io.Reads;
import arc.util.io.Writes;
import org.jetbrains.annotations.NotNull;

public class SlotData {
    public int stackSize = 200;

    public boolean validStackSize() {
        return stackSize > 0; //if 0 or smaller then invalid
    }

    public void write(@NotNull Writes writes) {
        writes.i(stackSize);
    }

    public void read(@NotNull Reads reads) {
        stackSize = reads.i();
    }
}
