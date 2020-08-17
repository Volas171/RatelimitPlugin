package ratelimit;

import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.plugin.Plugin;

import java.util.HashMap;

public class main extends Plugin {
    public final int minEventsPerSec = 30;
    public HashMap<String, TempPlayerData> PlayerDataGroup = new HashMap<>();
    public void init() {
        Events.on(EventType.PlayerJoin.class, (event) -> {
            if(!PlayerDataGroup.containsKey(event.player.uuid)) {
                PlayerDataGroup.put(event.player.uuid,new TempPlayerData());
            }
        });
        Events.on(EventType.TapConfigEvent.class, (e) -> {
            if(e.player == null) return;
            TempPlayerData tdata = PlayerDataGroup.getOrDefault(e.player.uuid,null);
            if(tdata == null) return;
            tdata.eventsPerSecond++;
        });
        Events.on(EventType.PlayerLeave.class, (e) -> {
            PlayerDataGroup.remove(e.player.uuid);
        });
        Events.on(EventType.ServerLoadEvent.class, (e) -> {
            Vars.netServer.admins.addActionFilter((a) -> {
                if(a.player == null) return true;
                TempPlayerData tdata = PlayerDataGroup.getOrDefault(a.player.uuid, null);
                if(tdata == null) return true;
                if(tdata.interactUntil > 0) return false;
                return true;
            });
        });
        Timer.schedule(() -> {
            for(Player player:Vars.playerGroup) {
                TempPlayerData tdata = PlayerDataGroup.getOrDefault(player.uuid,null);
                if(tdata == null) return;
                if(tdata.interactUntil > 0) {
                    tdata.interactUntil--;
                }

                if(tdata.eventsPerSecond >= minEventsPerSec){
                    player.sendMessage("[scarlet]You can't interact for 5 seconds because you exceeded rate limit");
                    tdata.interactUntil = 5;
               
                tdata.eventsPerSecond = 0;
            }
        },0 ,1);
    }
}
