package net.enchadd;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.LegacyEnchantCompat;
import net.enchadd.security.PluginProtection;
import net.enchadd.utils.LangManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class EnchADDBootstrap implements PluginBootstrap {

    private final Logger logger = LoggerFactory.getLogger("EnchADD");

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        PluginProtection.verifyBootstrap(logger);
        try {
            EnchADDConfig.init(context.getDataDirectory());
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Failed to initialize EnchADD configuration", e);
        }
        // 在 bootstrap 阶段提前初始化语言，确保附魔注册时 description 为可读文本而非未解析 key
        LangManager.init(context.getDataDirectory(), EnchADDConfig.getLanguage());

        Collection<EnchADDEnchant> enchaddEnchants = new ArrayList<>(EnchADDConfig.ENCHANTS.values());
        Collection<EnchADDEnchant> legacyCompatEnchants = LegacyEnchantCompat.bootstrapEnchants();
        enchaddEnchants.addAll(legacyCompatEnchants);
        logger.info(
                "Preparing {} active custom enchants and {} legacy compatibility enchants for bootstrap registration",
                EnchADDConfig.ENCHANTS.size(),
                legacyCompatEnchants.size()
        );

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ITEM).newHandler(event -> {
            for (EnchADDEnchant enchant : enchaddEnchants) {
                logger.debug("Registering item tag {}", enchant.getTagForSupportedItems().key());
                event.registrar().addToTag(
                        ItemTypeTagKeys.create(enchant.getTagForSupportedItems().key()),
                        enchant.getSupportedItems()
                );
            }
        }));

        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            for (EnchADDEnchant enchant : enchaddEnchants) {
                logger.debug("Registering enchantment {}", enchant.getKey());

                // Ensure the tag is registered before calling getOrCreateTag
                event.getOrCreateTag(enchant.getTagForSupportedItems());

                event.registry().register(TypedKey.create(RegistryKey.ENCHANTMENT, enchant.getKey()), enchantment -> {
                    enchantment.description(enchant.getDescriptionComponent());
                    enchantment.anvilCost(enchant.getAnvilCost());
                    enchantment.maxLevel(enchant.getMaxLevel());
                    enchantment.weight(enchant.getWeight());
                    enchantment.minimumCost(enchant.getMinimumCost());
                    enchantment.maximumCost(enchant.getMaximumCost());
                    enchantment.activeSlots(enchant.getActiveSlots());
                    enchantment.supportedItems(event.getOrCreateTag(enchant.getTagForSupportedItems()));
                });
            }
        }));

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
            for (EnchADDEnchant enchant : enchaddEnchants) {
                enchant.getEnchantTagKeys().forEach(enchantmentTagKey -> event.registrar().addToTag(enchantmentTagKey, Set.of(enchant.getTagEntry())));
            }
        }));

    }

}
