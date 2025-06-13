package me.wolfity.util

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class ItemBuilder {

    private val itemStack: ItemStack
    private val meta: ItemMeta
    private val enchants: MutableMap<Enchantment, Int> = mutableMapOf()
    private var amount: Int = 1
    private var textureBase64: String? = null

    constructor(material: Material, name: Component) {
        itemStack = ItemStack(material)
        meta = itemStack.itemMeta ?: throw IllegalStateException("ItemMeta cannot be null")
        setName(name)
    }

    constructor(itemStack: ItemStack, initializer: (ItemBuilder.() -> Unit)? = null) {
        this.itemStack = itemStack
        this.meta = itemStack.itemMeta ?: throw IllegalStateException("ItemMeta cannot be null")
        initializer?.invoke(this)
    }

    constructor(itemStack: ItemStack, name: Component, initializer: (ItemBuilder.() -> Unit)? = null) {
        this.itemStack = itemStack
        this.meta = itemStack.itemMeta ?: throw IllegalStateException("ItemMeta cannot be null")
        meta.displayName(name)
        itemStack.itemMeta = meta
        initializer?.invoke(this)
    }

    constructor(material: Material, name: Component, initializer: (ItemBuilder.() -> Unit)? = null) : this(
        material,
        name
    ) {
        initializer?.invoke(this)
    }

    constructor(material: Material) : this(material, ItemStack(material).itemMeta?.displayName() ?: Component.text(""))

    fun setName(name: Component): ItemBuilder {
        meta.displayName(name.decoration(TextDecoration.ITALIC, false))
        return this
    }

    fun setLore(lore: List<Component>?): ItemBuilder {
        lore?.let {
            val formatted = lore.map { line -> line.decoration(TextDecoration.ITALIC, false) }
            meta.lore(formatted)
        }
        return this
    }

    fun addEnchant(enchantment: Enchantment, level: Int): ItemBuilder {
        enchants[enchantment] = level
        return this
    }

    fun addEnchants(newEnchants: Map<Enchantment, Int>): ItemBuilder {
        enchants.putAll(newEnchants)
        return this
    }

    fun setAmount(amount: Int): ItemBuilder {
        this.amount = amount
        return this
    }

    fun setCustomTexture(base64: String?): ItemBuilder {
        this.textureBase64 = base64
        return this
    }

    fun build(): ItemStack {
        itemStack.amount = amount

        if (textureBase64 != null && meta is SkullMeta) {
            val profile: PlayerProfile = Bukkit.createProfile(UUID.randomUUID())
            profile.setProperty(ProfileProperty("textures", textureBase64!!))
            (meta as SkullMeta).playerProfile = profile
        }

        itemStack.itemMeta = meta

        enchants.forEach { (enchant, level) ->
            itemStack.addUnsafeEnchantment(enchant, level)
        }

        return itemStack
    }
}
