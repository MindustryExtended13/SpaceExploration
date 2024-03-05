package se.ui.window;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;

import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;

import se.SpaceExploration;
import se.prototypes.item.Hidden;
import se.ui.UIUtil;
import se.util.EventState;
import se.ui.window.WindowListeners.*;
import se.ui.window.WindowWarning.WindowWarningCell;

import static arc.Core.*;

public class Window extends Table
{
    public static final float BASE_HEIGHT_DEFAULT = 125;
    public Seq<WindowWarningCell> warnings;
    public final ScaleInputListener scale;
    public final DragHandleListener mover;

    public boolean rebuildOnStateChanged;
    public boolean closable;
    public boolean rolledUp;
    public boolean resizing;
    public boolean moving;

    public Table contentRoot;
    public Table warningBox;
    public Table content;
    public Table footer;
    public Table header;

    {
        scale = new ScaleInputListener(this);
        mover = new DragHandleListener(this);
        rebuildOnStateChanged = true;
        warnings = new Seq<>();
        closable = true;
    }

    public Window()
    {
        super();
        onBuild(EventState.PRE);

        visible(this::opened);
        update(this::updateWindow);

        table(header ->
        {
            this.header = header;
            header.addListener(mover);
            header.setBackground(Tex.buttonEdge3);

            if(closable)
            {
                header.button(Icon.cancelSmall, Styles.clearNonei, this::close).size(24).padRight(6f);
            }

            header.button(Icon.cancelSmall, Styles.clearNonei, this::roll).update(button ->
            {
                button.getStyle().imageUp = SpaceExploration.getDrawableFor(rolledUp ? Hidden.minus : Hidden.plus);
            }).size(24).padRight(3f);

            UIUtil.label(header, this::getTitle).grow();

            header.pane(warning2 ->
            {
                warning2.right().defaults().padRight(10);
                warningBox = warning2;
            }).grow().scrollY(false).get();
        }).growX().height(48).row();

        table(content ->
        {
            this.contentRoot = content;
            content.pane(Styles.noBarPane, paneContent ->
            {
                this.content = paneContent;
                paneContent.left().top();
            }).grow();
        }).visible(this::deployed).grow().row();

        table(footer ->
        {
            this.footer = footer;
            footer.setBackground(Tex.pane2);

            footer.pane(Styles.noBarPane, info ->
            {
                info.left();
                UIUtil.label(info, () -> new StringBuilder()
                        .append("[gray]#[")
                        .append(Mathf.floor(x))
                        .append(", ")
                        .append(Mathf.floor(y))
                        .append("]@[")
                        .append(Mathf.floor(getWidth()))
                        .append(", ")
                        .append(Mathf.floor(getHeight()))
                        .append("]"))
                        .left();
            }).grow();

            if(minWidth() != maxWidth() || minHeight() != maxHeight())
            {
                footer.button(Icon.resizeSmall, Styles.clearNonei, () -> {})
                        .padLeft(6)
                        .get()
                        .addListener(scale);
            }
        }).visible(this::deployed).height(48).growX();

        onBuild(EventState.POST);
    }

    public boolean contains(WindowWarning warning)
    {
        return warning != null && warnings.contains(x -> warning.equals(x.owner));
    }

    public void rebuildWarnings()
    {
        warningBox.clear();
        Seq<WindowWarningCell> replacement = new Seq<>();

        for(var cell : warnings)
        {
            replacement.add(cell.owner.build(warningBox, this));
        }

        warnings.clear();
        warnings.addAll(replacement);
    }

    public void mallocWarning(WindowWarning warning)
    {
        if(contains(warning))
        {
            rmWarning(warning);
        }

        pushWarning(warning);
    }

    public void pushWarning(WindowWarning warning)
    {
        if(warning != null)
        {
            warnings.add(warning.build(warningBox, this));
        }
    }

    public void rmWarning(WindowWarning warning)
    {
        if(warning == null)
        {
            return;
        }

        var found = warnings.find(cell ->
        {
            return warning.equals(cell.owner);
        });

        if(found != null)
        {
            warnings.remove(found, false);
            rebuildWarnings();
        }
    }

    public void onBuild(EventState state)
    {
        if(state != null && state.post())
        {
            rebuild();
        }
    }

    public void move(float x, float y)
    {
        onMoved(x, y, EventState.PRE);
        setPosition(x, y);
        invalidateHierarchy();
        onMoved(x, y, EventState.POST);
    }

    public void resize(float width, float height)
    {
        onResize(width, height, EventState.PRE);
        setSize(width, height);
        invalidateHierarchy();
        onResize(width, height, EventState.POST);
    }

    public void updateWindow()
    {
        scene.unfocus(this);
    }

    public void rebuild()
    {
    }

    public void open()
    {
        WindowStack.open(this);
    }

    public void close()
    {
        WindowStack.close(this);
    }

    public void roll()
    {
        if(rolledUp)
        {
            deploy();
        }
        else
        {
            rollUp();
        }
    }

    public void deploy()
    {
        onDeployed(EventState.PRE);
        rolledUp = false;
        onDeployed(EventState.POST);
    }

    public void rollUp()
    {
        onRolledUp(EventState.PRE);
        rolledUp = true;
        onRolledUp(EventState.POST);
    }

    public void onDeployed(EventState state)
    {
        if(state != null && state.post())
        {
            header.setBackground(Tex.buttonEdge3);

            if(rebuildOnStateChanged)
            {
                rebuild();
            }
        }
    }

    public void onRolledUp(EventState state)
    {
        if(state != null && state.post())
        {
            header.setBackground(Tex.pane);

            if(rebuildOnStateChanged)
            {
                rebuild();
            }
        }
    }

    public boolean deployed()
    {
        return !rolledUp;
    }

    public boolean opened()
    {
        return WindowStack.opened(this);
    }

    public void onOpened(EventState state)
    {
        if(rebuildOnStateChanged && state.post())
        {
            rebuild();
        }
    }

    public void onClosed(EventState state)
    {
        if(rebuildOnStateChanged && state.post())
        {
            rebuild();
        }
    }

    public void onMoved(float newX, float newY, EventState state)
    {
        if(rebuildOnStateChanged && state.post())
        {
            rebuild();
        }
    }

    public void onResize(float newWidth, float newHeight, EventState state) {
        if(rebuildOnStateChanged && state.post())
        {
            rebuild();
        }
    }

    public String getTitle()
    {
        return getClass().getSimpleName();
    }

    public float minWidth()
    {
        return 200;
    }

    public float maxWidth()
    {
        return Float.POSITIVE_INFINITY;
    }

    public float minHeight()
    {
        return BASE_HEIGHT_DEFAULT;
    }

    public float maxHeight()
    {
        return Float.POSITIVE_INFINITY;
    }
}