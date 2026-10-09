package dev.hen.client.util;

import dev.hen.client.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CombatUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isFriend(Entity e) {
        return e instanceof PlayerEntity p && Config.friends.contains(p.getName().getString().toLowerCase());
    }

    public static boolean validTarget(PlayerEntity p) {
        return p != mc.player && p.isAlive() && !p.isSpectator() && !isFriend(p);
    }

    public static List<PlayerEntity> players() {
        List<PlayerEntity> out = new ArrayList<>();
        for (PlayerEntity p : mc.world.getPlayers()) if (validTarget(p)) out.add(p);
        return out;
    }

    public static boolean hasLineOfSight(Vec3d from, Vec3d to) {
        return mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    /** Entity under the crosshair using a pure ray-vs-hitbox test. If throughWalls, blocks are ignored. */
    public static Optional<PlayerEntity> rayTarget(double reach, boolean throughWalls) {
        Vec3d start = mc.player.getEyePos();
        Vec3d end = start.add(mc.player.getRotationVec(1f).multiply(reach));
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlayerEntity p : players()) {
            Box box = p.getBoundingBox().expand(p.getTargetingMargin());
            Optional<Vec3d> hit = box.raycast(start, end);
            Vec3d point = hit.orElse(box.contains(start) ? start : null);
            if (point == null) continue;
            if (!throughWalls && !hasLineOfSight(start, point)) continue;
            double d = start.squaredDistanceTo(point);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return Optional.ofNullable(best);
    }

    /** Falling, not in water/ladder = a critical hit will land. */
    public static boolean critReady() {
        return !mc.player.isOnGround() && mc.player.getVelocity().y < 0
                && !mc.player.isClimbing() && !mc.player.isTouchingWater() && !mc.player.hasVehicle();
    }

    public static void autoWeapon() {
        int best = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isIn(ItemTags.SWORDS)) { best = i; break; }
            if (s.isIn(ItemTags.AXES) && best == -1) best = i;
        }
        if (best != -1) mc.player.getInventory().setSelectedSlot(best);
    }

    public static void attack(Entity e) {
        mc.interactionManager.attackEntity(mc.player, e);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
