package com.enadd.gui.components;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;


/**
 * Builder for creating GUI buttons with modern Adventure API
 * Replaces deprecated setDisplayName/setLore methods with Adventure Components
 */
public final class GuiButtonBuilder {

    private Material material = Material.STONE;
    private Component displayName;
    private List<Component> lore = new ArrayList<>();
    private int amount = 1;
    private boolean hideAttributes = true;
    private boolean hideEnchants = true;

    private GuiButtonBuilder() {}

    public static GuiButtonBuilder create() {
        return new GuiButtonBuilder();
    }

    public GuiButtonBuilder material(Material material) {
        this.material = material;
        return this;
    }

    public GuiButtonBuilder displayName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.displayName = LegacyComponentSerializer.legacySection().deserialize(name)
                .decoration(TextDecoration.ITALIC, false);
        }
        return this;
    }

    public GuiButtonBuilder displayName(Component name) {
        if (name != null) {
            this.displayName = name.decoration(TextDecoration.ITALIC, false);
        }
        return this;
    }

    public GuiButtonBuilder lore(List<String> loreLines) {
        this.lore.clear();
        if (loreLines != null) {
            for (String line : loreLines) {
                if (line != null) {
                    this.lore.add(LegacyComponentSerializer.legacySection().deserialize(line)
                        .decoration(TextDecoration.ITALIC, false));
                }
            }
        }
        return this;
    }

    public GuiButtonBuilder lore(String... loreLines) {
        this.lore.clear();
        if (loreLines != null) {
            for (String line : loreLines) {
                if (line != null) {
                    this.lore.add(LegacyComponentSerializer.legacySection().deserialize(line)
                        .decoration(TextDecoration.ITALIC, false));
                }
            }
        }
        return this;
    }

    public GuiButtonBuilder loreComponents(List<Component> loreComponents) {
        this.lore.clear();
        if (loreComponents != null) {
            for (Component component : loreComponents) {
                if (component != null) {
                    this.lore.add(component.decoration(TextDecoration.ITALIC, false));
                }
            }
        }
        return this;
    }

    public GuiButtonBuilder addLoreLine(String line) {
        if (line != null) {
            this.lore.add(LegacyComponentSerializer.legacySection().deserialize(line)
                .decoration(TextDecoration.ITALIC, false));
        }
        return this;
    }

    public GuiButtonBuilder amount(int amount) {
        this.amount = Math.max(1, Math.min(64, amount));
        return this;
    }

    public GuiButtonBuilder hideAttributes(boolean hide) {
        this.hideAttributes = hide;
        return this;
    }

    public GuiButtonBuilder hideEnchants(boolean hide) {
        this.hideEnchants = hide;
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Use modern Adventure API methods instead of deprecated ones
            if (displayName != null) {
                meta.displayName(displayName);
            }

            if (!lore.isEmpty()) {
                meta.lore(lore);
            }

            // Add item flags
            if (hideAttributes) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
            if (hideEnchants) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Create a simple button with legacy string support
     */
    public static ItemStack createSimple(Material material, String displayName, String... lore) {
        return GuiButtonBuilder.create()
            .material(material)
            .displayName(displayName)
            .lore(lore)
            .build();
    }

    /**
     * Create a border item (empty glass pane)
     */
    public static ItemStack createBorder() {
        return GuiButtonBuilder.create()
            .material(Material.BLACK_STAINED_GLASS_PANE)
            .displayName("§0 ")
            .build();
    }

    /**
     * Create a navigation button
     */
    public static ItemStack createNavigation(String displayName, String... lore) {
        return GuiButtonBuilder.create()
            .material(Material.PAPER)
            .displayName(displayName)
            .lore(lore)
            .build();
    }
}
