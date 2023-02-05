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
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class PlayerManager extends JavaPlugin implements Listener, CommandExecutor {
    private ProtocolManager protocolManager;
    public int maxindex = 27;
    public Material background = Material.IRON_AXE;
    public int playerhead = -1;
    public int playerlevel = -1;
    public int playerjob = -1;

    public HashMap<Integer, String> cmd = new HashMap<Integer,String>();
    public HashMap<Integer, ItemStack> cmditem = new HashMap<Integer, ItemStack>();
    public HashMap<UUID, Inventory> inv = new HashMap<UUID, Inventory>();
    ConsoleCommandSender consol = Bukkit.getConsoleSender();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        reloadconfig();
        for(Player player : getServer().getOnlinePlayers())
        {
            inv.put(player.getUniqueId(), Bukkit.createInventory(null, maxindex, " "));

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

                    if(!what.contains("INTERACT_AT"))//interact////attack
                    {
                        return;
                    }
                    if(!what.contains("MAIN_HAND"))
                    {
                        return;
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
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        Player player = (Player) sender;
        if(!sender.isOp())
        {
            return true;
        }
        if(command.getName().equalsIgnoreCase("playermanager"))
        {
            reloadconfig();
        }
        return false;
    }
    public void reloadconfig()
    {
        for(Player player : getServer().getOnlinePlayers())
        {
            player.closeInventory();

        }
        cmd.clear();
        playerlevel = -1;
        playerjob =-1;
        playerhead =-1;
        File pluginfile = new File("plugins","PlayerManager.jar");
        consol.sendMessage(pluginfile.getAbsolutePath().split("PlayerManager.jar")[0]+"PlayerManager");
        String folderpath = pluginfile.getAbsolutePath().split("PlayerManager.jar")[0]+"PlayerManager";
        File Folder = new File(folderpath);
        if (!Folder.exists()) {
            try{
                Folder.mkdir(); //폴더 생성합니다.
                System.out.println("[PlayerManager]폴더가 생성되었습니다.");
            }
            catch(Exception e){
                e.getStackTrace();
            }
        }
        String Filepath = folderpath+"\\guisetting.yml";
        File cffile = new File(Filepath);
        if (!cffile.exists()) {	// 파일이 존재하지 않으면 생성
            try {
                if (cffile.createNewFile())
                    System.out.println("[guisetting.yml]파일 생성 성공");
                else
                    System.out.println("[guisetting.yml]파일 생성 실패");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File("plugins/PlayerManager", "guisetting.yml");
        FileConfiguration cnf = YamlConfiguration.loadConfiguration(file);
        consol.sendMessage(ChatColor.YELLOW+"---------------로드---------------");
        if(cnf.contains("gui")&&cnf.contains("command")){
            //consol.sendMessage("File exist");
            cnf = YamlConfiguration.loadConfiguration(file);
            List<String> guis = (List<String>) cnf.getList("gui");
            if(!(guis.isEmpty()))
            {
                for(String gui: guis)
                {
                    try
                    {
                        playerhead = Integer.valueOf(gui);
                    }
                    catch (Exception e)
                    {
                        consol.sendMessage(ChatColor.RED+"플레이어헤드가 로드되지 않음");
                    }
                }
            }
            else
            {
                consol.sendMessage(ChatColor.YELLOW+"플레이어헤드의 설정값이 없어 사라집니다.");
            }
            List<String> command = (List<String>) cnf.getList("command");
            if(!(command.isEmpty()))
            {
                for(String cmdd: command)
                {
                    try
                    {
                        cmd.put(Integer.valueOf(cmdd.split("/")[0]), cmdd.split("/")[1]);
                        ItemStack item = createGuidmaItem(Material.valueOf(cmdd.split("/")[2]),ChatColor.GRAY+cmdd.split("/")[3],Short.valueOf(cmdd.split("/")[4]), " ");
                        cmditem.put(Integer.valueOf(cmdd.split("/")[0]), item);
                        consol.sendMessage(ChatColor.RED+cmdd.split("/")[1]+"이 로드됨");
                    }
                    catch (Exception e)
                    {
                        consol.sendMessage(ChatColor.RED+cmdd.split("/")[1]+"이 로드되지 않음");
                    }

                }
            }
            else
            {
                consol.sendMessage(ChatColor.YELLOW+"로드할 command가 없음");
            }
        }else{
            //consol.sendMessage("File doesnt exist");
            String[] list = {"(몇번째칸인지)"};
            cnf.set("gui", list);
            String[] lists = {"(몇번째칸인지)/(커맨드)/(메테리얼ex)DIAMOND_SWORD)/(이름)/(내구도)"};
            cnf.set("command",lists);
            try {
                cnf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //consol.sendMessage("Created");
        }

        consol.sendMessage(ChatColor.YELLOW+"---------------완료---------------");
    }
    public void openInventory(final Player ent, UUID target) {
        initializeItems(target);

        ent.openInventory(inv.get(target));
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        for (HumanEntity player:inv.get(e.getPlayer().getUniqueId()).getViewers()) {
            player.closeInventory();
        }
    }
    @EventHandler
    public void onDead(PlayerDeathEvent e)
    {
        for (HumanEntity player:inv.get(e.getEntity().getUniqueId()).getViewers()) {
            player.closeInventory();
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();

        inv.put(e.getPlayer().getUniqueId(), Bukkit.createInventory(null, maxindex, "playerinfo"));
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public void initializeItems(UUID target) {
        Player player = Bukkit.getPlayer(target);
        inv.get(target).clear();
        for(int i = 0;i<maxindex;i++)
        {
            inv.get(target).setItem(i, createGuidmaItem(background," ", (short)64, " "));
        }
        inv.get(target).setItem(maxindex-1, createGuidmaItem(background," ", (short)11, " "));
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1,(short) 3);

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(ChatColor.GRAY+player.getDisplayName());
        List<String> lore = Arrays.asList(ChatColor.GRAY+"lv."+String.valueOf(player.getLevel()));
        skullMeta.setLore(lore);

        skullMeta.setOwner(ChatColor.stripColor(player.getDisplayName()));

        skull.setItemMeta(skullMeta);
        if(playerhead!=-1)
        {
            inv.get(target).setItem(playerhead, skull);
        }
        String job = "무직";
        //if(getServer().getPlayer(target).getInventory().)
        //inv.get(target).setItem(playerjob, createGuiItem(Material.IRON_INGOT,job, " "));
        //if(playerlevel!=-1)
        //{
        //    inv.get(target).setItem(playerlevel,createGuiItem(Material.DIAMOND,ChatColor.GRAY+"lv."+String.valueOf(player.getLevel())," "));
        //}

        for(int i = 0; i<maxindex;i++)
        {
            if(cmd.containsKey(i)&&cmditem.containsKey(i))
            {
                inv.get(target).setItem(i,cmditem.get(i));
            }
        }
    }
    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(null);

        item.setItemMeta(meta);

        return item;
    }
    protected ItemStack createGuidmaItem(final Material material, final String name, short gamage, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        item.setDurability(gamage);

        final ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        // Set the name of the item
        meta.setDisplayName(name);

        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }
    @EventHandler
    public void oninventoryclick(InventoryClickEvent e)
    {
        UUID uuid = null;
        try {
            uuid = getServer().getPlayer(ChatColor.stripColor(e.getInventory().getItem(playerhead).getItemMeta().getDisplayName())).getUniqueId();
            if (!e.getInventory().equals(inv.get(uuid))) return;
        }
        catch (Exception ee)
        {
            return;
        }
        Player player = (Player)e.getWhoClicked();
        //volume pitch

        e.setCancelled(true);
        if(cmd.containsKey(e.getRawSlot()))
        {
            getServer().dispatchCommand(e.getWhoClicked(),cmd.get(e.getRawSlot()).replace("{targetplayer}",getServer().getPlayer(uuid).getName()));
            e.getWhoClicked().closeInventory();
            player.playSound(player.getLocation(),Sound.ENTITY_CHICKEN_EGG,100, 1);
        }

    }
}
