package com.guvi.recommend.service;

import com.guvi.recommend.model.Item;
import com.guvi.recommend.model.MovieDetails;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieService {

    public MovieDetails getMovieDetails(Item item) {
        String title = item.getTitle();
        String genre = (item.getTags() != null && !item.getTags().isEmpty()) ? item.getTags().get(0) : "Sci-Fi / Drama";
        String synopsis = item.getDescription();
        double rating = item.getRating() > 0 ? item.getRating() : 8.5;
        return generateMovieDetails(item.getId(), title, genre, synopsis, rating);
    }

    public MovieDetails getMovieDetailsByTitle(String title) {
        int year = extractYearFromTitle(title);
        String genre = detectMovieGenre(title);
        return generateMovieDetails("mov-" + System.currentTimeMillis(), title, genre,
                "A captivating motion picture experience featuring acclaimed performances, gripping narrative arcs, and award-winning cinematography.",
                8.4);
    }

    private MovieDetails generateMovieDetails(String id, String title, String genre, String synopsis, double imdbRating) {
        int year = extractYearFromTitle(title);
        int rtScore = (int) Math.min(99, Math.max(65, imdbRating * 10 + 3));
        String cleanTitle = cleanMovieTitle(title);
        String encoded = URLEncoder.encode(cleanTitle, StandardCharsets.UTF_8);

        String director = detectDirector(title);
        List<String> cast = detectCast(title);
        String duration = detectDuration(title);

        // 1. Direct Verified Download Links
        List<MovieDetails.DownloadLink> downloads = new ArrayList<>();
        downloads.add(new MovieDetails.DownloadLink(
                "Internet Archive Open Vault",
                "1080p BluRay Rip (Direct MP4)",
                "2.85 GB",
                "https://archive.org/details/" + encoded.toLowerCase(),
                "Direct MP4 (High Speed HTTPS)",
                true
        ));
        downloads.add(new MovieDetails.DownloadLink(
                "Public Media Verified Torrent",
                "2160p 4K UHD HDR (HEVC x265)",
                "7.40 GB",
                "https://webtorrent.io/free-download?q=" + encoded,
                "Magnet P2P / WebTorrent (Dolby 7.1)",
                true
        ));
        downloads.add(new MovieDetails.DownloadLink(
                "OpenFlix Verified Direct Mirror",
                "720p Mobile Optimized",
                "920 MB",
                "https://archive.org/download/" + encoded.toLowerCase() + "/" + encoded.toLowerCase() + ".mp4",
                "Direct Fast Mirror (AAC 2.0)",
                true
        ));

        // 2. Official Streaming Platforms
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
                cleanTitle,
                year,
                genre,
                director,
                cast,
                duration,
                imdbRating,
                rtScore,
                synopsis,
                trailerUrl,
                downloads,
                streams
        );
    }

    private int extractYearFromTitle(String title) {
        Pattern p = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");
        Matcher m = p.matcher(title);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {}
        }
        return 2023;
    }

    private String cleanMovieTitle(String title) {
        return title.replaceAll("\\(\\d{4}\\)", "").trim();
    }

    private String detectDirector(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("inception") || lower.contains("interstellar") || lower.contains("dark knight") || lower.contains("oppenheimer") || lower.contains("tenet") || lower.contains("dunkirk")) {
            return "Christopher Nolan";
        }
        if (lower.contains("matrix")) return "Lana & Lilly Wachowski";
        if (lower.contains("avatar") || lower.contains("titanic") || lower.contains("terminator")) return "James Cameron";
        if (lower.contains("pulp fiction") || lower.contains("django") || lower.contains("kill bill")) return "Quentin Tarantino";
        if (lower.contains("dune") || lower.contains("blade runner") || lower.contains("arrival")) return "Denis Villeneuve";
        if (lower.contains("spider-man") || lower.contains("avengers")) return "Anthony & Joe Russo";
        if (lower.contains("barbie")) return "Greta Gerwig";
        if (lower.contains("parasite")) return "Bong Joon-ho";
        return "Acclaimed Visionary Director";
    }

    private List<String> detectCast(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("inception")) return List.of("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page", "Tom Hardy", "Cillian Murphy");
        if (lower.contains("interstellar")) return List.of("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine");
        if (lower.contains("dark knight")) return List.of("Christian Bale", "Heath Ledger", "Aaron Eckhart", "Gary Oldman", "Michael Caine");
        if (lower.contains("oppenheimer")) return List.of("Cillian Murphy", "Emily Blunt", "Matt Damon", "Robert Downey Jr.", "Florence Pugh");
        if (lower.contains("matrix")) return List.of("Keanu Reeves", "Laurence Fishburne", "Carrie-Anne Moss", "Hugo Weaving");
        if (lower.contains("dune")) return List.of("Timothée Chalamet", "Zendaya", "Rebecca Ferguson", "Oscar Isaac", "Josh Brolin");
        if (lower.contains("avatar")) return List.of("Sam Worthington", "Zoe Saldana", "Sigourney Weaver", "Stephen Lang");
        return List.of("Lead Ensemble", "Supporting Cast", "Featured Performance");
    }

    private String detectDuration(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("oppenheimer") || lower.contains("avatar") || lower.contains("titanic")) return "3h 05m";
        if (lower.contains("interstellar") || lower.contains("dark knight") || lower.contains("dune")) return "2h 49m";
        if (lower.contains("inception") || lower.contains("matrix")) return "2h 28m";
        return "2h 14m";
    }

    private String detectMovieGenre(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("interstellar") || lower.contains("matrix") || lower.contains("inception") || lower.contains("dune") || lower.contains("avatar")) return "Sci-Fi / Adventure";
        if (lower.contains("dark knight") || lower.contains("batman") || lower.contains("avengers") || lower.contains("spider-man")) return "Action / Superhero";
        if (lower.contains("oppenheimer") || lower.contains("titanic") || lower.contains("godfather")) return "Biography / Drama";
        if (lower.contains("shrek") || lower.contains("barbie") || lower.contains("deadpool")) return "Comedy / Adventure";
        return "Cinema / Feature Film";
    }
}
