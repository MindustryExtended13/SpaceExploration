package se.prototypes.slot;

import arc.util.io.Reads;
import arc.util.io.Writes;

import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.world.Block;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.*;

public class SlotItem
{
    private int block_id;
    private int item_id;

    //FOR JSON
    @Contract(pure = true)
    protected SlotItem() {
    }

    @Contract(pure = true)
    public SlotItem(Item item)
    {
        this.block_id = -1;
        this.item_id = item == null ? -1 : item.id;
    }

    @Contract(pure = true)
    public SlotItem(Block block)
    {
        this.block_id = block == null ? -1 : block.id;
        this.item_id = -1;
    }

    public UnlockableContent get()
    {
        return block_id == -1 ? content.item(item_id) : content.block(block_id);
    }

    public boolean isItem()
    {
        return block_id == -1;
    }

    public void set(UnlockableContent content)
    {
        if(content instanceof Item it)
        {
            item_id = it.id;
        }
        else if(content instanceof Block bl)
        {
            block_id = bl.id;
        }
    }

    public boolean equals(SlotItem other)
    {
        return other != null && other.item_id == item_id && other.block_id == block_id;
    }

    public void read(@NotNull Reads reads)
    {
        String name = reads.str();

        if(name == null)
        {
            block_id = -1;
            item_id = -1;
        }
        else if(reads.bool())
        {
            item_id = content.item(name).id;
            block_id = -1;
        }
        else
        {
            block_id = content.block(name).id;
            item_id = -1;
        }
    }

    public void write(@NotNull Writes writes)
    {
        var get = get();
        writes.str(get == null ? null : get.name);
        writes.bool(isItem());
    }

    @Override
    public String toString()
    {
        var get = get();
        return get == null ? "null" : get.localizedName;
    }
}