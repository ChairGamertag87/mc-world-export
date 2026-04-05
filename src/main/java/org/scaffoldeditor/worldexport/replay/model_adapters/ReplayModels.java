package org.scaffoldeditor.worldexport.replay.model_adapters;

import org.scaffoldeditor.worldexport.replay.model_adapters.BipedModelAdapter.BipedModelFactory;
import org.scaffoldeditor.worldexport.replay.model_adapters.ReplayModelAdapter.ReplayModelAdapterFactory;
import org.scaffoldeditor.worldexport.replay.model_adapters.custom.FireballModelAdapter;
import org.scaffoldeditor.worldexport.replay.model_adapters.custom.FlyingItemModelAdapter;
import org.scaffoldeditor.worldexport.replay.model_adapters.custom.ProjectileModelAdapter;
import org.scaffoldeditor.worldexport.replay.model_adapters.specific.ChickenModelAdapter;
import org.scaffoldeditor.worldexport.replay.model_adapters.specific.HorseModelAdapter;
import org.scaffoldeditor.worldexport.replay.model_adapters.specific.ItemModelAdapter;
import org.scaffoldeditor.worldexport.replay.model_adapters.specific.PlayerModelAdapter;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.Identifier;

/**
 * Contains replay models for vanilla Minecraft entities.
 */
public final class ReplayModels {
    private ReplayModels() {
    };

    public static final float BIPED_Y_OFFSET = 1.5f;

    public static class AnimalModelFactory<T extends LivingEntity> implements ReplayModelAdapterFactory<T> {

        public Identifier tex;
        public AnimalModelFactory(Identifier tex) {
            this.tex = tex;
        }

        @Override
        public AnimalModelAdapter<T> create(T entity) {
            return new AnimalModelAdapter<T>(entity, tex);
        }

    }

    public static class SinglePartModelFactory implements ReplayModelAdapterFactory<LivingEntity> {

        @Override
        public ReplayModelAdapter<?> create(LivingEntity entity) {
            return new SinglePartModelAdapter<>(entity);
        }
          
    }

    public static class CompositeModelFactory implements ReplayModelAdapterFactory<LivingEntity> {

        @Override
        public ReplayModelAdapter<?> create(LivingEntity entity) {
                return new CompositeModelAdapter<>(entity);
        }
        
    }

