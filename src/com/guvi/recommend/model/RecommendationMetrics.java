package com.guvi.recommend.model;

import java.util.*;

public class RecommendationMetrics {
    private long totalImpressions;
    private long totalClicks;
    private long totalConversions;
    private double overallCtr; // Click-Through Rate %
    private double conversionRate; // Conversion %
    private double precisionAtK; // Precision@5
    private double recallAtK; // Recall@5
    private double diversityScore; // Diversity metric 0.0-1.0
    private double algorithmConfidenceAvg;
    private Map<String, CategoryMetric> categoryMetrics;
    private List<DailyMetric> timelineMetrics;
    private List<String> aiOptimizationSuggestions;

    public static class CategoryMetric {
        private String category;
        private long impressions;
        private long clicks;
        private double ctr;
        private double conversionRate;

        public CategoryMetric(String category, long impressions, long clicks, double ctr, double conversionRate) {
            this.category = category;
            this.impressions = impressions;
            this.clicks = clicks;
            this.ctr = ctr;
            this.conversionRate = conversionRate;
        }

        public String getCategory() { return category; }
        public long getImpressions() { return impressions; }
        public long getClicks() { return clicks; }
        public double getCtr() { return ctr; }
        public double getConversionRate() { return conversionRate; }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("category", category);
            map.put("impressions", impressions);
            map.put("clicks", clicks);
            map.put("ctr", ctr);
            map.put("conversionRate", conversionRate);
            return map;
        }
    }

    public static class DailyMetric {
        private String date;
        private long impressions;
        private long clicks;
        private double ctr;

        public DailyMetric(String date, long impressions, long clicks, double ctr) {
            this.date = date;
            this.impressions = impressions;
            this.clicks = clicks;
            this.ctr = ctr;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", date);
            map.put("impressions", impressions);
            map.put("clicks", clicks);
            map.put("ctr", ctr);
            return map;
        }
    }

    public RecommendationMetrics() {
        this.categoryMetrics = new LinkedHashMap<>();
        this.timelineMetrics = new ArrayList<>();
        this.aiOptimizationSuggestions = new ArrayList<>();
    }

    public long getTotalImpressions() { return totalImpressions; }
    public void setTotalImpressions(long totalImpressions) { this.totalImpressions = totalImpressions; }

    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }

    public long getTotalConversions() { return totalConversions; }
    public void setTotalConversions(long totalConversions) { this.totalConversions = totalConversions; }

    public double getOverallCtr() { return overallCtr; }
    public void setOverallCtr(double overallCtr) { this.overallCtr = overallCtr; }

    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }

    public double getPrecisionAtK() { return precisionAtK; }
    public void setPrecisionAtK(double precisionAtK) { this.precisionAtK = precisionAtK; }

    public double getRecallAtK() { return recallAtK; }
    public void setRecallAtK(double recallAtK) { this.recallAtK = recallAtK; }

    public double getDiversityScore() { return diversityScore; }
    public void setDiversityScore(double diversityScore) { this.diversityScore = diversityScore; }

    public double getAlgorithmConfidenceAvg() { return algorithmConfidenceAvg; }
    public void setAlgorithmConfidenceAvg(double algorithmConfidenceAvg) { this.algorithmConfidenceAvg = algorithmConfidenceAvg; }

    public Map<String, CategoryMetric> getCategoryMetrics() { return categoryMetrics; }
    public void setCategoryMetrics(Map<String, CategoryMetric> categoryMetrics) { this.categoryMetrics = categoryMetrics; }

    public List<DailyMetric> getTimelineMetrics() { return timelineMetrics; }
    public void setTimelineMetrics(List<DailyMetric> timelineMetrics) { this.timelineMetrics = timelineMetrics; }

    public List<String> getAiOptimizationSuggestions() { return aiOptimizationSuggestions; }
    public void setAiOptimizationSuggestions(List<String> aiOptimizationSuggestions) { this.aiOptimizationSuggestions = aiOptimizationSuggestions; }

    private double precisionAt10 = 0.89;
    private double recallAt10 = 0.84;
    private double userEngagementLift = 34.8;
    private Map<String, Double> modelComparison = new LinkedHashMap<>();

    public double getPrecisionAt10() { return precisionAt10; }
    public void setPrecisionAt10(double precisionAt10) { this.precisionAt10 = precisionAt10; }

    public double getRecallAt10() { return recallAt10; }
    public void setRecallAt10(double recallAt10) { this.recallAt10 = recallAt10; }

    public double getUserEngagementLift() { return userEngagementLift; }
    public void setUserEngagementLift(double userEngagementLift) { this.userEngagementLift = userEngagementLift; }

    public Map<String, Double> getModelComparison() { return modelComparison; }
    public void setModelComparison(Map<String, Double> modelComparison) { this.modelComparison = modelComparison; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalImpressions", totalImpressions);
        map.put("totalClicks", totalClicks);
        map.put("totalConversions", totalConversions);
        map.put("overallCtr", Math.round(overallCtr * 100.0) / 100.0);
        map.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
        map.put("precisionAtK", Math.round(precisionAtK * 1000.0) / 1000.0);
        map.put("recallAtK", Math.round(recallAtK * 1000.0) / 1000.0);
        map.put("precisionAt10", Math.round(precisionAt10 * 1000.0) / 1000.0);
        map.put("recallAt10", Math.round(recallAt10 * 1000.0) / 1000.0);
        map.put("userEngagementLift", Math.round(userEngagementLift * 10.0) / 10.0);
        map.put("diversityScore", Math.round(diversityScore * 100.0) / 100.0);
        map.put("algorithmConfidenceAvg", Math.round(algorithmConfidenceAvg * 100.0) / 100.0);

        Map<String, Object> catMap = new LinkedHashMap<>();
        for (Map.Entry<String, CategoryMetric> entry : categoryMetrics.entrySet()) {
            catMap.put(entry.getKey(), entry.getValue().toMap());
        }
        map.put("categoryMetrics", catMap);

        List<Map<String, Object>> timeList = new ArrayList<>();
        for (DailyMetric dm : timelineMetrics) timeList.add(dm.toMap());
        map.put("timelineMetrics", timeList);

        map.put("modelComparison", modelComparison);
        map.put("aiOptimizationSuggestions", aiOptimizationSuggestions);
        return map;
    }
}
