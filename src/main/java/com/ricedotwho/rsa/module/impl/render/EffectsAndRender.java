package com.ricedotwho.rsa.module.impl.render;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.core.particles.ParticleTypes;

@Getter
@Environment(EnvType.CLIENT)
@ModuleInfo(aliases = "Effects", id = "EffectsAndRender", category = Category.RENDER)
public class EffectsAndRender extends Module {
    private final BooleanSetting Explosions = new BooleanSetting("Explosions", false, () -> true);
    private final BooleanSetting Fires = new BooleanSetting("Fires", false, () -> true);
    private final BooleanSetting EtherWarp = new BooleanSetting("EtherWarp", false, () -> true);
    private final BooleanSetting SMOKE = new BooleanSetting("SMOKE", false, () -> true);

    public EffectsAndRender() {
        this.registerProperty(
                Explosions,
                Fires,
                EtherWarp,
                SMOKE
        );
    }

    public static void init() {
        //FUCK YOU EXPLOSIONS
        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.EXPLOSION,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.HugeExplosionParticle.Provider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).Explosions.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                    // i lowk have no idea what g, h, or i is but :shrug: it works so it works. im assuming d, e, f = xyz, but i have no idea what the rest are
                }
        );

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.EXPLOSION_EMITTER,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.HugeExplosionSeedParticle.Provider();
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).Explosions.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        //fire
        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.DRAGON_BREATH,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.DragonBreathParticle.Provider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).Fires.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.FLAME,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.FlameParticle.Provider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).Fires.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        //etherwarp
        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.PORTAL,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.PortalParticle.Provider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).EtherWarp.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.WITCH,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.SpellParticle.WitchProvider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).EtherWarp.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        //smoke
        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.LARGE_SMOKE,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.LargeSmokeParticle.Provider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).SMOKE.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.SMOKE,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.SmokeParticle.Provider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).SMOKE.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.CampfireSmokeParticle.CosyProvider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).SMOKE.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.CampfireSmokeParticle.SignalProvider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).SMOKE.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );

        ParticleFactoryRegistry.getInstance().register(
                ParticleTypes.WHITE_SMOKE,
                spriteSet -> {
                    var originalFactory = new net.minecraft.client.particle.WhiteSmokeParticle.Provider(spriteSet);
                    return (simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource) ->
                            RSM.getModule(EffectsAndRender.class).SMOKE.getValue() ? null : originalFactory.createParticle(simpleParticleType, clientLevel, d, e, f, g, h, i, randomSource);
                }
        );
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void reset() {
    }
}