    @SuppressWarnings("rawtypes")
    public static void registerDefaults() {

        ReplayModelAdapter.REGISTRY.put(Identifier.of("player"), entity -> PlayerModelAdapter.newInstance((AbstractClientPlayerEntity) entity));

        /**
         * QUADRIPEDS
         */
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:cow"),
                new AnimalModelFactory(Identifier.of("textures/entity/cow/cow.png")));
                
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:goat"),
                new AnimalModelFactory(Identifier.of("textures/entity/goat/goat.png")));

        // TODO: write custom model adapter that updates texture situationally.
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:panda"), 
                new AnimalModelFactory(Identifier.of("textures/entity/panda/panda.png")));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:pig"),
                new AnimalModelFactory(Identifier.of("textures/entity/pig/pig.png")));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:polar_bear"),
                new AnimalModelFactory(Identifier.of("textures/entity/bear/polarbear.png")));
        
        // TODO: Make this render wool properly.
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:sheep"),
                new AnimalModelFactory(Identifier.of("textures/entity/sheep/sheep.png")));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:turtle"), 
                new AnimalModelFactory(Identifier.of("textures/entity/turtle/big_sea_turtle.png")));
            
        /**
         * BIPEDS
         */
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:zombie"),
                new BipedModelFactory(Identifier.of("textures/entity/zombie/zombie.png")));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:drowned"),
                new BipedModelFactory(Identifier.of("textures/entity/zombie/drowned.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:enderman"), 
                new AnimalModelFactory(Identifier.of("textures/entity/enderman/enderman.png")));
            
        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:skeleton"),
                new BipedModelFactory(Identifier.of("textures/entity/skeleton/skeleton.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:wither_skeleton"), 
                new BipedModelFactory(Identifier.of("textures/entity/skeleton/wither_skeleton.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:stray"), 
                new BipedModelFactory(Identifier.of("textures/entity/skeleton/stray.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("minecraft:vex"), 
                new BipedModelFactory(Identifier.of("textures/entity/illager/vex.png")));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("zombie_villager"), 
                new BipedModelFactory(Identifier.of("textures/entity/zombie_villager/zombie_villager.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("piglin"),
                new BipedModelFactory(Identifier.of("textures/entity/piglin/piglin.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("piglin_brute"),
                new BipedModelFactory(Identifier.of("textures/entity/piglin/piglin_brute.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("zombified_piglin"),
                new BipedModelFactory(Identifier.of("textures/entity/piglin/zombified_piglin.png")));

        /**
         * MISC
         */

        // TODO: Axolotl's varients make implementation non-trivial

        ReplayModelAdapter.REGISTRY.put(Identifier.of("bee"),
                new AnimalModelFactory(Identifier.of("textures/entity/bee/bee.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("chicken"), entity -> new ChickenModelAdapter((ChickenEntity) entity));

        // ReplayModelAdapter.REGISTRY.put(Identifier.of("chicken"), entity -> new ChickenModelAdapter(entity));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("fox"),
                new AnimalModelFactory(Identifier.of("textures/entity/fox/fox.png")));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("hoglin"), 
                new AnimalModelFactory(Identifier.of("textures/entity/hoglin/hoglin.png")));

        ReplayModelAdapter.REGISTRY.put(Identifier.of("horse"), ent -> new HorseModelAdapter((HorseEntity) ent));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("donkey"),
                new AnimalModelFactory(Identifier.of("textures/entity/horse/donkey.png")));
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("item"), ent -> new ItemModelAdapter((ItemEntity) ent));
        
        /**
         * SINGLE PART
         */
        registerSinglePart("creeper");
        registerSinglePart("illager");
        registerSinglePart("wither");
        registerSinglePart("magma_cube");
        registerSinglePart("parrot");
        registerSinglePart("dolphin");
        registerSinglePart("villager");
        registerSinglePart("salmon");
        registerSinglePart("spider");
        registerSinglePart("phantom");
        registerSinglePart("ghast");
        registerSinglePart("strider");
        registerSinglePart("ravager");
        registerSinglePart("silverfish");
        registerSinglePart("guardian");
        registerSinglePart("snow_golem");
        registerSinglePart("slime");
        registerSinglePart("iron_golem");
        registerSinglePart("cod");
        registerSinglePart("bat");
        registerSinglePart("endermite");
        registerSinglePart("blaze");

        /**
         * COMPOSITE
         */
        registerComposite("shulker");

        /**
         * CUSTOM
         */
        ReplayModelAdapter.REGISTRY.put(Identifier.of("fireball"), FireballModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("small_fireball"), FireballModelAdapter::new);
        
        ReplayModelAdapter.REGISTRY.put(Identifier.of("arrow"), ProjectileModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("spectral_arrow"), ProjectileModelAdapter::new);

        /**
         * FLYING ITEMS (thrown projectiles rendered as an item model)
         */
        ReplayModelAdapter.REGISTRY.put(Identifier.of("snowball"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("egg"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("ender_pearl"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("experience_bottle"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("potion"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("splash_potion"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("lingering_potion"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("wind_charge"), FlyingItemModelAdapter::new);
        ReplayModelAdapter.REGISTRY.put(Identifier.of("breeze_wind_charge"), FlyingItemModelAdapter::new);
    }

    /**
     * Register a single part entity model. Cuts down on the typing.
     */
    private static void registerSinglePart(Identifier id) {
        ReplayModelAdapter.REGISTRY.put(id, new SinglePartModelFactory());
    }

    private static void registerSinglePart(String id) {
        registerSinglePart(Identifier.of(id));
    }

    private static void registerComposite(Identifier id) {
        ReplayModelAdapter.REGISTRY.put(id, new CompositeModelFactory());
    }

    private static void registerComposite(String id) {
        registerComposite(Identifier.of(id));
    }
}
