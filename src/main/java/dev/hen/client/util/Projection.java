package dev.hen.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/** World -> screen projection done in plain math so we don't depend on version-specific render APIs. */
public class Projection {
    private static Vec3d cam, fwd, right, up;
    private static double scale;
    private static int w, h;

    public static void begin(MinecraftClient mc, float tickProgress, int scaledW, int scaledH) {
        cam = mc.player.getLerpedPos(tickProgress).add(0, mc.player.getStandingEyeHeight(), 0);
        float yaw = mc.player.getYaw(tickProgress), pitch = mc.player.getPitch(tickProgress);
        fwd = Vec3d.fromPolar(pitch, yaw).normalize();
        right = fwd.crossProduct(new Vec3d(0, 1, 0)).normalize();
        up = right.crossProduct(fwd).normalize();
        double fovY = Math.toRadians(mc.options.getFov().getValue());
        w = scaledW; h = scaledH;
        scale = (h / 2.0) / Math.tan(fovY / 2.0);
    }

    /** @return {x, y} in scaled GUI coords, or null if behind the camera. */
    public static double[] project(Vec3d p) {
        Vec3d d = p.subtract(cam);
        double z = d.dotProduct(fwd);
        if (z <= 0.05) return null;
        double x = d.dotProduct(right), y = d.dotProduct(up);
        return new double[]{ w / 2.0 + x / z * scale, h / 2.0 - y / z * scale };
    }

    public static int width() { return w; }
    public static int height() { return h; }
}
