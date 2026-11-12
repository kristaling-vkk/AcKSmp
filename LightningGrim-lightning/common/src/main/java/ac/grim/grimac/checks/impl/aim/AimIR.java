package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketReceiveListener;
import ac.grim.grimac.checks.type.RotationListener;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.AttackPackets;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "AimIR", stableKey = "grom.aim.identical_rotation", setback = 0, description = "Identical Rotation (Artemis Inspiration)")
public class AimIR extends Check implements RotationListener, PacketReceiveListener {
    private float lastYawDelta;
    private double streak, lastAttack;

    public AimIR(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final int attackedEntityId = AttackPackets.attackedEntityId(event);
        if (attackedEntityId != AttackPackets.NOT_AN_ATTACK) {
            lastAttack = System.currentTimeMillis();
        }
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (System.currentTimeMillis() - lastAttack > 2000 || player.packetStateData.lastPacketWasTeleport) {
            streak = 0; return;
        }
        float yawDelta = rotationUpdate.deltaYawABS();
        float pitchDelta = rotationUpdate.deltaPitchABS();

        if (yawDelta > 0.0f && pitchDelta > 0.0f) {
            int roundedYaw = Math.round(yawDelta);
            if (roundedYaw == Math.round(lastYawDelta) && yawDelta > 1.5f && Math.abs(yawDelta - lastYawDelta) > 0.001f && pitchDelta > 0.5f) {
                if (++streak > 6) flagAndAlert("streak=" + (int)streak);
            } else {
                streak = Math.max(0, streak - 0.25);
            }
        }
        this.lastYawDelta = yawDelta;
    }
}
