package lavalink.client.io.pylon.event;

import lavalink.client.io.pylon.PylonLavalink;
import lavalink.client.io.pylon.PylonLink;
import lol.up.pylon.gateway.client.entity.event.ChannelDeleteEvent;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;

public class ChannelDeleteReceiver extends AbstractEventReceiver<ChannelDeleteEvent> {

    private final PylonLavalink lavalink;

    public ChannelDeleteReceiver(PylonLavalink lavalink) {
        this.lavalink = lavalink;
    }

    @Override
    protected void receive(ChannelDeleteEvent event) {
        final PylonLink link = lavalink.getExistingLink(event.getGuild().complete());
        if (link == null || !String.valueOf(event.getChannel().getId()).equals(link.getLastChannel())) {
            return;
        }

        link.removeConnection();
    }
}
