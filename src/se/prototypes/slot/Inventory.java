package se.prototypes.slot;

import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;

import org.jetbrains.annotations.NotNull;

public class Inventory {
    private final Seq<Slot> slots = new Seq<>();

    public int length() {
        return slots.size;
    }

    public Slot getAt(int index) {
        return slots.get(index);
    }

    public float amountOf(SlotItem item) {
        return item == null ? 0 : slots
                .select(s -> item.equals(s.stack.item))
                .sumf(s -> s.stack.count);
    }

    //a.k.a. avaliable
    public Slot findAvailableFor(SlotItem item, boolean findFull) {
        if(item == null) {
            return null;
        }

        var need = slots.find(s -> item.equals(s.stack.item) && (findFull || s.stack.count < s.maxStackSize()));

        if(need == null) {
            var slot = slots.find(s -> s.stack.item == null);

            if(slot == null) {
                return null;
            }

            slot.stack.item = item;
            slot.stack.count = 0;
            return slot;
        }

        return need;
    }

    public Seq<Slot> expand(int amount) {
        if(amount >= 0) {
            for(int i = 0; i < amount; i++) {
                slots.add(new Slot());
            }

            return new Seq<>();
        } else {
            Seq<Slot> output = new Seq<>();

            for(int i = 0; i < -amount; i++) {
                if(output.size == 0) {
                    break;
                }

                output.add(slots.pop());
            }

            return output.reverse();
        }
    }

    public StackChangeResult removeItem(SlotItem item, float count) {
        var result = new StackChangeResult();
        result.queue = new Seq<>();
        result.item = item;
        result.count = count;

        if(item != null && count > 0) {
            _func481031(result, count);
        }

        return result;
    }

    public StackChangeResult pushItem(SlotItem item, float count) {
        var result = new StackChangeResult();
        result.queue = new Seq<>();
        result.item = item;
        result.count = count;

        if(item != null && count > 0) {
            _func139531(result, count);
        }

        return result;
    }

    private void _func481031(@NotNull StackChangeResult result, float x) {
        var avaliable = findAvailableFor(result.item, true);

        if(avaliable == null) {
            if(x > 0) {
                result.left = x;
            }

            return;
        }

        result.queue.add(avaliable);
        float stack = avaliable.stack.count - x;

        if(stack < 0) {
            avaliable.stack.item = null;
            _func481031(result, x - avaliable.stack.count);
        } else {
            avaliable.stack.count -= x;
            result.left = 0;
        }
    }

    private void _func139531(@NotNull StackChangeResult result, float x) {
        var available = findAvailableFor(result.item, false);

        if(available == null) {
            if(x > 0) {
                result.left = x;
            }

            return;
        }

        float maxStack = available.maxStackSize();
        float stack = available.stack.count + x;

        if(stack > maxStack) {
            available.stack.count = maxStack;
            _func139531(result, stack - maxStack);
        } else {
            available.stack.count += x;
            result.left = 0;
        }

        result.queue.add(available);
    }

    public void write(@NotNull Writes writes) {
        writes.i(length());
        slots.each(s -> s.write(writes));
    }

    public void read(@NotNull Reads reads) {
        expand(reads.i());

        for(int i = 0; i < length(); i++) {
            slots.get(i).read(reads);
        }
    }

    public static class StackChangeResult {
        public Seq<Slot> queue;
        public SlotItem item;
        public float count;
        public float left;
    }
}
