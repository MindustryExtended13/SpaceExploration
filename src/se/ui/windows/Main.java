package se.ui.windows;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Cons3;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;

import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.ui.Styles;

import org.jetbrains.annotations.NotNull;

import se.ServerIntegration;
import se.SpaceExploration;
import se.event.PlayerPacketCallbackResult;
import se.graphics.TextDraw2;
import se.prototypes.item.Hidden;
import se.prototypes.slot.Inventory;
import se.prototypes.slot.Slot;
import se.prototypes.slot.SlotItem;
import se.ui.layout.SlotImage;
import se.ui.window.Window;
import se.ui.window.WindowWarning;
import se.util.EventState;

public class Main extends Window implements ApplicationListener {
    public static final Cons3<Main, Button, Button> def = (window, ignored, self) -> {
        window.btnScrollX = 0;
        window.btnScrollY = 0;
        window.selected = self;
        window.rebuild();
    };

    public static final Seq<CraftingCategory> categories = new Seq<>();
    public static final Seq<Button> buttons = new Seq<>();
    public static final Slot selectedSlot = new Slot();

    public static @NotNull CraftingCategory addCategory(String name, UnlockableContent icon) {
        CraftingCategory category = new CraftingCategory();
        category.icon = icon == null ? Hidden.err : icon;
        category.name = name;
        categories.add(category);
        return category;
    }

    public static @NotNull Button addButton(String text, Cons3<Main, Button, Button> cons, Cons<Main> builder) {
        Button button = new Button();
        button.name = text;
        button.cons = cons;
        button.builder = builder;
        buttons.add(button);
        return button;
    }

    public static void buildCrafting(Main main) {
    }

    public float scrollX;
    public float scrollY;
    public float btnScrollX;
    public float btnScrollY;
    public Inventory accessor;
    public Table buttonsCont;
    public Button selected;

    private boolean _await_135913;
    private int _await_599208;

    public static boolean picked(SlotItem item) {
        return selectedSlot.item != null && (item == null || item.equals(selectedSlot.item));
    }

    public void unfocus() {
        Core.scene.unfocus(this);
    }

    @Override
    public void updateWindow() {
        if(!Vars.state.isPlaying() || !SpaceExploration.inited()) {
            return;
        }

        boolean rebuild = false;
        var n = SpaceExploration.getInventory();
        if(n != accessor) {
            rebuild = true;
        }
        accessor = n;

        var stack = Vars.player.unit().stack();
        if(stack != null && stack.amount > 0 && !_await_135913 && _await_599208 <= 0) {
            accessor.pushItem(new SlotItem(stack.item), stack.amount);
            ServerIntegration.inventorySync();

            if(ServerIntegration.host()) {
                Vars.player.unit().stack().amount = 0;
            } else {
                Call.serverPacketReliable("SeItemDrop", null);
                _await_135913 = true;
            }

            _await_599208 = 60;
        }

        if(_await_599208 > 0) {
            _await_599208--;
        }

        if(rebuild) {
            rebuild();
        }
    }

    @Override
    public void onOpened(EventState state) {
        super.onOpened(state);

        if(state.post()) {
            mallocWarning(WindowWarning.FOCUSABLE);
        }
    }

    @Override
    public void onBuild(EventState state) {
        super.onBuild(state);

        if(state.pre()) {
            Events.run(EventType.WorldLoadEndEvent.class, this::rebuild);

            Events.on(PlayerPacketCallbackResult.class, e -> {
                if(e.packet().equals("SeItemDrop")) {
                    _await_135913 = false;
                }
            });
        }

        if(state.post()) {
            footer.update(() -> Core.scene.unfocus(footer));
            footer.clicked(this::unfocus);
        }
    }

