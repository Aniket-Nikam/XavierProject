package com.example.xavierproject;

import java.util.Map;

public class Report {
    private String reportId;
    private String userId;
    private String title;
    private String category;
    private String description;
    private String imageUrl;
    private String imagePublicId;
    private String thumbnailUrl;
    private long timestamp;
    private String status;
    private Map<String, Object> location;

    // Default constructor required for Firebase
    public Report() {
    }

    public Report(String reportId, String userId, String title, String category,
                  String description, String imageUrl, String imagePublicId,
                  String thumbnailUrl, long timestamp, String status,
                  Map<String, Object> location) {
        this.reportId = reportId;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.imagePublicId = imagePublicId;
        this.thumbnailUrl = thumbnailUrl;
        this.timestamp = timestamp;
        this.status = status;
        this.location = location;
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePublicId() {
        return imagePublicId;
    }

    public void setImagePublicId(String imagePublicId) {
        this.imagePublicId = imagePublicId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    /**
     * Get latitude from location map
     */
    public double getLatitude() {
        if (location != null && location.containsKey("latitude")) {
            Object lat = location.get("latitude");
            if (lat instanceof Double) {
                return (Double) lat;
            } else if (lat instanceof Long) {
                return ((Long) lat).doubleValue();
            }
        }
        return 0.0;
    }

    /**
     * Get longitude from location map
     */
    public double getLongitude() {
        if (location != null && location.containsKey("longitude")) {
            Object lng = location.get("longitude");
            if (lng instanceof Double) {
                return (Double) lng;
            } else if (lng instanceof Long) {
                return ((Long) lng).doubleValue();
            }
        }
        return 0.0;
    }
}