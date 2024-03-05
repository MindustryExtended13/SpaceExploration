package se.ui.window;

import arc.struct.Seq;

import se.util.EventState;

import static arc.Core.*;
import static mindustry.Vars.*;

public class WindowStack
{
    public static final Seq<Window> opened = new Seq<>();

    public static boolean opened(Window window)
    {
        return (state.isPlaying() || state.isPaused()) && opened.contains(window, true);
    }

    public static void open(Window window)
    {
        if(window == null || opened(window))
        {
            return;
        }

        window.onOpened(EventState.PRE);
        opened.add(window);
        window.onOpened(EventState.POST);
    }

    public static void close(Window window)
    {
        if(window == null)
        {
            return;
        }

        window.onClosed(EventState.PRE);
        opened.remove(window, true);
        window.onClosed(EventState.POST);
    }

    public static void push(Window window)
    {
        if(window == null)
        {
            return;
        }

        scene.add(window);
        float w = window.minWidth();
        float h = window.minHeight();
        window.resize(w, h);
        window.move(scene.getWidth() / 2 - w / 2, scene.getHeight() / 2 - h / 2);
    }
}