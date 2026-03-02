package com.ricedotwho.rsa.utils;

import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.lang.reflect.InvocationTargetException;

@UtilityClass
public class Util implements Accessor {

    public void setTickRate(float tickRate, boolean frozen) {
        if (tickRate > 20 || tickRate < 0) {
            throw new IllegalArgumentException("tickRate must be between 0 and 20!");
        }

        TickRateManager tickRateManager = mc.level.tickRateManager();
        tickRateManager.setTickRate(tickRate);

        tickRateManager.setFrozen(frozen);
        if (frozen) {
            tickRateManager.setFrozenTicksToRun(0);
        }
    }

    public void setTickRate(float tickRate) {
        setTickRate(tickRate, tickRate == 0);
    }

    public boolean isZero() {
        if (FabricLoader.getInstance().isModLoaded("zeroclient")) {
            try {
                Class<?> clazz = Class.forName("com.ricedotwho.zero.ZeroClient");
                return (boolean) clazz.getMethod("isZero").invoke(null);
            } catch (Throwable t) {
                return false;
            }
        }
        return false;
    }
}
