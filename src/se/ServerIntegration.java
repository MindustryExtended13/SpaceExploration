package se;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Cons2;

import mindustry.game.EventType.*;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.io.JsonIO;

import se.event.PlayerPacketCallbackResult;
import se.prototypes.slot.Inventory;

import static mindustry.Vars.*;
import static se.SpaceExploration.*;

public class ServerIntegration {
    public static void load() {
        Events.on(PlayerJoin.class, e -> {
            if(e.player != null && inited()) {
                LOGGER.log("Sending inventory to new player");
                inventoryLoad(e.player);
            }
        });

        Events.run(HostEvent.class, () -> Core.app.post(() -> {
            LOGGER.log("Started server");
        }));

        netClient.addPacketHandler("SeCallback", args -> {
            Events.fire(new PlayerPacketCallbackResult(args));
        });

        addClientPacketHandler("SeInventoryLoad", args -> {
            currentInventory = JsonIO.read(Inventory.class, args);
        });

        addServerPacketHandler("SeInventorySync", (target, args) -> {
            getEntity().inventoryMap.put(target.con.uuid, JsonIO.read(Inventory.class, args));
        });

        addServerPacketHandler("SeItemDrop", (target, args) -> {
            target.unit().stack().amount = 0;
        });

        /*
        Events.on(PlayerPacketCallbackResult.class, e -> {
            LOGGER.log("Received callback packet: {}", e.packet());
        });
         */
    }

    public static void addClientPacketHandler(String type, Cons<String> processor) {
        netClient.addPacketHandler(type, (args) -> {
            processor.get(args);
            Call.clientPacketReliable("SeCallback", type);
        });
    }

    public static void addServerPacketHandler(String type, Cons2<Player, String> processor) {
        netServer.addPacketHandler(type, (target, args) -> {
            processor.get(target, args);
            Call.clientPacketReliable(target.con, "SeCallback", type);
        });
    }

    public static boolean host() {
        return net.server() || !net.active();
    }

    public static void inventorySync() {
        if(net.client()) Call.serverPacketReliable("SeInventorySync", JsonIO.write(getInventory()));
    }

    public static void inventoryLoad(Player player) {
        if(net.active()) Call.clientPacketReliable(player.con, "SeInventoryLoad", JsonIO.write(getEntity().getInventory(player)));
    }
}