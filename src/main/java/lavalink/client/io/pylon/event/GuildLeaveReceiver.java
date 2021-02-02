package lavalink.client.io.pylon.event;

import lavalink.client.io.pylon.PylonLavalink;
import lavalink.client.io.pylon.PylonLink;
import lol.up.pylon.gateway.client.entity.event.GuildMemberRemoveEvent;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;

public class GuildLeaveReceiver extends AbstractEventReceiver<GuildMemberRemoveEvent> {

    private final PylonLavalink lavalink;

    public GuildLeaveReceiver(PylonLavalink lavalink) {
        this.lavalink = lavalink;
    }

    @Override
    protected void receive(final GuildMemberRemoveEvent event) {
        if(event.getMember().getId() != event.getBotId()) {
            return;
        }
        final PylonLink link = lavalink.getExistingLink(event.getGuild().complete());
        if (link != null) {
            link.removeConnection();
        }
    }
}
