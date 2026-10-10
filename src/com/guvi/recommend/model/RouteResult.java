package com.guvi.recommend.model;

import java.util.*;

public class RouteResult {
    private String originName;
    private double originLat;
    private double originLon;
    private String destinationName;
    private double destinationLat;
    private double destinationLon;
    private double distanceKm;
    private double durationMinutes;
    private String formattedDuration;
    private String routeSummary;
    private List<RouteStep> steps;
    private List<List<Double>> pathCoordinates; // [[lat, lon], ...]
    private String navigationUrl;

    public static class RouteStep {
        private String instruction;
        private double distanceMeters;
        private double durationSeconds;
        private String maneuver;

        public RouteStep(String instruction, double distanceMeters, double durationSeconds, String maneuver) {
            this.instruction = instruction;
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.maneuver = maneuver;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("instruction", instruction);
            map.put("distanceMeters", distanceMeters);
            map.put("durationSeconds", durationSeconds);
            map.put("maneuver", maneuver);
            return map;
        }
    }

    public RouteResult() {
        this.steps = new ArrayList<>();
        this.pathCoordinates = new ArrayList<>();
    }

    public RouteResult(String originName, double originLat, double originLon, String destinationName, double destinationLat, double destinationLon, double distanceKm, double durationMinutes, String routeSummary, List<RouteStep> steps, List<List<Double>> pathCoordinates) {
        this.originName = originName;
        this.originLat = originLat;
        this.originLon = originLon;
        this.destinationName = destinationName;
        this.destinationLat = destinationLat;
        this.destinationLon = destinationLon;
        this.distanceKm = Math.round(distanceKm * 100.0) / 100.0;
        this.durationMinutes = Math.round(durationMinutes * 10.0) / 10.0;
        int hours = (int) (durationMinutes / 60);
        int mins = (int) (durationMinutes % 60);
        this.formattedDuration = (hours > 0 ? hours + " hr " : "") + mins + " min";
        this.routeSummary = routeSummary;
        this.steps = steps != null ? steps : new ArrayList<>();
        this.pathCoordinates = pathCoordinates != null ? pathCoordinates : new ArrayList<>();
        this.navigationUrl = String.format(Locale.US, "https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f", originLat, originLon, destinationLat, destinationLon);
    }

    public String getOriginName() { return originName; }
    public double getOriginLat() { return originLat; }
    public double getOriginLon() { return originLon; }
    public String getDestinationName() { return destinationName; }
    public double getDestinationLat() { return destinationLat; }
    public double getDestinationLon() { return destinationLon; }
    public double getDistanceKm() { return distanceKm; }
    public double getDurationMinutes() { return durationMinutes; }
    public String getFormattedDuration() { return formattedDuration; }
    public String getRouteSummary() { return routeSummary; }
    public List<RouteStep> getSteps() { return steps; }
    public List<List<Double>> getPathCoordinates() { return pathCoordinates; }
    public String getNavigationUrl() { return navigationUrl; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("originName", originName);
        map.put("originLat", originLat);
        map.put("originLon", originLon);
        map.put("destinationName", destinationName);
        map.put("destinationLat", destinationLat);
        map.put("destinationLon", destinationLon);
        map.put("distanceKm", distanceKm);
        map.put("durationMinutes", durationMinutes);
        map.put("formattedDuration", formattedDuration);
        map.put("routeSummary", routeSummary);
        List<Map<String, Object>> stepMaps = new ArrayList<>();
        for (RouteStep s : steps) stepMaps.add(s.toMap());
        map.put("steps", stepMaps);
        map.put("pathCoordinates", pathCoordinates);
        map.put("navigationUrl", navigationUrl);
        return map;
    }
}
