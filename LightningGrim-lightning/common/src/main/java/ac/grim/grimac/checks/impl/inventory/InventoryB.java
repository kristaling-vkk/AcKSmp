package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockBreakListener;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;

@CheckData(name = "InventoryB", stableKey = "grim.inventory.dig_open", setback = 3, description = "Started digging blocks while inventory is open")
public class InventoryB extends InventoryCheck implements BlockBreakListener {
    public InventoryB(GrimPlayer player) {
        super(player);
    }

    // This check used to expose a handle(event, packet) method that nothing called after the listener refactor,
    // which left it permanently disabled. Hook into the block break pipeline instead, so that cancelling
    // the break also resyncs the block for the client (see CheckManagerListener#handleDigging).
    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action != DiggingAction.START_DIGGING) return;

        // Is not possible to start digging a block while the inventory is open.
        if (player.hasInventoryOpen) {
            if (flag()) {
                // Cancel the packet
                if (shouldModifyPackets()) {
                    blockBreak.cancel();
                }
                if (!isNoSetbackPermission()) {
                    closeInventory();
                }
            }
        } else {
            reward();
        }
    }
}
