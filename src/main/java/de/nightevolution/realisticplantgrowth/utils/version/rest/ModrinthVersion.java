package de.nightevolution.realisticplantgrowth.utils.version.rest;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The ModrinthVersion class represents a version of the RealisticPlantGrowth plugin on Modrinth.
 * It implements {@link Comparable} to allow for version comparison based on version numbers.
 */
public class ModrinthVersion implements Comparable<ModrinthVersion> {


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


    /**
     * Compares this {@link ModrinthVersion} with another version based on version numbers.
     *
     * @param otherVersion The {@link ModrinthVersion} to compare to.
     * @return A negative integer, zero, or a positive integer if this version is
     * less than, equal to, or greater than the specified version.
     */
    @Override
    public int compareTo(@NotNull ModrinthVersion otherVersion) {

        String[] thisParts = this.getFilteredVersion().split("\\.");
        String[] otherParts = otherVersion.getFilteredVersion().split("\\.");

        int length = Math.max(thisParts.length, otherParts.length);

        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int otherPart = i < otherParts.length ?
                    Integer.parseInt(otherParts[i]) : 0;
            if (thisPart < otherPart)
                return -1;
            if (thisPart > otherPart)
                return 1;
        }
        return 0;
    }

    /**
     * Removes certain elements from the version number to facilitate numeric comparison.
     *
     * @return The version number with specified elements removed.
     */
    public String getFilteredVersion() {
        return this.getVersion_number().replaceAll("(ALPHA|BETA|SNAPSHOT|HOTFIX|-)", "");
    }
}
