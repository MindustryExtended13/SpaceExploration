package se.prototypes.slot;

import arc.util.io.Reads;
import arc.util.io.Writes;

import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.*;

public class SlotItem {
    private int content_row;
    private int content_id;

    //FOR JSON
    @Contract(pure = true)
    protected SlotItem() {
    }

    @Contract(pure = true)
    public SlotItem(UnlockableContent content) {
        set(content);
    }

    public UnlockableContent get() {
        return content.getByID(ContentType.values()[content_row], content_id);
    }

    public boolean is(ContentType type) {
        return content_row == type.ordinal();
    }

    public boolean isBlock() {
        return is(ContentType.block);
    }

    public boolean isItem() {
        return is(ContentType.item);
    }

    public void set(UnlockableContent content) {
        if(content == null) {
            content_row = content_id = -1;
        } else {
            content_row = content.getContentType().ordinal();
            content_id = content.id;
        }
    }

    public boolean equals(SlotItem other) {
        return other != null && other.content_row == content_row && other.content_id == content_id;
    }

    public void read(@NotNull Reads reads) {
        String name = reads.str();
        int x = reads.i();

        if(name == null) {
            content_row = -1;
            content_id = -1;
        } else {
            content_row = x;
            content_id = content.getByName(ContentType.values()[x], name).id;
        }
    }

    public void write(@NotNull Writes writes) {
        var get = get();
        writes.str(get == null ? null : get.name);
        writes.i(content_row);
    }

    @Override
    public String toString() {
        var get = get();
        return get == null ? "null" : get.localizedName;
    }
}