package ac.grim.grimac.utils.anticheat;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

/**
 * Newer clients send attacks in a dedicated ATTACK packet instead of INTERACT_ENTITY(ATTACK).
 * Checks that only look at INTERACT_ENTITY never see attacks from those clients, so every
 * attack-driven check should go through here.
 */
public final class AttackPackets {
    public static final int NOT_AN_ATTACK = Integer.MIN_VALUE;

    private AttackPackets() {
    }

    /**
     * @return the attacked entity id, or {@link #NOT_AN_ATTACK} if this packet is not an attack
     */
    public static int attackedEntityId(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
            return new WrapperPlayClientAttack(event).getEntityId();
        }
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return wrapper.getEntityId();
            }
        }
        return NOT_AN_ATTACK;
    }

    public static boolean isAttack(PacketReceiveEvent event) {
        return attackedEntityId(event) != NOT_AN_ATTACK;
    }
}
