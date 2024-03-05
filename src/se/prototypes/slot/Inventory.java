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

    public int amountOf(SlotItem item) {
        return item == null ? 0 : slots
                .select(s -> item.equals(s.item))
                .sum(s -> s.count);
    }

    //a.k.a. avaliable
    public Slot findAvailableFor(SlotItem item, boolean findFull) {
        if(item == null) {
            return null;
        }

        var need = slots.find(s -> item.equals(s.item) && (findFull || s.count < s.maxStackSize()));

        if(need == null) {
            var slot = slots.find(s -> s.item == null);

            if(slot == null) {
                return null;
            }

            slot.item = item;
            slot.count = 0;
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

    public StackChangeResult removeItem(SlotItem item, int count) {
        var result = new StackChangeResult();
        result.queue = new Seq<>();
        result.item = item;
        result.count = count;

        if(item != null && count > 0) {
            _func481031(result, count);
        }

        return result;
    }

    public StackChangeResult pushItem(SlotItem item, int count) {
        var result = new StackChangeResult();
        result.queue = new Seq<>();
        result.item = item;
        result.count = count;

        if(item != null && count > 0) {
            _func139531(result, count);
        }

        return result;
    }

    private void _func481031(@NotNull StackChangeResult result, int x) {
        var avaliable = findAvailableFor(result.item, true);

        if(avaliable == null) {
            if(x > 0) {
                result.left = x;
            }

            return;
        }

        result.queue.add(avaliable);
        int stack = avaliable.count - x;

        if(stack < 0) {
            avaliable.item = null;
            _func481031(result, x - avaliable.count);
        } else {
            avaliable.count -= x;
            result.left = 0;
        }
    }

    private void _func139531(@NotNull StackChangeResult result, int x) {
        var available = findAvailableFor(result.item, false);

        if(available == null) {
            if(x > 0) {
                result.left = x;
            }

            return;
        }

        int maxStack = available.maxStackSize();
        int stack = available.count + x;

        if(stack > maxStack) {
            available.count = maxStack;
            _func139531(result, stack - maxStack);
        } else {
            available.count += x;
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
        public int count;
        public int left;
    }
}
