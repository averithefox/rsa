package com.ricedotwho.rsa.module.impl.player;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ItemUtils;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

@Getter
@ModuleInfo(aliases = "Cancel Interact", id = "CancelInteract", category = Category.PLAYER)
public class CancelInteract extends Module {

    private final BooleanSetting abilityOnly = new BooleanSetting("Ability Only", false);

    private static final List<Class<?>> WHITELIST = List.of(
            LeverBlock.class,
            SkullBlock.class,
            CauldronBlock.class,
            ChestBlock.class
    );

    private static final List<TagKey<Block>> WHITELIST_TAGS = List.of(
            BlockTags.BUTTONS,
            BlockTags.COPPER_CHESTS
    );

    private static final List<Class<?>> BLACKLIST = List.of(
            HopperBlock.class,
            CraftingTableBlock.class,
            LavaCauldronBlock.class,
            LayeredCauldronBlock.class
    );


    private static final List<TagKey<Block>> BLACKLIST_TAGS = List.of(
            BlockTags.BUTTONS,
            BlockTags.COPPER_CHESTS,
            BlockTags.WALLS,
            BlockTags.FENCES,
            BlockTags.DIRT
    );

    public CancelInteract() {
        registerProperty(abilityOnly);
    }

    public static boolean shouldCancelInteract(BlockHitResult hit, LocalPlayer player, ItemStack item) {
        CancelInteract module = RSM.getModule(CancelInteract.class);
        if (!module.isEnabled()) return false;
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        if (WHITELIST.stream().anyMatch(c -> c.isInstance(state.getBlock())) || WHITELIST_TAGS.stream().anyMatch(state::is)) return false;
        if ("ENDER_PEARL".equals(ItemUtils.getID(item))) return true;

        return (!module.getAbilityOnly().getValue()
                || ItemUtils.isAbilityItem(mc.player.getInventory().getSelectedItem()))
                && (BLACKLIST_TAGS.stream().anyMatch(state::is)
                || BLACKLIST.stream().anyMatch(c -> c.isInstance(state.getBlock())));
    }
}
