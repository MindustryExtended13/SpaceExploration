package se.content;

import mindustry.core.ContentLoader;
import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;

public class HiddenWorkContentLoader extends ContentLoader {
    protected boolean noAddEvent = false;

    public void setNoAddMode(boolean noAddEvent) {
        this.noAddEvent = noAddEvent;
    }

    public boolean isNoAddEvent() {
        return noAddEvent;
    }

    @Override
    public void handleContent(Content content) {
        if(!isNoAddEvent()) super.handleContent(content);
    }

    @Override
    public void handleMappableContent(MappableContent content) {
        if(!isNoAddEvent()) super.handleMappableContent(content);
    }
}