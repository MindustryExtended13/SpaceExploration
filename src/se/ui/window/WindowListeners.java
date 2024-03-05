package se.ui.window;

import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.util.Tmp;

public class WindowListeners
{
    public static class ScaleInputListener extends TouchPosInputListener
    {
        public ScaleInputListener(Window window)
        {
            super(window);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button)
        {
            super.touchUp(event, x, y, pointer, button);
            window.resize(window.getWidth(), window.getHeight());
            window.resizing = false;
        }

        @Override
        public void touchDragged(InputEvent event, float dx, float dy, int pointer)
        {
            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(dx, dy));
            float w = v.x - lastX;
            float h = v.y - lastY;

            // will soft-lock if initial size is smaller than minimum
            // so don't do that!
            float ww = window.getWidth();
            float wh = window.getHeight();
            if(ww + w < window.minWidth()  || ww + w > window.maxWidth())  w = 0;
            if(wh - h < window.minHeight() || wh - h > window.maxHeight()) h = 0;
            window.sizeBy(w, -h);
            window.moveBy(0, h);
            lastX = v.x;
            lastY = v.y;

            if(window.rebuildOnStateChanged)
            {
                window.rebuild();
            }

            window.resizing = true;
        }
    }

    public static class DragHandleListener extends TouchPosInputListener
    {
        public DragHandleListener(Window window)
        {
            super(window);
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button)
        {
            super.touchUp(event, x, y, pointer, button);
            window.moving = false;
        }

        @Override
        public void touchDragged(InputEvent event, float dx, float dy, int pointer)
        {
            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(dx, dy));
            window.move(window.x + (v.x - lastX), window.y + (v.y - lastY));
            lastX = v.x;
            lastY = v.y;
            window.moving = true;
        }
    }

    public static class TouchPosInputListener extends InputListener
    {
        protected float lastX, lastY;
        protected Window window;

        public TouchPosInputListener(Window window)
        {
            this.window = window;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button)
        {
            if(event == null)
            {
                return true;
            }

            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(x, y));
            lastX = v.x;
            lastY = v.y;
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float dx, float dy, int pointer)
        {
            if(event == null)
            {
                return;
            }

            Vec2 v = event.listenerActor.localToStageCoordinates(Tmp.v1.set(dx, dy));
            lastX = v.x;
            lastY = v.y;
        }
    }
}