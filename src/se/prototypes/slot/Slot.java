package se.prototypes.slot;

import arc.struct.ObjectMap;
import arc.util.io.Reads;
import arc.util.io.Writes;

import mindustry.ctype.UnlockableContent;

import org.jetbrains.annotations.NotNull;

import se.prototypes.item.SeItemStack;

public class Slot {
    public static final ObjectMap<UnlockableContent, SlotData> DEFAULTS = new ObjectMap<>();
    public static final Slot tmp1 = new Slot();
    public SlotData overdrive = new SlotData();
    public SeItemStack stack = SeItemStack.empty();

    {
        overdrive.stackSize = -7;
    }

    public void pick(@NotNull Slot slot) {
        stack = slot.stack;
        overdrive = slot.overdrive;

        slot.stack = SeItemStack.empty();
    }

    public void drop(@NotNull Slot slot) {
        slot.stack.item = stack.item;

        float left = slot.maxStackSize() - slot.stack.count;
        slot.stack.count += Math.min(left, stack.count);
        stack.count -= Math.min(left, stack.count);

        if(stack.count <= 0) {
            stack.item = null;
        }
    }

    public UnlockableContent getItem() {
        return stack.item == null ? null : stack.item.get();
    }

    public SlotData defaults() {
        var it = getItem();
        var def = DEFAULTS.get(it);

        if(def == null) {
            var dat = new SlotData();
            DEFAULTS.put(it, dat);
            return dat;
        }

        return def;
    }

    public float maxStackSize() {
        return overdrive.validStackSize() ? overdrive.stackSize : defaults().stackSize;
    }

    public void read(@NotNull Reads reads) {
        if(reads.bool()) {
            stack.item = new SlotItem(null);
            stack.item.read(reads);
        }

        overdrive.read(reads);
        stack.count = reads.f();
    }

    public void write(@NotNull Writes writes) {
        writes.bool(stack.item != null);

        if(stack.item != null) {
            stack.item.write(writes);
        }

        overdrive.write(writes);
        writes.f(stack.count);
    }
}
