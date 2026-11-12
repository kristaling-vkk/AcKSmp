package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketReceiveListener;
import ac.grim.grimac.checks.type.RotationListener;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.AttackPackets;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "AimConstant", stableKey = "grom.aim.constant_speed", setback = 0, description = "Constant Speed (Rhys Inspiration)")
public class AimConstant extends Check implements RotationListener, PacketReceiveListener {
    private float lastDeltaYaw;
    private double buffer;
    private long lastAttackTime;

    public AimConstant(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final int attackedEntityId = AttackPackets.attackedEntityId(event);
        if (attackedEntityId != AttackPackets.NOT_AN_ATTACK) {
            lastAttackTime = System.currentTimeMillis();
        }
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (System.currentTimeMillis() - lastAttackTime > 200) return;
        float deltaYaw = rotationUpdate.deltaYawABS();
        if (deltaYaw > 1.0f && deltaYaw == lastDeltaYaw) {
            if (++buffer > 5) flagAndAlert("speed=" + deltaYaw);
        } else buffer = Math.max(0, buffer - 0.5);
        lastDeltaYaw = deltaYaw;
    }
}
