package com.guvi.recommend.model;

import java.util.*;

public class MovieDetails {
    private String movieId;
    private String title;
    private int year;
    private String genre;
    private String director;
    private List<String> cast;
    private String duration;
    private double imdbRating;
    private int rottenTomatoesScore;
    private String synopsis;
    private String trailerUrl;
    private List<DownloadLink> downloadLinks;
    private List<StreamingPlatform> streamingPlatforms;

    public static class DownloadLink {
        private String sourceName;
        private String quality; // e.g. "4K UHD", "1080p BluRay", "720p HD"
        private String size; // e.g. "2.4 GB"
        private String directUrl;
        private String format; // "MP4", "MKV", "Direct Stream"
        private boolean isOfficialFreeArchive;

        public DownloadLink(String sourceName, String quality, String size, String directUrl, String format, boolean isOfficialFreeArchive) {
            this.sourceName = sourceName;
            this.quality = quality;
            this.size = size;
            this.directUrl = directUrl;
            this.format = format;
            this.isOfficialFreeArchive = isOfficialFreeArchive;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sourceName", sourceName);
            map.put("quality", quality);
            map.put("size", size);
            map.put("directUrl", directUrl);
            map.put("format", format);
            map.put("isOfficialFreeArchive", isOfficialFreeArchive);
            return map;
        }
    }

    public static class StreamingPlatform {
        private String platformName;
        private String streamType; // "Included in Subscription", "Rent/Buy", "Free with Ads"
        private String directWatchUrl;
        private String price;

        public StreamingPlatform(String platformName, String streamType, String directWatchUrl, String price) {
            this.platformName = platformName;
            this.streamType = streamType;
            this.directWatchUrl = directWatchUrl;
            this.price = price;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("platformName", platformName);
            map.put("streamType", streamType);
            map.put("directWatchUrl", directWatchUrl);
            map.put("price", price);
            return map;
        }
    }

    public MovieDetails(String movieId, String title, int year, String genre, String director, List<String> cast, String duration, double imdbRating, int rottenTomatoesScore, String synopsis, String trailerUrl, List<DownloadLink> downloadLinks, List<StreamingPlatform> streamingPlatforms) {
        this.movieId = movieId;
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.director = director;
        this.cast = cast != null ? cast : new ArrayList<>();
        this.duration = duration;
        this.imdbRating = imdbRating;
        this.rottenTomatoesScore = rottenTomatoesScore;
        this.synopsis = synopsis;
        this.trailerUrl = trailerUrl;
        this.downloadLinks = downloadLinks != null ? downloadLinks : new ArrayList<>();
        this.streamingPlatforms = streamingPlatforms != null ? streamingPlatforms : new ArrayList<>();
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getGenre() { return genre; }
    public String getDirector() { return director; }
    public List<String> getCast() { return cast; }
    public String getDuration() { return duration; }
    public double getImdbRating() { return imdbRating; }
    public int getRottenTomatoesScore() { return rottenTomatoesScore; }
    public String getSynopsis() { return synopsis; }
    public String getTrailerUrl() { return trailerUrl; }
    public List<DownloadLink> getDownloadLinks() { return downloadLinks; }
    public List<StreamingPlatform> getStreamingPlatforms() { return streamingPlatforms; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("movieId", movieId);
        map.put("title", title);
        map.put("year", year);
        map.put("genre", genre);
        map.put("director", director);
        map.put("cast", cast);
        map.put("duration", duration);
        map.put("imdbRating", imdbRating);
        map.put("rottenTomatoesScore", rottenTomatoesScore);
        map.put("synopsis", synopsis);
        map.put("trailerUrl", trailerUrl);

        List<Map<String, Object>> downloads = new ArrayList<>();
        for (DownloadLink d : downloadLinks) downloads.add(d.toMap());
        map.put("downloadLinks", downloads);

        List<Map<String, Object>> streams = new ArrayList<>();
        for (StreamingPlatform s : streamingPlatforms) streams.add(s.toMap());
        map.put("streamingPlatforms", streams);

        return map;
    }
}
