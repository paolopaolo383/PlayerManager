package playermanager.playermanager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class PlayerManager extends JavaPlugin implements Listener, CommandExecutor {
    public int playerhead = 1;
    private ProtocolManager protocolManager;
    public HashMap<UUID, Inventory> inv = new HashMap<UUID, Inventory>();
    ConsoleCommandSender consol = Bukkit.getConsoleSender();
    public void onLoad() {

    }
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        for(Player player : getServer().getOnlinePlayers())
        {
            inv.put(player.getUniqueId(), Bukkit.createInventory(null, 54, "playerinfo"));
        }
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(
                this,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPlayer().isSneaking())
                    return;
                PacketContainer packet = event.getPacket();
                //consol.sendMessage(ChatColor.YELLOW + String.valueOf(packet.getIntegers().size()) + ChatColor.BLUE + packet.getIntegers().getValues().toString() + ChatColor.RED + getServer().getPlayer("LastPieceOfLife").getEntityId());
                //consol.sendMessage(ChatColor.YELLOW + String.valueOf(packet.getFloat().size()) + ChatColor.GREEN + packet.getFloat().getValues().toString());
                //consol.sendMessage(ChatColor.YELLOW + packet.getFloat().getValues().toString());
                 //entity
                try{

                    String what = packet.getStructures().getValues().toString();

                    if(!what.contains("INTERACT_AT"))
                    {
                        if(!what.contains("MAIN_HAND")||what.contains("OFF_HAND"))
                        {
                            return;
                        }
                    }
                }
                catch (Exception ex)
                {
                    return;
                }
                Integer Target = packet.getIntegers().read(0);
                for (Player e : getServer().getOnlinePlayers()) {
                    if (e.getEntityId() == Target) {
                        openInventory(event.getPlayer(), e.getUniqueId());
                    }
                }
            }
        });
    }
    public void openInventory(final Player ent, UUID target) {
        initializeItems(target);

        ent.openInventory(inv.get(target));
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {

        inv.put(e.getPlayer().getUniqueId(), Bukkit.createInventory(null, 54, "playerinfo"));

    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public void initializeItems(UUID target) {
        Player player = Bukkit.getPlayer(target);
        inv.get(target).clear();
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(player.getDisplayName());
        skullMeta.setOwner(ChatColor.stripColor(player.getDisplayName()));
        skull.setItemMeta(skullMeta);
        inv.get(target).setItem(playerhead,skull);
    }
    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

}
