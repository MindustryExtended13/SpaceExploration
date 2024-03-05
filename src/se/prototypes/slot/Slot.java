package se.prototypes.slot;

import arc.struct.ObjectMap;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import org.jetbrains.annotations.NotNull;

public class Slot
{
    public static final ObjectMap<UnlockableContent, SlotData> DEFAULTS = new ObjectMap<>();
    public static final Slot tmp1 = new Slot();
    public SlotData overdrive = new SlotData();
    public SlotItem item;
    public int count;

    {
        overdrive.stackSize = -7;
    }

    public void pick(@NotNull Slot slot)
    {
        item = slot.item;
        count = slot.count;
        overdrive = slot.overdrive;

        slot.item = null;
        slot.count = 0;
    }

    public void drop(@NotNull Slot slot)
    {
        slot.item = item;

        int left = slot.maxStackSize() - slot.count;
        slot.count += Math.min(left, count);
        count -= Math.min(left, count);

        if(count == 0)
        {
            item = null;
        }
    }

    public UnlockableContent getItem()
    {
        return item == null ? null : item.get();
    }

    public SlotData defaults()
    {
        var it = getItem();
        var def = DEFAULTS.get(it);

        if(def == null)
        {
            var dat = new SlotData();
            DEFAULTS.put(it, dat);
            return dat;
        }

        return def;
    }

    public int maxStackSize()
    {
        return overdrive.validStackSize() ? overdrive.stackSize : defaults().stackSize;
    }

    public void read(Reads reads)
    {
        if(reads.bool())
        {
            item = new SlotItem((Item) null);
            item.read(reads);
        }

        overdrive.read(reads);
        count = reads.i();
    }

    public void write(Writes writes)
    {
        writes.bool(item != null);

        if(item != null)
        {
            item.write(writes);
        }

        overdrive.write(writes);
        writes.i(count);
    }
}
