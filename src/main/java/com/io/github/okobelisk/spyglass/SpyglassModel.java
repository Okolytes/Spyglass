package com.io.github.okobelisk.spyglass;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Spyglass.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SpyglassModel {

    // thanks to TeamSpen210#7765 for helping me figure all of this out

    @SubscribeEvent
    public static void loadSpyglassModel(ModelRegistryEvent event) {
        ModelLoader.addSpecialModel(new ModelResourceLocation(Spyglass.SPYGLASS.get().getRegistryName() + "_3d", "inventory"));
    }

    @SubscribeEvent
    public static void bakeSpyglass(ModelBakeEvent event) {
        IBakedModel spyglassModel = event.getModelRegistry().get(new ModelResourceLocation(Objects.requireNonNull(Spyglass.SPYGLASS.get().getRegistryName()), "inventory"));
        IBakedModel spyglassModel3D = event.getModelRegistry().get(new ModelResourceLocation(Spyglass.SPYGLASS.get().getRegistryName() + "_3d", "inventory"));
        event.getModelRegistry().put(new ModelResourceLocation(Objects.requireNonNull(Spyglass.SPYGLASS.get().getRegistryName()), "inventory"), new BakedSpyglassModel(spyglassModel, spyglassModel3D));
    }

    protected static class BakedSpyglassModel implements IBakedModel {

        private final IBakedModel spyglassModel;
        private final IBakedModel spyglassModel3D;

        public BakedSpyglassModel(IBakedModel _spyglassModel, IBakedModel _spyglassModel3D) {
            super();
            spyglassModel = _spyglassModel;
            spyglassModel3D = _spyglassModel3D;
        }

        @Override
        public IBakedModel handlePerspective(ItemCameraTransforms.TransformType transformType, MatrixStack mat) {
            IBakedModel bakedModel = spyglassModel3D;
            if (transformType == ItemCameraTransforms.TransformType.GUI || transformType == ItemCameraTransforms.TransformType.GROUND || transformType == ItemCameraTransforms.TransformType.FIXED || transformType == ItemCameraTransforms.TransformType.HEAD)
                bakedModel = spyglassModel;

            return ForgeHooksClient.handlePerspective(bakedModel, transformType, mat);
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
            return spyglassModel.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return spyglassModel.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return spyglassModel.isGui3d();
        }

        @Override
        public boolean func_230044_c_() {
            return spyglassModel.func_230044_c_();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return spyglassModel.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return spyglassModel.getParticleTexture();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return spyglassModel.getOverrides();
        }
    }
}
