package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.checks.type.PostPredictionListener;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.data.KnownInput;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.data.VehicleData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.StringJoiner;

@CheckData(name = "InventoryD", stableKey = "grim.inventory.move_open", setback = 1, decay = 0.25)
public class InventoryD extends InventoryCheck implements PostPredictionListener {
    private int horseJumpVerbose;
    // A vanilla client can't use an item or place a block while a screen is open.
    // If it does, the "recently used an item" / "slowed by using an item" exemptions below must not apply,
    // otherwise an InvMove cheat could spam USE_ITEM every tick to be permanently exempt from this check.
    private boolean usedItemWhileOpen;

    public InventoryD(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        super.onPacketReceive(event);

        if (player.hasInventoryOpen && (event.getPacketType() == PacketType.Play.Client.USE_ITEM
                || event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT)) {
            usedItemWhileOpen = true;
        }
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!player.hasInventoryOpen) {
            usedItemWhileOpen = false;
        }

        if (!predictionComplete.isChecked() ||
                predictionComplete.getData().isTeleport() ||
                player.getSetbackTeleportUtil().blockOffsets ||
                player.packetStateData.lastPacketWasTeleport ||
                player.wasTouchingWater ||
                !usedItemWhileOpen && (player.packetStateData.isSlowedByUsingItem() ||
                        System.currentTimeMillis() - player.lastBlockPlaceUseItem < 50L)) {
            return;
        }

        if (player.hasInventoryOpen) {
            boolean inVehicle = player.inVehicle();
            boolean isJumping, isMoving;

            if (inVehicle) {
                VehicleData vehicle = player.vehicleData;

                // Will flag once if player open anything with pressed space bar
                isJumping = !vehicle.pendingJumps.isEmpty() && horseJumpVerbose++ >= 1;
                isMoving = vehicle.nextVehicleForward != 0 || vehicle.nextVehicleHorizontal != 0;
            } else {
                // The prediction engine records the chosen input on VectorData.input (MoveVectorData is no longer
                // produced since the upstream prediction rewrite, which left this check unable to ever see movement).
                Vector3dm input = player.predictedVelocity.input;
                isJumping = player.predictedVelocity.isJump();
                isMoving = input != null && (input.getX() != 0 || input.getZ() != 0);

                // 1.21.2+ clients report their held movement keys every tick; a vanilla client releases them all
                // when a screen opens. Give the opening tick a short grace for keys sent before the screen showed.
                if (player.supportsEndTick() && System.currentTimeMillis() - player.lastInventoryOpen > 100L) {
                    KnownInput keys = player.packetStateData.knownInput;
                    isMoving |= keys.movingNoJump();
                    isJumping |= keys.jump() && player.lastOnGround;
                }
            }

            if (!isMoving && !isJumping) {
                reward();
                return;
            }

            if (flag()) {
                if (!isNoSetbackPermission())
                    closeInventory();

                StringJoiner joiner = new StringJoiner(" ");

                if (isMoving) joiner.add("moving");
                if (isJumping) joiner.add("jumping");
                if (inVehicle) joiner.add("inVehicle");

                alert(joiner.toString());
            }
        } else {
            horseJumpVerbose = 0;
        }
    }
}
