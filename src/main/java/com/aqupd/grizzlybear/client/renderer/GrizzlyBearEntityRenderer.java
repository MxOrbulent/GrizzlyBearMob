package com.aqupd.grizzlybear.client.renderer;

import com.aqupd.grizzlybear.client.model.GrizzlyBearEntityModel;
import com.aqupd.grizzlybear.entities.GrizzlyBearEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class GrizzlyBearEntityRenderer extends MobEntityRenderer<GrizzlyBearEntity, GrizzlyBearEntityModel<GrizzlyBearEntity>> {
    private static final Identifier TEXTURE_NORMAL = new Identifier("aqupd", "textures/entity/grizzly_bear.png");
    private static final Identifier TEXTURE_RAGE = new Identifier("aqupd", "textures/entity/grizzly_bear_rage.png");

    public GrizzlyBearEntityRenderer(Context context) {
        super(context, new GrizzlyBearEntityModel<>(context.getPart(EntityModelLayers.POLAR_BEAR)), 0.9F);
    }

    public Identifier getTexture(GrizzlyBearEntity grizzlyBearEntity) {
        if (grizzlyBearEntity.rageToDeath == true) {
            return TEXTURE_RAGE;
        }

        return TEXTURE_NORMAL;
    }

    protected void scale(GrizzlyBearEntity grizzlyBearEntity, MatrixStack matrixStack, float f) {
        matrixStack.scale(1.2F, 1.2F, 1.2F);
        super.scale(grizzlyBearEntity, matrixStack, f);
    }
}

