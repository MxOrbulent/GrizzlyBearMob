package com.aqupd.grizzlybear;
//Master Branch.
import com.aqupd.grizzlybear.client.renderer.GrizzlyBearEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class MainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.INSTANCE.register(Main.GRIZZLYBEAR, (dispatcher, context) -> new GrizzlyBearEntityRenderer(dispatcher));
    }
}
