package se.prototypes.recipe;

import arc.struct.Seq;

import mindustry.ctype.ContentType;
import mindustry.world.Block;

import se.prototypes.item.SeItemStack;
import se.prototypes.slot.Inventory;

public class Recipe {
    public Seq<SeItemStack> in  = new Seq<>();
    public Seq<SeItemStack> out = new Seq<>();
    protected boolean canCraftedByHand;
    public float energyConsuming = 1;
    public int craftTime = 60;

    public static Recipe create(Block block) {
        return new Recipe().in(SeItemStack.create(block.requirements).toArray(SeItemStack.class)).out(new SeItemStack(block, 1))
                .setEnergyConsuming(2).setCraftTime(block.size * 60).setCanCraftedByHand(block.size <= 3);
    }

    public float craft(Inventory inventory) {
        if(canCraft(inventory)) {
            for(var stack : in) {
                inventory.removeItem(stack.item, stack.intCount());
            }
            float accumulator = 0;
            for(var stack : out) {
                accumulator += inventory.pushItem(stack.item, stack.intCount()).left;
            }
            return accumulator;
        }
        return -1;
    }

    public boolean canCraft(Inventory inventory) {
        for(var stack : in) {
            if(inventory.amountOf(stack.item) < stack.intCount()) {
                return false;
            }
        }

        return true;
    }

    public boolean requiresLiquids() {
        return in.contains(stack -> stack.item.is(ContentType.liquid));
    }

    public boolean producingLiquids() {
        return out.contains(stack -> stack.item.is(ContentType.liquid));
    }

    public Recipe out(SeItemStack... stacks) {
        this.out.addAll(stacks);
        return this;
    }

    public Recipe in(SeItemStack... stacks) {
        this.in.addAll(stacks);
        return this;
    }

    public Recipe setCraftTime(int craftTime) {
        this.craftTime = craftTime;
        return this;
    }

    public Recipe setEnergyConsuming(float consuming) {
        this.energyConsuming = consuming;
        return this;
    }

    public Recipe setCanCraftedByHand(boolean canCraftedByHand) {
        this.canCraftedByHand = canCraftedByHand;
        return this;
    }

    public boolean isCanCraftedByHand() {
        if(requiresLiquids() || producingLiquids()) {
            return false;
        }

        return canCraftedByHand;
    }
}