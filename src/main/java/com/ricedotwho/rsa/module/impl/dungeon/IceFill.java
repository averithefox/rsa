package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationHandler;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationProvider;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.EtherUtils;
import lombok.Getter;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Getter
@ModuleInfo(aliases = "IceFill", id = "IceFill", category = Category.DUNGEONS, isOverwrite = true)
public class IceFill extends com.ricedotwho.rsm.module.impl.dungeon.IceFill implements ClientRotationProvider {
	public final BooleanSetting autoEnabled = new BooleanSetting("Auto Ice Fill", false);
	public final BooleanSetting autoRotate = new BooleanSetting("Rotate", false);

	boolean waitTeleport = false;
	private boolean isRotationActive = false;

	public IceFill() {
		super();
		this.registerProperty(autoEnabled, autoRotate);
	}

	@SubscribeEvent
	public void onInputPoll(InputPollEvent event) {
		if (!autoEnabled.getValue()) return;
		assert mc.player != null;
		if (this.path == null || mc.player.getMainHandItem().getItem() != Items.DIAMOND_SHOVEL) {
			isRotationActive = false;
			return;
		}
		if (waitTeleport) return;
		int index = this.findIndex(mc.player.position());
		if (index != -1) {
			if (index >= this.path.size() - 1) return;
			isRotationActive = true;
			ClientRotationHandler.registerProvider(this);
			Pos current = this.path.get(index);
			Pos nextDir = getDirection(current, this.path.get(index + 1));
			float yaw = EtherUtils.getYawAndPitch(nextDir.x, nextDir.y, nextDir.z)[0];
			float pitch = 90;
			ClientRotationHandler.setYaw(yaw);
			ClientRotationHandler.setPitch(pitch);
			if (velocityClean(mc.player.getDeltaMovement(), nextDir) || index == 0) {
				event.getInputConsumer().accept(new Input(true, false, false, false, false, false, false));
			} else {
				PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
					if (!SwapManager.checkClientItem(Items.DIAMOND_SHOVEL) || !SwapManager.checkServerItem(Items.DIAMOND_SHOVEL)) return;
					SwapManager.sendAirC08(yaw, pitch, false, false);
				});
				waitTeleport = true;
			}
		} else isRotationActive = false;
//		event.getInputConsumer().accept(new Input());
	}

	private boolean velocityClean(Vec3 velocity, Pos nextDir) {
		return (velocity.x == 0 && nextDir.x == 0) ||
			(velocity.z == 0 && nextDir.z == 0);
	}

	private boolean inRange(Vec3 pos, Pos target) {
		return pos.x > target.x - 0.5 && pos.x < target.x + 0.5 &&
			pos.y == target.y &&
			pos.z > target.z - 0.5 && pos.z < target.z + 0.5;
	}

	private int findIndex(Vec3 pos) {
		int sz = this.path.size();
		for (int i = 0; i < sz; ++i) {
			if (inRange(pos, this.path.get(i))) return i;
		}
		return -1;
	}

	private Pos getDirection(Pos pos1, Pos pos2) {
		Pos dir = pos2.subtract(pos1);
		dir.y = 0;
		return dir.normalize();
	}

	@Override
	public boolean isActive() {
		return isRotationActive;
	}
}
