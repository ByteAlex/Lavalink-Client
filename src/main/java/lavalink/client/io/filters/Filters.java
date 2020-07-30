package lavalink.client.io.filters;

import lavalink.client.player.LavalinkPlayer;

import javax.annotation.Nonnull;

import static com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer.BAND_COUNT;

@SuppressWarnings("unused")
public class Filters {

    @SuppressWarnings("WeakerAccess")
    public static int DEFAULT_VOLUME = 100;
    public static double DEFAULT_SPEED = 1.0;
    public static double DEFAULT_RATE = 1.0;
    public static double DEFAULT_PITCH = 1.0;
    public static float DEFAULT_TREMOLO_FREQUENCY = 0.0f;
    public static float DEFAULT_TREMOLO_DEPTH = 0.0f;

    private final LavalinkPlayer player;
    private final Runnable onCommit;
    private int volume = DEFAULT_VOLUME;
    private double speed = DEFAULT_SPEED;
    private double rate = DEFAULT_RATE;
    private double pitch = DEFAULT_PITCH;
    private float tremoloFrequency = DEFAULT_VOLUME;
    private float tremoloDepth = DEFAULT_VOLUME;
    private float[] bands = new float[BAND_COUNT];

    /**
     * Intended for internal use only
     */
    public Filters(LavalinkPlayer player, Runnable onCommit) {
        this.player = player;
        this.onCommit = onCommit;
    }


    @Nonnull
    public LavalinkPlayer getPlayer() {
        return player;
    }

    public float[] getBands() {
        return bands;
    }

    /**
     * Configures the equalizer.
     *
     * @param band the band to change, values 0-14
     * @param gain the gain in volume for the given band, range -0.25 (mute) to 1.0 (quadruple).
     */
    public void setBand(int band, float gain) {
        if (gain < -0.25 || gain > 1) throw new IllegalArgumentException("Gain must be -0.25 to 1.0");
        bands[band] = gain;
    }

    public int getVolume() {
        return volume;
    }

    /**
     * @param volume where 1.0f is regular volume. Values greater than 1.0f are allowed, but may cause clipping.
     */
    public void setVolume(int volume) {
        if (volume < 0) throw new IllegalArgumentException("Volume must be greater than 0");
        this.volume = volume;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0) throw new IllegalArgumentException("Speed must be greater than 0");
        this.speed = speed;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        if (pitch < 0) throw new IllegalArgumentException("Pitch must be greater than 0");
        this.pitch = pitch;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Rate must be greater than 0");
        this.rate = rate;
    }

    public float getTremoloFrequency() {
        return tremoloFrequency;
    }

    public void setTremoloFrequency(float tremoloFrequency) {
        if (tremoloFrequency < 0) throw new IllegalArgumentException("TremoloFrequency must be greater than 0");
        this.tremoloFrequency = tremoloFrequency;
    }

    public float getTremoloDepth() {
        return tremoloDepth;
    }

    public void setTremoloDepth(float tremoloDepth) {
        if (tremoloDepth < 0) throw new IllegalArgumentException("TremoloDepth must be greater than 0");
        this.tremoloDepth = tremoloDepth;
    }


    /**
     * Commits these filters to the Lavalink server.
     * <p>
     * The client may choose to commit changes at any time, even if this method is never invoked.
     */
    public void commit() {
        onCommit.run();
    }
}