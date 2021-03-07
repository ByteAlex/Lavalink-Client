package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nullable;

public class RemotePlayerInfo {

    @Nullable
    final AudioTrack playing;
    final boolean paused;
    final long position;
    final long updateTime;

    @Nullable
    final AudioTrack lastTrack;

    public RemotePlayerInfo(@Nullable AudioTrack playing, boolean paused, long position, long updateTime, @Nullable AudioTrack lastTrack) {
        this.playing = playing;
        this.paused = paused;
        this.position = position;
        this.updateTime = updateTime;
        this.lastTrack = lastTrack;
    }

    public boolean isPaused() {
        return paused;
    }

    public long getPosition() {
        return position;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    @Nullable
    public AudioTrack getPlaying() {
        return playing;
    }

    @Nullable
    public AudioTrack getLastTrack() {
        return lastTrack;
    }
}
