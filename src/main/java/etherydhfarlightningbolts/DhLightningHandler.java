package etherydhfarlightningbolts;

import org.checkerframework.checker.units.qual.s;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import java.awt.Color;

import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBoxGroupShading;
import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBox;
import com.seibel.distanthorizons.api.objects.math.DhApiVec3d;
import com.seibel.distanthorizons.api.interfaces.world.IDhApiLevelWrapper;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiRenderableBoxGroup;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiBlockMaterial;
import com.seibel.distanthorizons.api.DhApi;

@Mod.EventBusSubscriber(modid = EtheryDhFarlightningBoltMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DhLightningHandler {
	private static final Map<Double, IDhApiRenderableBoxGroup> activeBolts = new HashMap<>();
	private static final Map<Double, Integer> boltAges = new HashMap<>();

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.phase != TickEvent.Phase.END)
			return;
		if (mc.player == null || mc.level == null) {
			ThunderFlashPacket.pendingBolts.clear();
			activeBolts.clear();
			boltAges.clear();
			return;
		}
		
				// 1. Process incoming packets
		ThunderFlashPacket pkt;
		while ((pkt = ThunderFlashPacket.pendingBolts.poll()) != null) {
			double px = mc.player.getX();
			double py = mc.player.getY();
			double pz = mc.player.getZ();

			double distSq = (pkt.x - px) * (pkt.x - px) + (pkt.y - py) * (pkt.y - py) + (pkt.z - pz) * (pkt.z - pz);
			double minDist = Math.max(64.0, (mc.options.getEffectiveRenderDistance() - 1) * 16.0);

			if (distSq >= (minDist * minDist)) {
				spawnDhBolt(pkt.x, pkt.y, pkt.z);
			}
		}

		// 2. Age and remove old bolts
		List<Double> toRemove = new ArrayList<>();
		for (Map.Entry<Double, Integer> entry : boltAges.entrySet()) {
			int age = entry.getValue() + 1;
			if (age >= 10) {
				toRemove.add(entry.getKey());
			} else {
				entry.setValue(age);
			}
		}
		for (Double id : toRemove) {
			removeDhBolt(id);
			boltAges.remove(id);
		}
	}

	private static void spawnDhBolt(double x, double y, double z) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;
		double id = x + (y * 1000) + (z * 1000000);
		if (activeBolts.containsKey(id))
			return;
		List<DhApiRenderableBox> boxes = new ArrayList<>();
		long seed = (long) id + System.currentTimeMillis();
		java.util.Random rand = new java.util.Random(seed);
		double totalHeight = 140.0;
		int segments = 20;
		double stepY = totalHeight / segments;
		double curX = x;
		double curY = y;
		double curZ = z;
		Color coreColor = new Color(255, 255, 255);
		Color glowColor = new Color(160, 200, 255, 95);
		for (int i = 0; i < segments; i++) {
			double nextY = curY + stepY;
			double jitter = 2.0 + (rand.nextDouble() * 6.0);
			double nextX = curX + (rand.nextDouble() - 0.5) * jitter;
			double nextZ = curZ + (rand.nextDouble() - 0.5) * jitter;
			drawLine(boxes, curX, curY, curZ, nextX, nextY, nextZ, 0.8, 0.2, glowColor, coreColor);
			if (i > 3 && i < segments - 2 && rand.nextFloat() < 0.35f) {
				double branchStartX = curX;
				double branchStartY = curY;
				double branchStartZ = curZ;
				double spread = 10.0 + (rand.nextDouble() * 20.0);
				double drop = 10.0 + (rand.nextDouble() * 40.0);
				double branchEndX = branchStartX + (rand.nextDouble() - 0.5) * spread;
				double branchEndY = branchStartY - drop;
				double branchEndZ = branchStartZ + (rand.nextDouble() - 0.5) * spread;
				boolean isThick = rand.nextFloat() < 0.2f;
				double glowR = isThick ? 0.6 : 0.4;
				double coreR = isThick ? 0.15 : 0.1;
				drawLine(boxes, branchStartX, branchStartY, branchStartZ, branchEndX, branchEndY, branchEndZ, glowR, coreR, glowColor, coreColor);
			}
			curX = nextX;
			curY = nextY;
			curZ = nextZ;
		}
		// Ground flash
		boxes.add(new DhApiRenderableBox(new DhApiVec3d(x - 3.0, y, z - 3.0), new DhApiVec3d(x + 3.0, y + 1.0, z + 3.0), coreColor, EDhApiBlockMaterial.ILLUMINATED));
		boxes.add(new DhApiRenderableBox(new DhApiVec3d(x - 6.0, y, z - 6.0), new DhApiVec3d(x + 6.0, y + 0.5, z + 6.0), new Color(150, 180, 255), EDhApiBlockMaterial.ILLUMINATED));
		IDhApiRenderableBoxGroup group = DhApi.Delayed.customRenderObjectFactory.createAbsolutePositionedGroup("ethery_farlightning:bolt_" + id, boxes);
		// Force maximum brightness
		group.setBlockLight(15);
		group.setSkyLight(15);
		// Disable shading
		group.setShading(DhApiRenderableBoxGroupShading.getUnshaded());
		IDhApiLevelWrapper levelWrapper = DhApi.Delayed.worldProxy.getSinglePlayerLevel();
		if (levelWrapper != null && levelWrapper.getRenderRegister() != null) {
			levelWrapper.getRenderRegister().add(group);
			activeBolts.put(id, group);
			boltAges.put(id, 0);
		}
	}

	private static void drawLine(List<DhApiRenderableBox> boxes, double x1, double y1, double z1, double x2, double y2, double z2, double glowRadius, double coreRadius, Color glowColor, Color coreColor) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		int steps = (int) Math.ceil(distance / 0.5);
		for (int s = 0; s < steps; s++) {
			double t = (double) s / steps;
			double bx = x1 + dx * t;
			double by = y1 + dy * t;
			double bz = z1 + dz * t;
			// Glow
			boxes.add(new DhApiRenderableBox(new DhApiVec3d(bx - glowRadius, by - 0.5, bz - glowRadius), new DhApiVec3d(bx + glowRadius, by + 0.5, bz + glowRadius), glowColor, EDhApiBlockMaterial.ILLUMINATED));
			// Core
			boxes.add(new DhApiRenderableBox(new DhApiVec3d(bx - coreRadius, by - 0.5, bz - coreRadius), new DhApiVec3d(bx + coreRadius, by + 0.5, bz + coreRadius), coreColor, EDhApiBlockMaterial.ILLUMINATED));
		}
	}

	private static void removeDhBolt(double id) {
		IDhApiRenderableBoxGroup group = activeBolts.remove(id);
		if (group != null) {
			IDhApiLevelWrapper levelWrapper = DhApi.Delayed.worldProxy.getSinglePlayerLevel();
			if (levelWrapper != null && levelWrapper.getRenderRegister() != null) {
				levelWrapper.getRenderRegister().remove(group.getId());
			}
		}
	}
}