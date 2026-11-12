package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.checks.type.PacketSendListener;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "InventoryE", stableKey = "grim.inventory.held_item_change_open", setback = 3, description = "Sent a held item change packet while inventory is open")
// Must implement PacketSendListener, otherwise CheckManager never dispatches onPacketSend to this check
public class InventoryE extends InventoryCheck implements PacketSendListener {
    // Transaction that was last sent before the latest server-sent held item change.
    // The client answers a server-sent held item change with its own held item change packet,
    // so we exempt until the player has responded to a transaction sent after it.
    //
    // This used to start at Long.MAX_VALUE and was only ever lowered by onPacketSend, which was never called.
    // That made "lastTransaction < lastTransactionReceived" impossible and the check could never flag.
    // Start with no pending server-sent held item change instead.
    private long lastTransaction = Long.MIN_VALUE;

    public InventoryE(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        super.onPacketReceive(event);

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            // It is not possible to change hotbar slots with held item change while the inventory is open
            // A container click packet would be sent instead
            if (player.hasInventoryOpen) {
                if (this.lastTransaction < player.lastTransactionReceived.get()
                        && flag()) {
                    // Cancel the packet
                    if (shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                        player.inventory.needResend = true;
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

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.HELD_ITEM_CHANGE) {
            this.lastTransaction = player.lastTransactionSent.get();
        }
    }
}
