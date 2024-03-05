package se.prototypes.unit;

import arc.struct.ObjectMap;
import arc.util.io.Reads;
import arc.util.io.Writes;

import me13.core.units.XeonUnitEntity;

import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Player;

import se.SpaceExploration;
import se.prototypes.slot.Inventory;

import static se.SpaceExploration.*;

public class DataUnitEntity extends XeonUnitEntity {
    public final ObjectMap<String, Inventory> inventoryMap = new ObjectMap<>();
    public static final String LOCAL_PREFIX = "LOCAL_GAME";

    public Inventory getInventory(Player player) {
        return getInventory(player.con == null ? LOCAL_PREFIX : player.con.uuid);
    }

    public Inventory getInventory(String id) {
        if(Vars.state.isCampaign()) {
            return SpaceExploration.campaignInventory;
        }

        if(!inventoryMap.containsKey(id)) {
            LOGGERW.log("ID not found in map. Creating default...");
            inventoryMap.put(id, SpaceExploration.createDefault());
        }

        return inventoryMap.get(id);
    }

    @Override
    public boolean dead() {
        return false;
    }

    @Override
    public void kill() {
    }

    @Override
    public void killed() {
    }

    @Override
    public void update() {
        x = -666;
        y = -666;
        dead(false);
    }

    @Override
    public void draw() {
    }

    @Override
    public boolean hasWeapons() {
        return false;
    }

    @Override
    public boolean targetable(Team targeter) {
        return false;
    }

    @Override
    public boolean hittable() {
        return false;
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        LOGGER.log("Writing data...");

        write.i(inventoryMap.size);
        for(var entry : inventoryMap) {
            LOGGER.log("Writing inventory of {}", entry.key);
            write.str(entry.key);
            entry.value.write(write);
        }
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        LOGGER.log("Reading data...");

        int s = read.i();
        for(int i = 0; i < s; i++) {
            var player = read.str();
            var inv = new Inventory();
            inv.read(read);
            inventoryMap.put(player, inv);
            LOGGER.log("Loaded inventory for: {}", player);
        }
    }
}
