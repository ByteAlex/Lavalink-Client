package lavalink.client.io.pylon;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import lavalink.client.io.Lavalink;
import lavalink.client.io.pylon.event.ChannelDeleteReceiver;
import lavalink.client.io.pylon.event.GuildLeaveReceiver;
import lavalink.client.io.pylon.event.VoiceServerUpdateReceiver;
import lavalink.client.io.pylon.event.VoiceStateUpdateReceiver;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.entity.event.ChannelDeleteEvent;
import lol.up.pylon.gateway.client.entity.event.GuildMemberRemoveEvent;
import lol.up.pylon.gateway.client.entity.event.VoiceServerUpdateEvent;
import lol.up.pylon.gateway.client.entity.event.VoiceStateUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PylonLavalink extends Lavalink<PylonLink> {

    private static final Logger log = LoggerFactory.getLogger(PylonLavalink.class);

    private final GatewayGrpcClient client;
    private boolean autoReconnect = true;

    public PylonLavalink(final GatewayGrpcClient client, int numShards) {
        super(String.valueOf(client.getDefaultBotId()), numShards);
        this.client = client;
        client.registerReceiver(VoiceServerUpdateEvent.class, new VoiceServerUpdateReceiver(this));
        client.registerReceiver(VoiceStateUpdateEvent.class, new VoiceStateUpdateReceiver(this));
        client.registerReceiver(GuildMemberRemoveEvent.class, new GuildLeaveReceiver(this));
        client.registerReceiver(ChannelDeleteEvent.class, new ChannelDeleteReceiver(this));

        /*
            Reconnect logic not implemented // todo
            if (autoReconnect) {
                getLinksMap().forEach((guildId, link) -> {
                    try {
                        //Note: We also ensure that the link belongs to the JDA object
                        if (link.getLastChannel() != null
                                && event.getJDA().getGuildById(guildId) != null) {
                            link.connect(event.getJDA().getVoiceChannelById(link.getLastChannel()), false);
                        }
                    } catch (Exception e) {
                        log.error("Caught exception while trying to reconnect link " + link, e);
                    }
                });
            }
         */
    }

    @SuppressWarnings("unused")
    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    @SuppressWarnings("unused")
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public GatewayGrpcClient getClient() {
        return client;
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public PylonLink getLink(Guild guild) {
        return getLink(String.valueOf(guild.getId()));
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @Nullable
    public PylonLink getExistingLink(Guild guild) {
        return getExistingLink(String.valueOf(guild.getId()));
    }

    @Override
    protected PylonLink buildNewLink(String guildId) {
        return new PylonLink(this, guildId);
    }
}
