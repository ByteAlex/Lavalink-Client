package lavalink.client.io.pylon.event;

import lavalink.client.io.pylon.PylonLavalink;
import lol.up.pylon.gateway.client.entity.event.ReadyEvent;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectReceiver extends AbstractEventReceiver<ReadyEvent> {

    private final PylonLavalink lavalink;
    private final Logger logger = LoggerFactory.getLogger(ReconnectReceiver.class);

    public ReconnectReceiver(PylonLavalink lavalink) {
        this.lavalink = lavalink;

    }

    @Override
    protected void receive(final ReadyEvent event) {
        boolean autoReconnect = lavalink.getAutoReconnect();
        if (!autoReconnect) return;
        lavalink.getLinksMap().forEach((guildId, link) -> {
            try {

                if (link.getLastChannel() != null) {
                    final long guildIdLong = Long.parseLong(guildId);
                    final long channelId = Long.parseLong(link.getLastChannel());
                    event.getClient().getCacheService().getChannel(event.getBotId(), guildIdLong, channelId)
                            .queue(channel -> link.connect(channel, false));

                }
            } catch (Exception e) {
                logger.error("Caught exception while trying to reconnect link " + link, e);
            }
        });
    }
}
