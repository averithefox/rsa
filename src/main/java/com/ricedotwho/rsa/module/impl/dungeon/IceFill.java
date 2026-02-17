package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
@ModuleInfo(aliases = "IceFill", id = "IceFill", category = Category.DUNGEONS)
public class IceFill extends com.ricedotwho.rsm.module.impl.dungeon.IceFill {
	public final BooleanSetting autoEnabled = new BooleanSetting("Auto Ice Fill", false);
	public final BooleanSetting autoRotate = new BooleanSetting("Rotate", false);

	private int index = -1;

	public IceFill() {
		super();
		this.registerProperty(autoEnabled, autoRotate);

	}



	private boolean inRange(Vec3 pos, Pos target) {
		return pos.x > target.x - 0.5 && pos.x < target.x + 0.5 &&
			pos.y == target.y &&
			pos.z > target.z - 0.5 && pos.z < target.z + 0.5;
	}
}
