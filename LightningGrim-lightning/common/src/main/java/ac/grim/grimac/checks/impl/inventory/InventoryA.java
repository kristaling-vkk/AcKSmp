package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction;

@CheckData(name = "InventoryA", stableKey = "grim.inventory.attack_open", setback = 3, description = "Attacked an entity while inventory is open")
public class InventoryA extends InventoryCheck {
    public InventoryA(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        super.onPacketReceive(event);

        // Newer clients send attacks in a dedicated ATTACK packet instead of INTERACT_ENTITY,
        // not handling it here let those clients attack with an open inventory without ever being checked
        final boolean isAttack;
        if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
            isAttack = true;
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            isAttack = new WrapperPlayClientInteractEntity(event).getAction() == InteractAction.ATTACK;
        } else {
            isAttack = false;
        }

        if (!isAttack) return;

        // Is not possible to attack while the inventory is open.
        if (player.hasInventoryOpen) {
            if (flag()) {
                // Cancel the packet
                if (shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
                if (!isNoSetbackPermission())
                    closeInventory();
            }
        } else {
            reward();
        }
    }
}
