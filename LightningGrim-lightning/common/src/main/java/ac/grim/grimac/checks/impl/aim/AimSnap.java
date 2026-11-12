package ac.grim.grimac.checks.impl.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.aim.processor.AimProcessor;
import ac.grim.grimac.checks.type.RotationListener;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "AimSnap", stableKey = "grom.aim.snap", setback = 0, description = "AimSnap(Kauri Inspiration)")
public class AimSnap extends Check implements RotationListener {

    private float buffer;
    private float lastDeltaYaw;

    public AimSnap(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (rotationUpdate.deltaYaw() == 0 && rotationUpdate.deltaPitch() == 0) return;

        float toYaw = player.yaw;
        float deltaYaw = Math.abs(rotationUpdate.deltaYaw());

        AimProcessor processor = player.checkManager.get(AimProcessor.class);
        if (processor == null) return;



        if (deltaYaw > 320
                && lastDeltaYaw > 0
                && lastDeltaYaw < 30
                && toYaw < 360 && toYaw > -360
                && !player.packetStateData.lastPacketWasTeleport
                && processor.sensitivityYaw < 0.65) {


            if (++buffer > 1) {
                flagAndAlert(String.format("yaw=%.3f", deltaYaw));
            }
        } else if (buffer > 0) {
            buffer -= 0.005f;
        }

        lastDeltaYaw = deltaYaw;
    }
}
