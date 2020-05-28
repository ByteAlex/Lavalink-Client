package lavalink.client.io;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import com.sedmelluq.lava.common.tools.DaemonThreadFactory;
import lavalink.client.LavalinkUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public final class LavalinkRestClient {

    private static final Logger log = LoggerFactory.getLogger(LavalinkRestClient.class);

    private static final Function<DataObject, List<AudioTrack>> SEARCH_TRANSFORMER = loadResult -> {
        final List<AudioTrack> tracks = new ArrayList<>();
        final DataArray trackData = loadResult.getArray("tracks");
        for (int index = 0; index < trackData.length(); index++) {
            DataObject track = trackData.getObject(index);
            try {
                final AudioTrack audioTrack = LavalinkUtil.toAudioTrack(track.getString("track"));
                tracks.add(audioTrack);
            } catch (final IOException ex) {
                log.error("Error converting track", ex);
            }
        }
        return tracks;
    };


    private final LavalinkSocket socket;
    private final ExecutorService executorService;
    private Consumer<HttpClientBuilder> builderConsumer;

    public LavalinkRestClient(final LavalinkSocket socket) {
        this.socket = socket;
        this.executorService = Executors.newCachedThreadPool(new DaemonThreadFactory("Lavalink-RestExecutor-" + socket.getName()));
    }

    public void setHttpClientBuilder(final Consumer<HttpClientBuilder> clientBuilder) {
        this.builderConsumer = clientBuilder;
    }

    public CompletableFuture<List<AudioTrack>> getYoutubeSearchResult(final String query) {
        return load("ytsearch:" + query)
                .thenApplyAsync(SEARCH_TRANSFORMER);
    }

    public CompletableFuture<List<AudioTrack>> getSoundCloudSearchResult(final String query) {
        return load("scsearch:" + query)
                .thenApplyAsync(SEARCH_TRANSFORMER);
    }

    public CompletableFuture<Void> loadItem(final String identifier, final AudioLoadResultHandler callback) {
        return load(identifier)
                .thenAcceptAsync(consumeCallback(callback));
    }

    private Consumer<DataObject> consumeCallback(final AudioLoadResultHandler callback) {
        return loadResult -> {
            if (loadResult == null) {
                callback.noMatches();
                return;
            }
            try {
                final String loadType = loadResult.getString("loadType");
                switch (loadType) {
                    case "TRACK_LOADED":
                        final DataArray trackDataSingle = loadResult.getArray("tracks");
                        final DataObject trackObject = trackDataSingle.getObject(0);
                        final String singleTrackBase64 = trackObject.getString("track");
                        final AudioTrack singleAudioTrack = LavalinkUtil.toAudioTrack(singleTrackBase64);
                        callback.trackLoaded(singleAudioTrack);
                        break;
                    case "PLAYLIST_LOADED":
                        final DataArray trackData = loadResult.getArray("tracks");
                        final List<AudioTrack> tracks = new ArrayList<>();
                        for (int index = 0; index < trackData.length(); index++) {
                            DataObject track = trackData.getObject(index);
                            final String trackBase64 = track.getString("track");
                            final AudioTrack audioTrack = LavalinkUtil.toAudioTrack(trackBase64);
                            tracks.add(audioTrack);
                        }
                        final DataObject playlistInfo = loadResult.getObject("playlistInfo");
                        final int selectedTrackId = playlistInfo.getInt("selectedTrack");
                        final AudioTrack selectedTrack;
                        if (selectedTrackId < tracks.size() && selectedTrackId >= 0) {
                            selectedTrack = tracks.get(selectedTrackId);
                        } else {
                            if (tracks.size() == 0) {
                                callback.loadFailed(new FriendlyException(
                                        "Playlist is empty",
                                        FriendlyException.Severity.SUSPICIOUS,
                                        new IllegalStateException("Empty playlist")
                                ));
                                return;
                            }
                            selectedTrack = tracks.get(0);
                        }
                        final String playlistName = playlistInfo.getString("name");
                        final BasicAudioPlaylist playlist = new BasicAudioPlaylist(playlistName, tracks, selectedTrack, true);
                        callback.playlistLoaded(playlist);
                        break;
                    case "NO_MATCHES":
                        callback.noMatches();
                        break;
                    case "LOAD_FAILED":
                        final DataObject exception = loadResult.getObject("exception");
                        final String message = exception.getString("message");
                        final FriendlyException.Severity severity = FriendlyException.Severity.valueOf(exception.getString("severity"));
                        final FriendlyException friendlyException = new FriendlyException(message, severity, new Throwable());
                        callback.loadFailed(friendlyException);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid loadType: " + loadType);
                }
            } catch (final Exception ex) {
                callback.loadFailed(new FriendlyException(ex.getMessage(), FriendlyException.Severity.FAULT, ex));
            }
        };
    }

    CompletableFuture<DataObject> load(final String identifier) {
        final CompletableFuture<DataObject> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                final String requestUrl = buildBaseAddress(socket) + URLEncoder.encode(identifier, "UTF-8");
                final DataObject response = apiGet(requestUrl, socket.getPassword());
                future.complete(response);
            } catch (final Throwable ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    private String buildBaseAddress(final LavalinkSocket socket) {
        // wss:// or ws:// -> http://
        return socket.getRemoteUri().toString().replaceFirst("[wW][sS]{1,2}[:][/]{2}", "http://") + "/loadtracks?identifier=";
    }

    private HttpClient buildClient() {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (builderConsumer == null)
            return httpClientBuilder.build();
        builderConsumer.accept(httpClientBuilder);
        return httpClientBuilder.build();
    }

    private DataObject apiGet(final String url, final String auth) throws IOException {
        final HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, auth);
        final HttpClient httpClient = buildClient();
        final HttpResponse httpResponse = httpClient.execute(request);
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode != 200)
            throw new IOException("Invalid API Request Status Code: " + statusCode);
        final HttpEntity entity = httpResponse.getEntity();
        if (entity == null)
            throw new IOException("Invalid API Response: No Content");
        final String response = EntityUtils.toString(entity);
        return DataObject.fromJson(response);
    }
}