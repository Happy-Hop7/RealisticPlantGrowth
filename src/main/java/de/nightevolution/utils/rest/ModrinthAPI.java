package de.nightevolution.utils.rest;

import java.util.List;

public class ModrinthAPI {


    private String name;
    private String version_number;
    private List<String> game_versions;
    private String versions_type;
    private List<String> loaders;
    private boolean featured;
    private String id;
    private String project_id;
    private String author_id;
    private String date_published;
    private int downloads;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion_number() {
        return version_number;
    }

    public void setVersion_number(String version_number) {
        this.version_number = version_number;
    }

    public List<String> getGame_versions() {
        return game_versions;
    }

    public void setGame_versions(List<String> game_versions) {
        this.game_versions = game_versions;
    }

    public String getVersions_type() {
        return versions_type;
    }

    public void setVersions_type(String versions_type) {
        this.versions_type = versions_type;
    }

    public List<String> getLoaders() {
        return loaders;
    }

    public void setLoaders(List<String> loaders) {
        this.loaders = loaders;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getDate_published() {
        return date_published;
    }

    public void setDate_published(String date_published) {
        this.date_published = date_published;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }
}
