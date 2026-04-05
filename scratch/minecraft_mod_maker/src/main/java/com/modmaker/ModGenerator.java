package com.modmaker;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModGenerator {

    private static final String FORGE_MDK_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.3.0/forge-1.20.1-47.3.0-mdk.zip";

    public static void generateMod(String modId, String modName, List<ModElement> elements, File outputDir, boolean useCustomCreativeTab, java.util.function.Consumer<String> logger) throws Exception {
        if (logger != null) logger.accept("Starting mod generation for " + modName + " (" + modId + ")...");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File workDir = Files.createTempDirectory("modmaker_").toFile();
        boolean success = false;

        try {
            if (logger != null) logger.accept("Downloading Forge MDK...");
            File mdkZip = File.createTempFile("forge_mdk", ".zip");
            downloadMDK(mdkZip);

            if (logger != null) logger.accept("Unpacking MDK...");
            unzip(mdkZip, workDir);
            mdkZip.delete();

            if (logger != null) logger.accept("Configuring project...");
            File exampleDir = new File(workDir, "src/main/java/com/example");
            deleteDirectory(exampleDir);

            updateGradleProperties(new File(workDir, "gradle.properties"), modId, modName);

            String group = "com.yourname." + modId;
            File javaSrcDir = new File(workDir, "src/main/java/" + group.replace('.', '/'));
            javaSrcDir.mkdirs();

            if (logger != null) logger.accept("Writing Java source files...");
            writeJavaClasses(javaSrcDir, group, modId, modName, elements, useCustomCreativeTab);

            if (logger != null) logger.accept("Creating resource assets...");
            File assetsDir = new File(workDir, "src/main/resources/assets/" + modId);
            createAssets(assetsDir, modId, modName, elements, useCustomCreativeTab);

            createDataTags(workDir, modId, elements);
            createRecipes(workDir, modId, elements);

            if (logger != null) logger.accept("Starting Gradle build (this may take a while)...");
            buildMod(workDir, outputDir, logger);

            File builtJar = new File(workDir, "build/libs/" + modId + "-1.0.0.jar");
            if (builtJar.exists()) {
                File finalJar = new File(outputDir, modId + ".jar");
                Files.copy(builtJar.toPath(), finalJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                success = true;
            } else {
                throw new IOException("Compiled JAR was not found at " + builtJar.getAbsolutePath() + ". See gradle_build.log for details.");
            }

        } finally {
            if (success) {
                deleteDirectory(workDir);
            } else {
                System.err.println("Mod generation failed. Project files kept in: " + workDir.getAbsolutePath());
            }
        }
    }

    private static void downloadMDK(File destination) throws IOException {
        URL url = new URL(FORGE_MDK_URL);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void unzip(File zipFile, File destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private static void deleteDirectory(File dir) throws IOException {
        if (!dir.exists()) return;
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void updateGradleProperties(File file, String modId, String modName) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        content = content.replace("mod_id=examplemod", "mod_id=" + modId);
        content = content.replace("mod_name=Example Mod", "mod_name=" + modName);
        content = content.replace("mod_group_id=com.example.examplemod", "mod_group_id=com.yourname." + modId);
        Files.write(file.toPath(), content.getBytes());
    }

    private static void writeJavaClasses(File dir, String group, String modId, String modName, List<ModElement> elements, boolean useCustomCreativeTab) throws IOException {
        StringBuilder registrations = new StringBuilder();
        StringBuilder tabEntries = new StringBuilder();
        StringBuilder renderTypeSetup = new StringBuilder();

        for (int i = 0; i < elements.size(); i++) {
            ModElement el = elements.get(i);
            String id = el.getName().toLowerCase().replace(" ", "_");
            String constName = "E_" + i;

            if (el.isBlock()) {
                BlockData bd = (BlockData) el;
                String toolType = bd.getToolType();
                String properties = "BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(" + bd.getBreakingTime() + "f, " + bd.getResistance() + "f).lightLevel(s -> " + bd.getLightLevel() + ").sound(SoundType." + bd.getSoundType() + ")";
                if (bd.isTransparent()) {
                    properties += ".noOcclusion()";
                    renderTypeSetup.append("            ItemBlockRenderTypes.setRenderLayer(BLOCK_").append(constName).append(".get(), RenderType.cutout());\n");
                }
                if (!"Hand".equals(toolType) && !"Sword".equals(toolType)) {
                    properties += ".requiresCorrectToolForDrops()";
                }

                registrations.append("    public static final RegistryObject<Block> BLOCK_").append(constName).append(" = BLOCKS.register(\"").append(id).append("\", \n")
                             .append("            () -> new Block(").append(properties).append("));\n\n");

                registrations.append("    public static final RegistryObject<Item> ITEM_").append(constName).append(" = ITEMS.register(\"").append(id).append("\", \n")
                             .append("            () -> new BlockItem(BLOCK_").append(constName).append(".get(), new Item.Properties()));\n\n");
                tabEntries.append("                        output.accept(ITEM_").append(constName).append(".get());\n");
            } else if (el.isItem()) {
                ItemData idata = (ItemData) el;
                String props = "new Item.Properties().stacksTo(" + idata.getMaxStackSize() + ").rarity(Rarity." + idata.getRarity() + ")";
                
                if (idata.isFood()) {
                    props += ".food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(" + idata.getNutrition() + ").saturationMod(" + idata.getSaturation() + "f).build())";
                }
                
                String itemCode = "";
                if (idata.isTool()) {
                    String tier = "Tiers." + idata.getToolTier().toUpperCase();
                    switch (idata.getToolType()) {
                        case "Sword":
                            itemCode = "new SwordItem(" + tier + ", 3, -2.4f, " + props + ")";
                            break;
                        case "Pickaxe":
                            itemCode = "new PickaxeItem(" + tier + ", 1, -2.8f, " + props + ")";
                            break;
                        case "Axe":
                            itemCode = "new AxeItem(" + tier + ", 5.0f, -3.0f, " + props + ")";
                            break;
                        case "Shovel":
                            itemCode = "new ShovelItem(" + tier + ", 1.5f, -3.0f, " + props + ")";
                            break;
                        case "Hoe":
                            itemCode = "new HoeItem(" + tier + ", -2, -3.0f, " + props + ")";
                            break;
                    }
                } else {
                    itemCode = "new Item(" + props + ")";
                }

                if (idata.isGlowing()) {
                    itemCode += " {\n" +
                                "                @Override\n" +
                                "                public boolean isFoil(ItemStack stack) {\n" +
                                "                    return true;\n" +
                                "                }\n" +
                                "            }";
                }

                registrations.append("    public static final RegistryObject<Item> ITEM_").append(constName).append(" = ITEMS.register(\"").append(id).append("\", \n")
                             .append("            () -> ").append(itemCode).append(");\n\n");
                
            } else if (el.isEntity()) {
                EntityData ed = (EntityData) el;
                String entityClassName = ed.getName().replace(" ", "") + "Entity";
                
                registrations.append("    public static final RegistryObject<EntityType<").append(entityClassName).append(">> ").append(constName).append("_ENTITY = ENTITIES.register(\"").append(id).append("\",\n")
                             .append("            () -> EntityType.Builder.of(").append(entityClassName).append("::new, MobCategory.CREATURE).sized(0.6f, 1.8f).build(\"").append(id).append("\"));\n\n");
                
                String pColor = ed.getPrimaryColor().replace("#", "0x");
                if (!pColor.startsWith("0x")) pColor = "0xFFFFFF"; // Fallback
                String sColor = ed.getSecondaryColor().replace("#", "0x");
                if (!sColor.startsWith("0x")) sColor = "0x000000"; // Fallback
                
                registrations.append("    public static final RegistryObject<Item> ").append(constName).append("_EGG = ITEMS.register(\"").append(id).append("_spawn_egg\",\n")
                             .append("            () -> new net.minecraftforge.common.ForgeSpawnEggItem(").append(constName).append("_ENTITY, ").append(pColor).append(", ").append(sColor).append(", new Item.Properties()));\n\n");
                tabEntries.append("                        output.accept(").append(constName).append("_EGG.get());\n");
                
                writeEntityClasses(dir, group, modId, id, entityClassName, ed);
            }
        }

        StringBuilder attributeCreation = new StringBuilder();
        StringBuilder layerRegistration = new StringBuilder();
        StringBuilder rendererRegistration = new StringBuilder();

        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).isEntity()) {
                String constName = "E_" + i;
                String entityClassName = elements.get(i).getName().replace(" ", "") + "Entity";
                attributeCreation.append("        event.put(").append(constName).append("_ENTITY.get(), ").append(entityClassName).append(".createAttributes().build());\n");
                layerRegistration.append("        event.registerLayerDefinition(").append(entityClassName.replace("Entity", "Model")).append(".LAYER_LOCATION, ").append(entityClassName.replace("Entity", "Model")).append("::createBodyLayer);\n");
                rendererRegistration.append("        event.registerEntityRenderer(").append(constName).append("_ENTITY.get(), ").append(entityClassName.replace("Entity", "Renderer")).append("::new);\n");
            }
        }

        String modClassCode = 
            "package " + group + ";\n" +
            "\n" +
            "import net.minecraft.world.item.*;\n" +
            "import net.minecraft.core.registries.Registries;\n" +
            "import net.minecraft.network.chat.Component;\n" +
            "import net.minecraft.world.level.block.Block;\n" +
            "import net.minecraft.world.level.block.Blocks;\n" +
            "import net.minecraft.world.level.block.SoundType;\n" +
            "import net.minecraft.world.level.block.state.BlockBehaviour;\n" +
            "import net.minecraft.world.level.material.MapColor;\n" +
            "import net.minecraft.world.entity.EntityType;\n" +
            "import net.minecraft.world.entity.MobCategory;\n" +
            "import net.minecraft.world.item.Items;\n" +
            "import net.minecraftforge.api.distmarker.Dist;\n" +
            "import net.minecraftforge.common.MinecraftForge;\n" +
            "import net.minecraft.client.renderer.ItemBlockRenderTypes;\n" +
            "import net.minecraft.client.renderer.RenderType;\n" +
            "import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;\n" +
            "import net.minecraft.resources.ResourceLocation;\n" +
            "import net.minecraftforge.event.entity.EntityAttributeCreationEvent;\n" +
            "import net.minecraftforge.client.event.EntityRenderersEvent;\n" +
            "import net.minecraftforge.eventbus.api.IEventBus;\n" +
            "import net.minecraftforge.eventbus.api.SubscribeEvent;\n" +
            "import net.minecraftforge.fml.common.Mod;\n" +
            "import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;\n" +
            "import net.minecraftforge.registries.DeferredRegister;\n" +
            "import net.minecraftforge.registries.ForgeRegistries;\n" +
            "import net.minecraftforge.registries.RegistryObject;\n" +
            "\n" +
            "@Mod(\"" + modId + "\")\n" +
            "public class MainMod {\n" +
            "    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, \"" + modId + "\");\n" +
            "    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, \"" + modId + "\");\n" +
            "    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, \"" + modId + "\");\n" +
            "\n" +
            registrations.toString() +
            (useCustomCreativeTab ?
                "    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, \"" + modId + "\");\n" +
                "    public static final RegistryObject<CreativeModeTab> MOD_TAB = TABS.register(\"mod_tab\",\n" +
                "            () -> CreativeModeTab.builder().title(Component.translatable(\"creativetab." + modId + "\"))\n" +
                "                    .icon(() -> Items.DIAMOND.getDefaultInstance())\n" +
                "                    .displayItems((parameters, output) -> {\n" +
                tabEntries.toString() +
                "                    }).build());\n\n"
            : "") +
            "    public MainMod() {\n" +
            "        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();\n" +
            "        BLOCKS.register(modEventBus);\n" +
            "        ITEMS.register(modEventBus);\n" +
            "        ENTITIES.register(modEventBus);\n" +
            (useCustomCreativeTab ? "        TABS.register(modEventBus);\n" : "") +
            "        MinecraftForge.EVENT_BUS.register(this);\n" +
            "    }\n" +
            "\n" +
            "    @Mod.EventBusSubscriber(modid = \"" + modId + "\", bus = Mod.EventBusSubscriber.Bus.MOD)\n" +
            "    public static class ModEvents {\n" +
            "        @SubscribeEvent\n" +
            "        public static void onAttributeCreate(EntityAttributeCreationEvent event) {\n" +
            attributeCreation.toString() +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    @Mod.EventBusSubscriber(modid = \"" + modId + "\", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)\n" +
            "    public static class ClientEvents {\n" +
            "        @SubscribeEvent\n" +
            "        public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {\n" +
            layerRegistration.toString() +
            "        }\n" +
            "        @SubscribeEvent\n" +
            "        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {\n" +
            rendererRegistration.toString() +
            "        }\n" +
            (renderTypeSetup.length() > 0 ?
                "        @SubscribeEvent\n" +
                "        public static void onClientSetup(FMLClientSetupEvent event) {\n" +
                "            event.enqueueWork(() -> {\n" +
                renderTypeSetup.toString() +
                "            });\n" +
                "        }\n"
            : "") +
            "    }\n" +
            "}\n";

        Files.write(new File(dir, "MainMod.java").toPath(), modClassCode.getBytes());
    }

    private static void writeEntityClasses(File dir, String group, String modId, String id, String className, EntityData data) throws IOException {
        String modelName = className.replace("Entity", "Model");
        String rendererName = className.replace("Entity", "Renderer");
        
        // --- Entity Class ---
        StringBuilder goals = new StringBuilder();
        goals.append("        this.goalSelector.addGoal(0, new FloatGoal(this));\n");
        
        if (data.isTimid()) {
            goals.append("        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));\n");
            goals.append("        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.0D, 1.2D));\n");
        }
        
        if (data.canMelee()) {
            goals.append("        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, true));\n");
            goals.append("        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));\n");
        }
        
        if (data.canLeap()) {
            goals.append("        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));\n");
        }
        
        if (data.isFollower()) {
            goals.append("        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, Ingredient.of(net.minecraft.world.item.Items.BONE), false));\n");
        }

        String strollGoal = data.avoidsWater() ? "new WaterAvoidingRandomStrollGoal(this, 1.0D)" : "new RandomStrollGoal(this, 1.0D)";
        goals.append("        this.goalSelector.addGoal(4, ").append(strollGoal).append(");\n");
        goals.append("        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));\n");

        String aiStep = "";
        if (data.burnsInSun()) {
            aiStep = 
                "    @Override\n" +
                "    public void aiStep() {\n" +
                "        if (this.level().isDay() && !this.level().isClientSide) {\n" +
                "            float f = this.getLightLevelDependentMagicValue();\n" +
                "            if (f > 0.5F && this.level().canSeeSky(this.blockPosition())) {\n" +
                "                this.setSecondsOnFire(8);\n" +
                "            }\n" +
                "        }\n" +
                "        super.aiStep();\n" +
                "    }\n\n";
        }

        String entityCode = 
            "package " + group + ";\n\n" +
            "import net.minecraft.world.entity.*;\n" +
            "import net.minecraft.world.entity.ai.goal.*;\n" +
            "import net.minecraft.world.entity.ai.attributes.*;\n" +
            "import net.minecraft.world.entity.player.Player;\n" +
            "import net.minecraft.world.item.crafting.Ingredient;\n" +
            "import net.minecraft.world.level.Level;\n\n" +
            "public class " + className + " extends PathfinderMob {\n" +
            "    public " + className + "(EntityType<? extends PathfinderMob> type, Level level) {\n" +
            "        super(type, level);\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    protected void registerGoals() {\n" +
            "        " + goals.toString().trim() + "\n" +
            "    }\n\n" +
            aiStep +
            "    public static AttributeSupplier.Builder createAttributes() {\n" +
            "        return Mob.createMobAttributes()\n" +
            "            .add(Attributes.MAX_HEALTH, " + data.getMaxHealth() + ".0D)\n" +
            "            .add(Attributes.MOVEMENT_SPEED, " + data.getMovementSpeed() + "D)\n" +
            "            .add(Attributes.ATTACK_DAMAGE, " + data.getAttackDamage() + "D);\n" +
            "    }\n" +
            "}\n";
        Files.write(new File(dir, className + ".java").toPath(), entityCode.getBytes());

        // --- Model Class ---
        if (data.getModelJavaFile() != null) {
            String originalContent = Files.readString(data.getModelJavaFile().toPath());
            // Capture the original class name before renaming so we can rename the constructor too
            java.util.regex.Matcher classMatcher = java.util.regex.Pattern.compile("public class (\\w+)").matcher(originalContent);
            String originalClassName = classMatcher.find() ? classMatcher.group(1) : null;
            // Strip package and all existing imports, then inject correct ones
            String strippedContent = originalContent
                .replaceAll("package [^;]+;\\s*", "")
                .replaceAll("import [^;]+;\\s*", "");
            String correctImports =
                "package " + group + ";\n\n" +
                "import com.mojang.blaze3d.vertex.PoseStack;\n" +
                "import com.mojang.blaze3d.vertex.VertexConsumer;\n" +
                "import net.minecraft.client.model.EntityModel;\n" +
                "import net.minecraft.client.model.geom.ModelLayerLocation;\n" +
                "import net.minecraft.client.model.geom.ModelPart;\n" +
                "import net.minecraft.client.model.geom.PartPose;\n" +
                "import net.minecraft.client.model.geom.builders.*;\n" +
                "import net.minecraft.resources.ResourceLocation;\n" +
                "import net.minecraft.world.entity.Entity;\n\n";
            String processedContent = correctImports + strippedContent.trim() + "\n";
            processedContent = processedContent.replaceAll("public class \\w+(<[^>]+>)?", "public class " + modelName + "<T extends Entity>");
            // Rename constructor to match new class name
            if (originalClassName != null) {
                processedContent = processedContent.replaceAll("public " + originalClassName + "\\(", "public " + modelName + "(");
            }
            processedContent = processedContent.replaceAll("new ResourceLocation\\(\"[^\"]+\", \"[^\"]+\"\\)",
                                                         "new ResourceLocation(\"" + modId + "\", \"" + id + "\")");
            Files.write(new File(dir, modelName + ".java").toPath(), processedContent.getBytes());
        } else {
            String modelCode = 
                "package " + group + ";\n\n" +
                "import net.minecraft.client.model.HierarchicalModel;\n" +
                "import net.minecraft.client.model.geom.ModelLayerLocation;\n" +
                "import net.minecraft.client.model.geom.ModelPart;\n" +
                "import net.minecraft.client.model.geom.PartPose;\n" +
                "import net.minecraft.client.model.geom.builders.*;\n" +
                "import net.minecraft.resources.ResourceLocation;\n" +
                "import net.minecraft.world.entity.Entity;\n\n" +
                "public class " + modelName + "<T extends Entity> extends HierarchicalModel<T> {\n" +
                "    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(\"" + modId + "\", \"" + id + "\"), \"main\");\n" +
                "    private final ModelPart root;\n\n" +
                "    public " + modelName + "(ModelPart root) {\n" +
                "        this.root = root.getChild(\"main\");\n" +
                "    }\n\n" +
                "    public static LayerDefinition createBodyLayer() {\n" +
                "        MeshDefinition meshdefinition = new MeshDefinition();\n" +
                "        PartDefinition partdefinition = meshdefinition.getRoot();\n" +
                "        partdefinition.addOrReplaceChild(\"main\", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));\n" +
                "        return LayerDefinition.create(meshdefinition, 64, 64);\n" +
                "    }\n\n" +
                "    @Override\n" +
                "    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}\n\n" +
                "    @Override\n" +
                "    public ModelPart root() { return this.root; }\n" +
                "}\n";
            Files.write(new File(dir, modelName + ".java").toPath(), modelCode.getBytes());
        }

        // --- Renderer Class ---
        String rendererCode = 
            "package " + group + ";\n\n" +
            "import net.minecraft.client.renderer.entity.EntityRendererProvider;\n" +
            "import net.minecraft.client.renderer.entity.MobRenderer;\n" +
            "import net.minecraft.resources.ResourceLocation;\n\n" +
            "public class " + rendererName + " extends MobRenderer<" + className + ", " + modelName + "<" + className + ">> {\n" +
            "    public " + rendererName + "(EntityRendererProvider.Context context) {\n" +
            "        super(context, new " + modelName + "<>(context.bakeLayer(" + modelName + ".LAYER_LOCATION)), 0.5f);\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    public ResourceLocation getTextureLocation(" + className + " entity) {\n" +
            "        return new ResourceLocation(\"" + modId + "\", \"textures/entity/" + id + ".png\");\n" +
            "    }\n" +
            "}\n";
        Files.write(new File(dir, rendererName + ".java").toPath(), rendererCode.getBytes());
    }

    private static void createAssets(File assetsDir, String modId, String modName, List<ModElement> elements, boolean useCustomCreativeTab) throws IOException {
        File blockstatesDir = new File(assetsDir, "blockstates");
        File modelsBlockDir = new File(assetsDir, "models/block");
        File modelsItemDir = new File(assetsDir, "models/item");
        File texturesBlockDir = new File(assetsDir, "textures/block");
        File texturesItemDir = new File(assetsDir, "textures/item");
        File texturesEntityDir = new File(assetsDir, "textures/entity");
        File langDir = new File(assetsDir, "lang");

        blockstatesDir.mkdirs();
        modelsBlockDir.mkdirs();
        modelsItemDir.mkdirs();
        texturesBlockDir.mkdirs();
        texturesItemDir.mkdirs();
        texturesEntityDir.mkdirs();
        langDir.mkdirs();

        StringBuilder langEntries = new StringBuilder();

        for (int i = 0; i < elements.size(); i++) {
            ModElement el = elements.get(i);
            String id = el.getName().toLowerCase().replace(" ", "_");

            if (i > 0) langEntries.append(",\n");

            if (el.isBlock()) {
                BlockData bd = (BlockData) el;
                String blockstatesJSON = "{\n  \"variants\": {\n    \"\": { \"model\": \"" + modId + ":block/" + id + "\" }\n  }\n}";
                Files.write(new File(blockstatesDir, id + ".json").toPath(), blockstatesJSON.getBytes());

                String modelBlockJSON = "{\n" +
                                        "  \"parent\": \"minecraft:block/cube_bottom_top\",\n" +
                                        "  \"textures\": {\n" +
                                        "    \"top\": \"" + modId + ":block/" + id + "_top\",\n" +
                                        "    \"bottom\": \"" + modId + ":block/" + id + "_bottom\",\n" +
                                        "    \"side\": \"" + modId + ":block/" + id + "_side\"\n" +
                                        "  }\n" +
                                        "}";
                Files.write(new File(modelsBlockDir, id + ".json").toPath(), modelBlockJSON.getBytes());

                String modelItemJSON = "{\n  \"parent\": \"" + modId + ":block/" + id + "\"\n}";
                Files.write(new File(modelsItemDir, id + ".json").toPath(), modelItemJSON.getBytes());

                Files.copy(bd.getTopTexture().toPath(), new File(texturesBlockDir, id + "_top.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(bd.getBottomTexture().toPath(), new File(texturesBlockDir, id + "_bottom.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(bd.getSideTexture().toPath(), new File(texturesBlockDir, id + "_side.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                langEntries.append("  \"block.").append(modId).append(".").append(id).append("\": \"").append(el.getName()).append("\"");
            } else if (el.isItem()) {
                String modelItemJSON = "{\n  \"parent\": \"minecraft:item/generated\",\n  \"textures\": {\n    \"layer0\": \"" + modId + ":item/" + id + "\"\n  }\n}";
                Files.write(new File(modelsItemDir, id + ".json").toPath(), modelItemJSON.getBytes());

                Files.copy(el.getTextureFile().toPath(), new File(texturesItemDir, id + ".png").toPath(), StandardCopyOption.REPLACE_EXISTING);
                langEntries.append("  \"item.").append(modId).append(".").append(id).append("\": \"").append(el.getName()).append("\"");
            } else if (el.isEntity()) {
                Files.copy(el.getTextureFile().toPath(), new File(texturesEntityDir, id + ".png").toPath(), StandardCopyOption.REPLACE_EXISTING);
                String modelEggJSON = "{\n  \"parent\": \"minecraft:item/template_spawn_egg\"\n}";
                Files.write(new File(modelsItemDir, id + "_spawn_egg.json").toPath(), modelEggJSON.getBytes());
                
                langEntries.append("  \"entity.").append(modId).append(".").append(id).append("\": \"").append(el.getName()).append("\",\n");
                langEntries.append("  \"item.").append(modId).append(".").append(id).append("_spawn_egg\": \"").append(el.getName()).append(" Spawn Egg\"");
            }
        }

        if (useCustomCreativeTab) {
            langEntries.append(",\n  \"creativetab.").append(modId).append("\": \"").append(modName).append(" Tab\"");
        }
        String langJSON = "{\n" + langEntries.toString() + "\n}";
        Files.write(new File(langDir, "en_us.json").toPath(), langJSON.getBytes());
    }

    private static void createDataTags(File workDir, String modId, List<ModElement> elements) throws IOException {
        File mineableDir = new File(workDir, "src/main/resources/data/minecraft/tags/blocks/mineable");
        mineableDir.mkdirs();

        for (String tool : new String[]{"Pickaxe", "Axe", "Shovel", "Hoe"}) {
            StringBuilder tagValues = new StringBuilder();
            boolean hasEntries = false;

            for (ModElement el : elements) {
                if (el.isBlock()) {
                    BlockData data = (BlockData) el;
                    if (data.getToolType().equals(tool)) {
                        if (hasEntries) tagValues.append(",\n");
                        tagValues.append("    \"").append(modId).append(":").append(data.getName().toLowerCase().replace(" ", "_")).append("\"");
                        hasEntries = true;
                    }
                }
            }

            if (hasEntries) {
                String json = "{\n  \"replace\": false,\n  \"values\": [\n" + tagValues.toString() + "\n  ]\n}";
                Files.write(new File(mineableDir, tool.toLowerCase() + ".json").toPath(), json.getBytes());
            }
        }
    }

    private static String findBundledJdkPath() {
        try {
            // Find the directory where the running JAR/class is located
            File appDir = new File(ModGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            // Check for bundled JDK in common locations relative to the app
            File[] candidates = {
                new File(appDir, "jdk"),
                new File(appDir, "../jdk"),
                new File(appDir, "../../jdk"),
            };
            for (File candidate : candidates) {
                if (candidate.exists() && candidate.isDirectory()) {
                    return candidate.getCanonicalPath();
                }
            }
        } catch (Exception e) {
            // Fall through to return null
        }
        return null;
    }

    private static void buildMod(File workDir, File outputDir, java.util.function.Consumer<String> logger) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        if (!isWindows) {
            Process chmodProcess = new ProcessBuilder("chmod", "+x", "gradlew")
                    .directory(workDir)
                    .start();
            chmodProcess.waitFor();
        }

        String gradleCmd = isWindows ? "gradlew.bat" : "./gradlew";
        ProcessBuilder pb = new ProcessBuilder(gradleCmd, "build")
            .directory(workDir)
            .redirectErrorStream(true);

        // Set JAVA_HOME to bundled JDK if available (for portable .exe builds)
        String bundledJdk = findBundledJdkPath();
        if (bundledJdk != null) {
            if (logger != null) logger.accept("Using bundled JDK: " + bundledJdk);
            pb.environment().put("JAVA_HOME", bundledJdk);
        }

        Process buildProcess = pb.start();

        StringBuilder buildLog = new StringBuilder();
        java.util.Queue<String> lastLines = new java.util.LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(buildProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (logger != null) logger.accept(line);
                buildLog.append(line).append("\n");
                lastLines.add(line);
                if (lastLines.size() > 10) {
                    lastLines.poll();
                }
            }
        }

        int exitCode = buildProcess.waitFor();
        if (exitCode != 0) {
            File logFile = new File(outputDir, "gradle_build.log");
            Files.write(logFile.toPath(), buildLog.toString().getBytes());

            String shortError = String.join("\n", lastLines);
            throw new IOException("Gradle build failed with exit code " + exitCode + ".\n" +
                                "Last 10 lines of output:\n" + shortError + "\n\n" +
                                "Detailed log saved to: " + logFile.getAbsolutePath());
        }
    }

    private static void createRecipes(File workDir, String modId, List<ModElement> elements) throws IOException {
        File recipeDir = new File(workDir, "src/main/resources/data/" + modId + "/recipes");
        recipeDir.mkdirs();

        for (ModElement el : elements) {
            Recipe recipe = null;
            if (el.isBlock()) recipe = ((BlockData) el).getRecipe();
            else if (el.isItem()) recipe = ((ItemData) el).getRecipe();

            if (recipe == null || recipe.isEmpty()) continue;

            String nameId = el.getName().toLowerCase().replace(" ", "_");
            String[][] grid = recipe.getGrid();

            StringBuilder pattern = new StringBuilder("    [\n");
            StringBuilder keys = new StringBuilder("    \"key\": {\n");
            
            char nextKey = 'A';
            java.util.Map<String, Character> ingredientToKey = new java.util.HashMap<>();

            for (int r = 0; r < 3; r++) {
                pattern.append("      \"");
                for (int c = 0; c < 3; c++) {
                    String ing = grid[r][c];
                    if (ing == null || ing.isEmpty() || "minecraft:air".equals(ing)) {
                        pattern.append(" ");
                    } else {
                        if (!ingredientToKey.containsKey(ing)) {
                            ingredientToKey.put(ing, nextKey);
                            if (nextKey > 'A') keys.append(",\n");
                            keys.append("      \"").append(nextKey).append("\": { \"item\": \"").append(ing).append("\" }");
                            nextKey++;
                        }
                        pattern.append(ingredientToKey.get(ing));
                    }
                }
                pattern.append("\"");
                if (r < 2) pattern.append(",\n");
            }
            pattern.append("\n    ]");
            keys.append("\n    }");

            String json = "{\n" +
                          "  \"type\": \"minecraft:crafting_shaped\",\n" +
                          "  \"pattern\": \n" + pattern.toString() + ",\n" +
                          keys.toString() + ",\n" +
                          "  \"result\": {\n" +
                          "    \"item\": \"" + modId + ":" + nameId + "\",\n" +
                          "    \"count\": 1\n" +
                          "  }\n" +
                          "}";

            Files.write(new File(recipeDir, nameId + ".json").toPath(), json.getBytes());
        }
    }
}
