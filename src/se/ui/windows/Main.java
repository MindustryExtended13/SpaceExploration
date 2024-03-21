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
import arc.scene.style.TextureRegionDrawable;
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
import mindustry.type.Category;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.distribution.Duct;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.BuildVisibility;

import org.jetbrains.annotations.Contract;
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
import se.util.Tables;

import static mindustry.Vars.*;

public class Main extends Window implements ApplicationListener {
    public static final Cons3<Main, Button, Button> def = (window, ignored, self) -> {
        window.selected = self;
        window.rebuild();
    };

    public static final Seq<CraftingCategory> categories = new Seq<>();
    public static final Seq<Button> buttons = new Seq<>();
    public static final Slot selectedSlot = new Slot();
    public static CraftingCategory selectedCategory;

    public static CraftingCategory transportation;
    public static CraftingCategory components;
    public static CraftingCategory military;
    public static CraftingCategory landfill;

    @Contract(pure = true)
    public static boolean military(@NotNull Block content) {
        return content.category == Category.units || content.category == Category.turret || content.category == Category.defense;
    }

    public static void load() {
        transportation.content.add(new Seq<>(), new Seq<>(), new Seq<>(), new Seq<>());
        transportation.content.add(new Seq<>(), new Seq<>(), new Seq<>(), new Seq<>());
        transportation.content.add(new Seq<>(), new Seq<>(), new Seq<>(), new Seq<>());
        landfill.content.add(new Seq<>(), new Seq<>());
        var content = Vars.content;

        content.blocks().each(b -> {
            if((b.buildVisibility == BuildVisibility.hidden || b instanceof OreBlock || b instanceof OverlayFloor)
                    && Tables.getTableType(b) != Floor.class) {
                return;
            }

            if(b instanceof Floor) {
                landfill.content.get(0).add(b);
            } else if(!military(b)) {
                if(b.buildVisibility == BuildVisibility.sandboxOnly) {
                    transportation.content.get(9).add(b);
                } else if(b.category == Category.production) {
                    transportation.content.get(0).add(b);
                } else if(b instanceof Conveyor || b instanceof StackConveyor || b instanceof Duct || b instanceof Router) {
                    transportation.content.get(1).add(b);
                } else if(b instanceof Conduit || b instanceof LiquidRouter) {
                    transportation.content.get(2).add(b);
                } else if(b.category == Category.distribution) {
                    transportation.content.get(3).add(b);
                } else if(b.category == Category.liquid) {
                    transportation.content.get(4).add(b);
                } else if(b instanceof PowerNode || b instanceof Battery) {
                    transportation.content.get(5).add(b);
                } else if(b instanceof GenericCrafter || b.category == Category.power) {
                    transportation.content.get(6).add(b);
                } else if(b.category == Category.logic) {
                    transportation.content.get(7).add(b);
                } else {
                    transportation.content.get(8).add(b);
                }
            }
        });
    }

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

    public static void buildCrafting(@NotNull Main main) {
        float fixedWidth = Math.max(main.width / 2, 500);
        int cells = Mathf.floor((main.width - fixedWidth) / 60);

        if(cells <= 0) {
            cells = 1;
        }

        final int finalCells = cells;
        main.buttonsCont.table(t -> {
            t.top().left().defaults().pad(0).margin(0);
            t.margin(0);
            var p = t.pane(cater -> {
                cater.defaults().growY().pad(0).margin(0).width(100);
                for(var category : categories) {
                    cater.button(new TextureRegionDrawable(category.icon.uiIcon), Styles.clearNonei, () -> {
                        selectedCategory = category;
                        main.rebuild();
                    }).checked(selectedCategory == category);
                }
            }).height(50).get();
            p.setOverscroll(false, false);
            p.setScrollingDisabled(false, true);
            t.row();
            var x = t.pane(cont -> {
                cont.top().left();
                for(var row : selectedCategory.content) {
                    cont.table(list -> {
                        list.left();
                        int j = 0;
                        for(var item : row) {
                            var s = new Slot();
                            s.stack.count = 1;
                            s.stack.item = new SlotItem(item);
                            s.overdrive.stackSize = Float.POSITIVE_INFINITY;
                            list.add(new SlotImage(s, Color.darkGray, true)).size(48).pad(6);

                            if(j++ % finalCells == finalCells - 1) {
                                list.row();
                            }
                        }
                    }).growX().row();
                }
            }).grow().get();
            x.setFadeScrollBars(true);
            x.setOverscroll(false, false);
            x.setScrollingDisabled(true, false);
            x.setupFadeScrollBars(1, 0.25f);
        }).grow();
    }

