package com.ricedotwho.rsa.module.impl.player;

import com.ricedotwho.rsa.component.impl.TickFreeze;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;

import java.util.Objects;

@Getter
@ModuleInfo(aliases = "Bonzo Helper", id = "BonzoHelper", category = Category.PLAYER)
public class BonzoHelper extends Module {
	private final NumberSetting time = new NumberSetting("Time", 0, 500, 100, 1);

	public BonzoHelper() {
		registerProperty(time);
	}

	@SubscribeEvent
	public void onPacketSend(PacketEvent.Send event) {
		if (event.getPacket() instanceof ServerboundUseItemPacket packet1) {
			if (packet1.getHand() != InteractionHand.MAIN_HAND) return;
		} else if (event.getPacket() instanceof ServerboundUseItemOnPacket packet2) {
			if (packet2.getHand() != InteractionHand.MAIN_HAND) return;
		} else return;
		if (mc.player == null || !Objects.equals(ItemUtils.getID(mc.player.getMainHandItem()), "BONZO_STAFF")) return;
		TickFreeze.freeze(time.getValue().longValue());
	}
}
