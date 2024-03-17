package se.content;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Reflect;

import mindustry.Vars;
import mindustry.core.ContentLoader;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mindustry.mod.Mods.LoadedMod;

import se.SpaceExploration;
import se.prototypes.Manager;
import se.util.Tables;

import java.lang.reflect.Modifier;

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

    public void fixFields(Content content) {
        if(content == null) return;
        Tables.deepEdit(content.getClass(), (field, ignored) -> {try {
            int modifiers = field.getModifiers();
            if(!Modifier.isFinal(modifiers)) {
                Object x = Modifier.isStatic(modifiers) ? null : content;

                var value = field.get(x);
                if(value instanceof MappableContent cnt) {
                    //I don't why need this for mappable but anyway
                    field.set(x, getByName(cnt.getContentType(), cnt.name));
                } if(value instanceof Content cnt) {
                    field.set(x, getByID(cnt.getContentType(), cnt.id));
                }
            }
        } catch(Throwable e) {
            SpaceExploration.LOGGERE.log("Error SE5201: {}", Tables.toString(e));
        }});
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