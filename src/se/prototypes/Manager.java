package se.prototypes;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.util.Reflect;

import mindustry.core.ContentLoader;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;

import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.*;

public class Manager {
    public static MappableContent[][] getTemporaryMapper() {
        return Reflect.get(ContentLoader.class, content, "temporaryMapper");
    }

    public static ObjectMap<String, MappableContent>[] getContentMap() {
        return Reflect.get(ContentLoader.class, content, "contentNameMap");
    }

    public static MappableContent replaceWithSameName(@NotNull MappableContent item, @NotNull Prov<MappableContent> replacementProv) {
        getContentMap()[item.getContentType().ordinal()].remove(item.name);
        var x = replacementProv.get();
        replace(item, x);
        x.init();
        x.load();
        x.loadIcon();
        return x;
    }

    public static void replace(@NotNull Content item, Content replacement) {
        var contentType = item.getContentType();
        var stack = content.getBy(contentType);

        if(item instanceof MappableContent mappableA && replacement instanceof MappableContent mappableB) {
            getContentMap()[contentType.ordinal()].put(mappableA.name, mappableB);
            var mapper = getTemporaryMapper();

            if(mapper != null) {
                mapper[contentType.ordinal()][item.id] = mappableB;
                content.setTemporaryMapper(mapper);
            }
        }

        int i = stack.indexOf(item, true);
        if(i != -1) {
            stack.set(i, replacement);
        }

        int j = stack.lastIndexOf(replacement, true);
        if(j != -1 && j != i) {
            stack.remove(j);
        }

        replacement.id = item.id;
    }
}