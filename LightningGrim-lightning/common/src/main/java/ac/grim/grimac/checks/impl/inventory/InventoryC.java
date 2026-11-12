package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceListener;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;

@CheckData(name = "InventoryC", stableKey = "grim.inventory.place_open", setback = 3, description = "Placed a block while inventory is open")
// Must implement BlockPlaceListener, otherwise CheckManager never dispatches block places to this check
public class InventoryC extends InventoryCheck implements BlockPlaceListener {

    public InventoryC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        // It is not possible to place a block while the inventory is open
        if (player.hasInventoryOpen) {
            if (flag()) {
                if (shouldModifyPackets()) {
                    place.resync();
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
