package com.guvi.recommend.service;

import com.guvi.recommend.model.Item;
import com.guvi.recommend.model.MovieDetails;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MovieService {

    public MovieDetails getMovieDetails(Item item) {
        String title = item.getTitle();
        String genre = (item.getTags() != null && !item.getTags().isEmpty()) ? item.getTags().get(0) : "Sci-Fi / Drama";
        String synopsis = item.getDescription();
        double rating = item.getRating() > 0 ? item.getRating() : 8.5;
        return generateMovieDetails(item.getId(), title, genre, synopsis, rating);
    }

    public MovieDetails getMovieDetailsByTitle(String title) {
        return generateMovieDetails("mov-" + System.currentTimeMillis(), title, "Action / Adventure", "An acclaimed cinematic masterpiece featuring breathtaking visuals and compelling storytelling.", 8.4);
    }

    private MovieDetails generateMovieDetails(String id, String title, String genre, String synopsis, double imdbRating) {
        int rtScore = (int) Math.min(99, Math.max(60, imdbRating * 10 + 4));
        String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);

        List<String> cast = List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page", "Tom Hardy", "Cillian Murphy");
        if (title.toLowerCase().contains("interstellar")) {
            cast = List.of("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine");
        } else if (title.toLowerCase().contains("matrix")) {
            cast = List.of("Keanu Reeves", "Laurence Fishburne", "Carrie-Anne Moss", "Hugo Weaving");
        } else if (title.toLowerCase().contains("dark knight")) {
            cast = List.of("Christian Bale", "Heath Ledger", "Aaron Eckhart", "Michael Caine", "Gary Oldman");
        }

        // Direct Download Links
        List<MovieDetails.DownloadLink> downloads = new ArrayList<>();
        downloads.add(new MovieDetails.DownloadLink(
                "Internet Archive Open Vault",
                "1080p BluRay Rip (Direct MP4)",
                "2.85 GB",
                "https://archive.org/details/" + encoded.toLowerCase(),
                "Direct MP4 High-Speed",
                true
        ));
        downloads.add(new MovieDetails.DownloadLink(
                "Public Media Torrent / Magnet Link",
                "2160p 4K UHD HDR (HEVC x265)",
                "7.12 GB",
                "https://webtorrent.io/free-download?q=" + encoded,
                "Magnet P2P / Direct Stream",
                true
        ));
        downloads.add(new MovieDetails.DownloadLink(
                "OpenFlix Verified Direct Mirror",
                "720p Mobile Optimized",
                "950 MB",
                "https://archive.org/download/" + encoded.toLowerCase() + "/" + encoded.toLowerCase() + ".mp4",
                "Direct Fast Mirror",
                true
        ));

        // Streaming Platforms
        List<MovieDetails.StreamingPlatform> streams = new ArrayList<>();
        streams.add(new MovieDetails.StreamingPlatform(
                "Netflix",
                "Included with Subscription",
                "https://www.netflix.com/search?q=" + encoded,
                "Subscription ($9.99/mo)"
        ));
        streams.add(new MovieDetails.StreamingPlatform(
                "Amazon Prime Video",
                "Rent in 4K UHD or Included with Prime",
                "https://www.amazon.com/s?k=" + encoded + "+prime+video",
                "$3.99 to Rent"
        ));
        streams.add(new MovieDetails.StreamingPlatform(
                "Apple TV+",
                "Purchase or Rent 4K HDR Dolby Atmos",
                "https://tv.apple.com/search?term=" + encoded,
                "$4.99 to Rent"
        ));
        streams.add(new MovieDetails.StreamingPlatform(
                "Disney+ / Hulu",
                "Stream in Full HD / 4K",
                "https://www.disneyplus.com/search?q=" + encoded,
                "Subscription"
        ));

        String trailerUrl = "https://www.youtube.com/results?search_query=" + encoded + "+official+trailer+4k";

        return new MovieDetails(
                id,
                title,
                2021,
                genre,
                "Christopher Nolan",
                cast,
                "2h 28m",
                imdbRating,
                rtScore,
                synopsis,
                trailerUrl,
                downloads,
                streams
        );
    }
}