    public float scrollX;
    public float scrollY;
    public Inventory accessor;
    public Table buttonsCont;
    public Button selected;

    private boolean _await_135913;
    private int _await_599208;

    public static boolean picked(SlotItem item) {
        return selectedSlot.stack.item != null && (item == null || item.equals(selectedSlot.stack.item));
    }

    public void unfocus() {
        Core.scene.unfocus(this);
    }

    @Override
    public void updateWindow() {
        if(!state.isPlaying() || !SpaceExploration.inited()) {
            return;
        }

        boolean rebuild = false;
        var n = SpaceExploration.getInventory();
        if(n != accessor) {
            rebuild = true;
        }
        accessor = n;

        var stack = player.unit().stack();
        if(stack != null && stack.amount > 0 && !_await_135913 && _await_599208 <= 0) {
            accessor.pushItem(new SlotItem(stack.item), stack.amount);
            ServerIntegration.inventorySync();

            if(ServerIntegration.host()) {
                player.unit().stack().amount = 0;
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
        }).growX().left().height(50).row();

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
                            if(slot.stack.count <= 1) {
                                selectedSlot.pick(slot);
                            } else {
                                selectedSlot.overdrive = slot.overdrive;
                                selectedSlot.stack.item = slot.stack.item;

                                float x = slot.stack.count % 2f;
                                float c = Mathf.floor(slot.stack.count / 2f);

                                selectedSlot.stack.count = c;
                                slot.stack.count = c + x;
                            }
                        } else if(picked(slot.stack.item) && (slot.stack.item == null || slot.stack.count < slot.maxStackSize())) {
                            if(selectedSlot.stack.count == 1) {
                                selectedSlot.stack.item = null;
                                selectedSlot.stack.count = 0;
                            } else if(selectedSlot.stack.count > 1) {
                                selectedSlot.stack.count--;
                            }

                            if(selectedSlot.stack.count >= 1) {
                                if(slot.stack.item == null) {
                                    slot.stack.item = selectedSlot.stack.item;
                                    slot.stack.count = 1;
                                } else {
                                    slot.stack.count++;
                                }
                            }
                        }

                        ServerIntegration.inventorySync();
                    });

                    elem.clicked(() -> {
                        if(picked(slot.stack.item)) {
                            selectedSlot.drop(slot);
                        } else if(slot.stack.item != null) {
                            if(picked(null)) {
                                Slot.tmp1.stack = slot.stack;
                                Slot.tmp1.overdrive = slot.overdrive;

                                slot.stack = selectedSlot.stack;
                                slot.overdrive = selectedSlot.overdrive;

                                selectedSlot.stack = Slot.tmp1.stack;
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

            data.table(cont -> {
                cont.setBackground(Tex.pane);
                buttonsCont = cont;

                if(selected != null) {
                    selected.builder.get(this);
                }
            }).grow().get();

            Core.app.post(() -> {
                pane.setScrollPercentY(scrollY);
                pane.updateVisualScroll();

                pane.update(() -> {
                    scrollY = pane.getScrollY();
                });
            });
        }).grow();
    }

    @Override
    public String getTitle() {
        return player.plainName();
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
                if(selectedSlot != null && selectedSlot.stack.item != null) {
                    var pos = camera.unproject(Core.input.mouse());
                    var icon = selectedSlot.stack.item.get().uiIcon;
                    Draw.rect(icon, pos.x, pos.y, icon.width, icon.height);
                    float val = Mathf.floor(selectedSlot.stack.count);
                    var size = TextDraw2.size(val, 0);
                    TextDraw2.TEXT_SCALE *= 3;
                    TextDraw2.text(icon.width / 2f + pos.x + size.x / 2 + 8, pos.y, 0, Color.white, val);
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

        public void onBuild(Cell<TextButton> ignored) {}
    }

    public static class CraftingCategory {
        public Seq<Seq<UnlockableContent>> content = new Seq<>();
        public UnlockableContent icon;
        public String name;
    }
}