    @Override
    public void rebuild() {
        if(accessor == null) {
            return;
        }

        float fixedWidth = Math.max(width / 2, 500);
        contentRoot.clearChildren();

        contentRoot.table(header -> {
            header.table(title -> {
                title.left();
                title.setBackground(Tex.pane);
                title.add("Inventory");
            }).width(fixedWidth).growY();

            var tmp = new ScrollPane[1];
            header.table(buttonsPane -> {
                buttonsPane.setBackground(Tex.pane);
                tmp[0] = buttonsPane.pane(Styles.noBarPane, buttons -> {
                    buttons.defaults().size(200, 30);
                    boolean x = false;

                    for(Button button : Main.buttons) {
                        button.onBuild(buttons.button(button.name, Styles.cleart, () -> {
                            button.cons.get(this, selected, button);
                        }).padLeft(x ? 12 : 0));

                        x = true;
                    }
                }).grow().get();
            }).grow();

            tmp[0].setScrollingDisabledY(true);
            tmp[0].setOverscroll(false, false);

            Core.app.post(() -> {
                tmp[0].setScrollX(scrollX);
                tmp[0].updateVisualScroll();

                tmp[0].update(() -> {
                    scrollX = tmp[0].getScrollX();
                });
            });
        }).growX().left().row();

        contentRoot.table(data -> {
            int count = (int) (fixedWidth / 60);

            var pane = data.pane(inv -> {
                inv.setBackground(Tex.pane);
                inv.top().left();

                for(int i = 0; i < accessor.length(); i++) {
                    var slot = accessor.getAt(i);
                    var elem = inv.add(new SlotImage(slot, Color.darkGray)).pad(6).size(48).get();

                    elem.clicked(KeyCode.mouseRight, () -> {
                        if(!picked(null)) {
                            if(slot.count == 1) {
                                selectedSlot.pick(slot);
                            } else {
                                selectedSlot.overdrive = slot.overdrive;
                                selectedSlot.item = slot.item;

                                int x = slot.count % 2;
                                int c = Mathf.floor(slot.count / 2f);

                                selectedSlot.count = c;
                                slot.count = c + x;
                            }
                        } else if(picked(slot.item) && (slot.item == null || slot.count < slot.maxStackSize())) {
                            if(selectedSlot.count == 1) {
                                selectedSlot.item = null;
                                selectedSlot.count = 0;
                            } else {
                                selectedSlot.count--;
                            }

                            if(slot.item == null) {
                                slot.item = selectedSlot.item;
                                slot.count = 1;
                            } else {
                                slot.count++;
                            }
                        }

                        ServerIntegration.inventorySync();
                    });

                    elem.clicked(() -> {
                        if(picked(slot.item)) {
                            selectedSlot.drop(slot);
                        } else if(slot.item != null) {
                            if(picked(null)) {
                                Slot.tmp1.item = slot.item;
                                Slot.tmp1.count = slot.count;
                                Slot.tmp1.overdrive = slot.overdrive;

                                slot.item = selectedSlot.item;
                                slot.count = selectedSlot.count;
                                slot.overdrive = selectedSlot.overdrive;

                                selectedSlot.item = Slot.tmp1.item;
                                selectedSlot.count = Slot.tmp1.count;
                                selectedSlot.overdrive = Slot.tmp1.overdrive;
                            } else {
                                selectedSlot.pick(slot);
                            }
                        }

                        ServerIntegration.inventorySync();
                    });

                    if(i % count == count - 1) {
                        inv.row();
                    }
                }
            }).width(fixedWidth).growY().get();

            pane.setScrollingDisabledX(true);
            pane.setOverscroll(false, false);
            pane.setFadeScrollBars(true);
            pane.setupFadeScrollBars(1, 0.25f);

            var pane2 = data.pane(cont -> {
                cont.setBackground(Tex.pane);
                buttonsCont = cont;

                if(selected != null) {
                    selected.builder.get(this);
                }
            }).grow().get();

            pane2.setOverscroll(false, false);

            Core.app.post(() -> {
                pane.setScrollPercentY(scrollY);
                pane.updateVisualScroll();

                pane.update(() -> {
                    scrollY = pane.getScrollY();
                });

                pane2.setScrollX(btnScrollX);
                pane2.setScrollY(btnScrollY);
                pane2.updateVisualScroll();

                pane2.update(() -> {
                    btnScrollX = pane2.getScrollX();
                    btnScrollY = pane2.getScrollY();
                });
            });
        }).grow();
    }

    @Override
    public String getTitle() {
        return Vars.player.plainName();
    }

    @Override
    public float minWidth() {
        return 950;
    }

    @Override
    public float minHeight() {
        return 600;
    }

    @Override
    public void update() {
        if(picked(null)) {
            var camera = Core.scene.getViewport().getCamera();
            Draw.proj(camera);

            Draw.draw(Layer.max, () -> {
                if(selectedSlot != null && selectedSlot.item != null) {
                    var pos = camera.unproject(Core.input.mouse());
                    var icon = selectedSlot.item.get().uiIcon;
                    Draw.rect(icon, pos.x, pos.y, icon.width, icon.height);
                    var size = TextDraw2.size(selectedSlot.count, 0);
                    TextDraw2.TEXT_SCALE *= 3;
                    TextDraw2.text(icon.width / 2f + pos.x + size.x / 2 + 8, pos.y, 0, Color.white, selectedSlot.count);
                    TextDraw2.TEXT_SCALE /= 3;
                }

            });

            Draw.flush();
        }
    }

    public static class Button {
        public Cons3<Main, Button, Button> cons;
        public Cons<Main> builder;
        public String name;

        public void onBuild(Cell<TextButton> cell) {}
    }

    public static class CraftingCategory {
        public UnlockableContent icon;
        public String name;
    }
}
