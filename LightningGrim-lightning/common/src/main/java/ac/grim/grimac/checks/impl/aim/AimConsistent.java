package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketReceiveListener;
import ac.grim.grimac.checks.type.RotationListener;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.AttackPackets;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "AimConsistent", stableKey = "grom.aim.consistent_speed", setback = 0, description = "Consistent Speed Rotation(Rhys Inspiration)")
public class AimConsistent extends Check implements RotationListener, PacketReceiveListener {

    private final List<Double> deltaYawList = new ArrayList<>();
    private double buffer;
    private long lastAttackTime;

    public AimConsistent(GrimPlayer playerData) {
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
        if (System.currentTimeMillis() - lastAttackTime > 500) return;

        double deltaYaw = (double) rotationUpdate.deltaYawABS();

        if (deltaYaw > 1.0) {
            deltaYawList.add(deltaYaw);

            if (deltaYawList.size() >= 25) {
                double std = GrimMath.calculateSD(deltaYawList);

                if (std < 0.02) {
                    if (++buffer > 3) {
                        flagAndAlert("std=" + String.format("%.4f", std));
                    }
                } else {
                    buffer = Math.max(0, buffer - 1.0);
                }

                deltaYawList.clear();
            }
        }
    }
}
