package com.ricedotwho.rsa.utils.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsa.utils.render3d.RSAVertexRenderer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.type.RenderTask;
import com.ricedotwho.rsm.utils.render.render3d.type.RenderType;
import net.minecraft.world.phys.Vec3;

public class Ring extends RenderTask {
    private final Vec3 pos;
    private final float radius;
    private final Colour colour;
    private final int slices;
    private final int layers;

    public Ring(Vec3 pos, boolean depth, float radius, Colour colour) {
        this(pos, depth, radius, colour, 64, 16);
    }

    public Ring(Vec3 pos, boolean depth, float radius, Colour colour, int slices, int layers) {
        super(RenderType.LINE, depth);
        this.pos = pos;
        this.radius = radius;
        this.colour = colour;
        this.slices = slices;
        this.layers = layers;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {
        RSAVertexRenderer.renderRing(
                stack.last(),
                buffer,
                this.pos,
                this.radius,
                this.colour,
                this.slices,
                this.layers
        );
    }
}