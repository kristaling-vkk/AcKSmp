package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.math.Vector3dm;
import com.github.retrooper.packetevents.util.Vector3d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// Copied directly from Hawk
public record Ray(@NotNull Vector3d origin, @NotNull Vector3d direction) implements Cloneable {
    public Ray(@NotNull Vector3dm origin, @NotNull Vector3dm direction) {
        this(new Vector3d(origin.getX(), origin.getY(), origin.getZ()),
                new Vector3d(direction.getX(), direction.getY(), direction.getZ()));
    }

    public Ray(@NotNull GrimPlayer player, double x, double y, double z, float xRot, float yRot) {
        this(new Vector3d(x, y, z), calculateDirection(player, xRot, yRot));
    }

    // Version-specific look vector, matching the client's own math (upstream 73eee60fc)
    public static @NotNull Vector3d calculateDirection(@NotNull GrimPlayer player, float xRot, float yRot) {
        Vector3dm look = ReachUtils.getLook(player, xRot, yRot);
        return new Vector3d(look.getX(), look.getY(), look.getZ());
    }

    @Contract(" -> this")
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public @NotNull Ray clone() {
        return this;
    }

    public @NotNull Vector3d getPointAtDistance(double distance) {
        return origin.add(direction.multiply(distance));
    }

    // https://en.wikipedia.org/wiki/Skew_lines#Nearest_Points
    public @NotNull Pair<@NotNull Vector3d, @NotNull Vector3d> closestPointsBetweenLines(@NotNull Ray other) {
        Vector3d n1 = direction.crossProduct(other.direction.crossProduct(direction));
        Vector3d n2 = other.direction.crossProduct(direction.crossProduct(other.direction));

        Vector3d c1 = origin.add(direction.multiply(other.origin.subtract(origin).dot(n2) / direction.dot(n2)));
        Vector3d c2 = other.origin.add(other.direction.multiply(origin.subtract(other.origin).dot(n1) / other.direction.dot(n1)));

        return new Pair<>(c1, c2);
    }
}
