package ac.grim.grimac.checks.impl.combat.killaura;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketReceiveListener;
import ac.grim.grimac.checks.type.RotationListener;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.AttackPackets;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "AuraHitSnap", stableKey = "grom.killaura.post_hit_snap", setback = 0, description = "Rotation Pos-Hit (LightAC Inspiration)")
public class AuraPostSnap extends Check implements RotationListener, PacketReceiveListener {
    private long lastStaticHitTime;
    private double buffer;
    private boolean lastPacketWasAttack;

    public AuraPostSnap(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final int attackedEntityId = AttackPackets.attackedEntityId(event);
        if (attackedEntityId != AttackPackets.NOT_AN_ATTACK) {
            lastPacketWasAttack = true;
        }
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        long now = System.currentTimeMillis();
        if (lastPacketWasAttack) {
            if (rotationUpdate.deltaYawABS() < 0.1 && rotationUpdate.deltaPitchABS() < 0.1) lastStaticHitTime = now;
            lastPacketWasAttack = false;
            return;
        }
        long timeSinceHit = now - lastStaticHitTime;
        if (timeSinceHit < 200 && lastStaticHitTime != 0) {
            if (rotationUpdate.deltaYawABS() > 35.0) {
                if (++buffer > 5) {
                    flagAndAlert("snap=" + rotationUpdate.deltaYawABS());
                    buffer = 3;
                }
                lastStaticHitTime = 0;
            }
        } else {
            buffer = Math.max(0, buffer - 0.01);
        }
    }
}
