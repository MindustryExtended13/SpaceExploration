package se.prototypes.recipe;

import arc.math.Mathf;
import arc.struct.Seq;

import mindustry.ctype.ContentType;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.distribution.Duct;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.OverlayFloor;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.BuildVisibility;

import se.prototypes.item.Hidden;
import se.prototypes.item.SeItemStack;
import se.prototypes.slot.Inventory;
import se.prototypes.slot.SlotItem;
import se.ui.windows.Main;
import se.ui.windows.Main.CraftingCategory;
import se.util.Tables;

public class Recipe {
    public Seq<SeItemStack> in  = new Seq<>();
    public Seq<SeItemStack> out = new Seq<>();
    public CraftingCategory category = null;
    protected boolean canCraftedByHand;
    public float energyConsuming = 1;
    public int craftTime = 60;
    public SlotItem icon;
    public int row;

    public static int getRowFor(Block b) {
        if(b instanceof Floor || Main.military(b)) return 0;

        if(
                b.buildVisibility == BuildVisibility.hidden ||
                b.buildVisibility == BuildVisibility.sandboxOnly ||
                b.buildVisibility == BuildVisibility.editorOnly) {
            return 9;
        } else if(b.category == Category.production) {
            return 0;
        } else if(b instanceof Conveyor || b instanceof StackConveyor || b instanceof Duct || b instanceof Router) {
            return 1;
        } else if(b instanceof Conduit || b instanceof LiquidRouter) {
            return 2;
        } else if(b.category == Category.distribution) {
            return 3;
        } else if(b.category == Category.liquid) {
            return 4;
        } else if(b instanceof PowerNode || b instanceof Battery) {
            return 5;
        } else if(b instanceof GenericCrafter || b.category == Category.power) {
            return 6;
        } else if(b.category == Category.logic) {
            return 7;
        } else {
            return 8;
        }
    }

    public static CraftingCategory getCategoryFor(Block b) {
        if((b.buildVisibility == BuildVisibility.hidden || b instanceof OreBlock || b instanceof OverlayFloor)
                && Tables.getTableType(b) != Floor.class) {
            return null;
        }

        if(b instanceof Floor) {
            return Main.landfill;
        }

        return Main.military(b) ? Main.military : Main.transportation;
    }

    public static Recipe create(Block block) {
        return new Recipe()
                .in(SeItemStack.create(block.requirements).toArray(SeItemStack.class))
                .out(new SeItemStack(block, 1))
                .setEnergyConsuming(2)
                .setCraftTime(block.size * 60)
                .setCanCraftedByHand(block.size <= 3)
                .category(getCategoryFor(block))
                .row(getRowFor(block))
                .icon(new SlotItem(block));
    }

    public float getCraftCount(Inventory inv) {
        if(!canCraft(inv)) return 0;
        Seq<Integer> xI = new Seq<>();

        for(var stack : in) {
            xI.add(Mathf.floor(inv.amountOf(stack.item) / stack.count));
        }

        if(xI.isEmpty()) return Float.POSITIVE_INFINITY;
        return xI.min(i -> i);
    }

    public float output(Inventory inventory) {
        float accumulator = 0;
        for(var stack : out) {
            accumulator += inventory.pushItem(stack.item, stack.intCount()).left;
        }
        return accumulator;
    }

    public float craft(Inventory inventory) {
        if(canCraft(inventory)) {
            for(var stack : in) {
                inventory.removeItem(stack.item, stack.intCount());
            }
            return output(inventory);
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

    public SlotItem getIcon() {
        if(icon == null) {
            var err = new SlotItem(Hidden.err);
            if(out == null || out.isEmpty()) {
                return err;
            }

            var x = out.get(0).item;
            return x == null ? err : x;
        }
        return icon;
    }

    public Recipe icon(SlotItem icon) {
        this.icon = icon;
        return this;
    }

    public Recipe row(int row) {
        this.row = Math.max(row, 0);
        return this;
    }

    public Recipe category(CraftingCategory category) {
        this.category = category;
        return this;
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