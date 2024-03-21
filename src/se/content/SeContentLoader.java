package se.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Reflect;

import mindustry.Vars;
import mindustry.core.ContentLoader;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.mod.Mods.LoadedMod;

import se.prototypes.Manager;
import se.util.Tables;

import java.lang.reflect.Modifier;

import static se.SpaceExploration.*;

public class SeContentLoader extends HiddenWorkContentLoader {
    public final Seq<SeContentMixin> mixins = new Seq<>();
    protected LoadedMod currentMod;
    protected Content tmp;

    protected Content update(Content c) {
        if(c instanceof MappableContent m) {
            tmp = mixins.reduce(m, (mixin, x) -> mixin.mappableTransformation(x, getCurrentMod()));
        } else {
            tmp = mixins.reduce(c, (mixin, x) -> mixin.contentTransformation(x, getCurrentMod()));
        }

        overrideSetter(tmp);
        return tmp;
    }

    public void fixFields(Object content) {
        if(content == null) return;
        Tables.hiddenWork(() -> Tables.deepEdit(content.getClass(), (field, ignored) -> {try {
            int modifiers = field.getModifiers();
            if(!Modifier.isFinal(modifiers)) {
                Object x = Modifier.isStatic(modifiers) ? null : content;
                var value = field.get(x);

                Object $ = mixins.reduce(value, (mixin, c) -> mixin.handleField(field, c, content));
                if($ != value) field.set(x, $);
            }
        } catch(Throwable e) {
            LOGGERE.log("Error SE5201: {}", Tables.toString(e));
        }}));
    }

    public void loadFrom() {
        var old = Vars.content;
        var map = old.getContentMap();
        var map2 = Manager.getContentMap();
        var mapper = Manager.getTemporaryMapper();

        for(int i = 0; i < map.length; i++) {
            map[i] = map[i].map(this::update);
        }

        for(int i = 0; i< map2.length; i++) {
            var m = map2[i];
            map2[i] = new ObjectMap<>();
            for(var x : m) {
                map2[i].put(x.key, (MappableContent) update(x.value));
            }
        }

        if(mapper != null) {
            for(int i = 0; i < mapper.length; i++) {
                for(int j = 0; j < mapper[i].length; j++) {
                    mapper[i][j] = (MappableContent) update(mapper[i][j]);
                }
            }
        }

        Reflect.set(ContentLoader.class, this, "contentMap", map);
        Reflect.set(ContentLoader.class, this, "contentNameMap", map2);
        setTemporaryMapper(mapper);

        setCurrentMod(Reflect.get(ContentLoader.class, old, "currentMod"));
        Reflect.set(ContentLoader.class, this, "initialization", Reflect.get(ContentLoader.class, old, "initialization"));
    }

    public LoadedMod getCurrentMod() {
        return currentMod;
    }

    public void overrideSetter(Content content) {
        mixins.each(mixin -> mixin.overrideContentSetter(content, getCurrentMod()));
    }

    @Override
    public <T extends Content> T getByID(ContentType type, int id) {
        if(id < 0) LOGGERW.log("Getting by id that smaller than 0");
        return super.getByID(type, id);
    }

    @Override
    public void logContent() {
        Seq<Content>[] var1 = getContentMap();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Seq<Content> arr = var1[var3];

            for(int i = 0; i < arr.size; ++i) {
                int id = arr.get(i).id;
                if (id != i) {
                    LOGGERE.log("Error SE5678: {} (expected {} but got {})", arr.get(i), i, id);
                }
            }
        }

        LOGGER.log("--- CONTENT INFO ---");

        for(int k = 0; k < var2; ++k) {
            LOGGER.log("[{}]: loaded {}", ContentType.all[k].name(), var1[k].size);
        }

        LOGGER.log("Total content loaded: {}", Seq.with(ContentType.all).mapInt((c) -> var1[c.ordinal()].size).sum());
        LOGGER.log("--------------------");
    }

    @Override
    public void setCurrentMod(LoadedMod mod) {
        super.setCurrentMod(mod);
        currentMod = mod;
    }

    @Override
    public String transformName(String name) {
        return mixins.reduce(super.transformName(name), (mixin, x) -> mixin.transformName(x, name, getCurrentMod()));
    }

    @Override
    public void handleContent(Content content) {
        if(isNoAddEvent()) {
            return;
        }

        super.handleContent(tmp = mixins.reduce(content, (mixin, x) -> mixin.contentTransformation(x, getCurrentMod())));
        overrideSetter(tmp);
        fixFields(tmp);
    }

    @Override
    public void handleMappableContent(MappableContent content) {
        if(isNoAddEvent()) {
            return;
        }

        super.handleMappableContent((MappableContent) (tmp = mixins.reduce(content, (mixin, x) -> mixin.mappableTransformation(x, getCurrentMod()))));
        overrideSetter(tmp);
        fixFields(tmp);
    }
}