package se.prototypes.item;

import arc.math.Mathf;
import arc.struct.Seq;

import mindustry.ctype.UnlockableContent;
import mindustry.type.ItemStack;

import org.jetbrains.annotations.Contract;

import se.prototypes.slot.SlotItem;

public class SeItemStack {
    public SlotItem item;
    public float count;

    public static SeItemStack empty() {
        SeItemStack stack = new SeItemStack(null, 0);
        stack.item = null;
        return stack;
    }

    @Contract(pure = true)
    public SeItemStack(UnlockableContent content, float count) {
        this.item = new SlotItem(content);
        this.count = count;
    }

    public static Seq<SeItemStack> create(ItemStack... stacks) {
        return Seq.with(stacks).map(stack -> new SeItemStack(stack.item, stack.amount));
    }

    public UnlockableContent item() {
        return this.item.get();
    }

    public int intCount() {
        return Mathf.floor(this.count);
    }

    public float liquidCount() {
        return this.count * 60;
    }
}