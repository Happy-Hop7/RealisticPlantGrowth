package utils.version.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.nightevolution.realisticplantgrowth.utils.version.rest.ModrinthVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link ModrinthVersion}.
 * Tests the functionality of the ModrinthVersion class including its
 * comparison logic and filtering methods.
 */
public class ModrinthVersionTest {

    private List<ModrinthVersion> versions;
    private Gson gson;

    @BeforeEach
    public void setUp() {
        // Initialize Gson
        gson = new Gson();

        // Sample JSON response
        String jsonResponse = "[" +
                "{\"game_versions\":[\"1.21.4\",\"1.21.5\"],\"loaders\":[\"paper\",\"purpur\",\"spigot\"],\"id\":\"9qO2Vft0\"," +
                "\"project_id\":\"TcGxLk2t\",\"author_id\":\"3wPZj8Ix\",\"featured\":false,\"name\":\"Realistic Plant Growth BETA-0.9.4\"," +
                "\"version_number\":\"BETA-0.9.4\",\"date_published\":\"2025-05-04T09:56:12.819583Z\",\"downloads\":25,\"version_type\":\"beta\"}," +
                "{\"game_versions\":[\"1.20.1\",\"1.20.2\",\"1.20.3\",\"1.20.4\",\"1.20.5\",\"1.20.6\",\"1.21\",\"1.21.1\",\"1.21.2\",\"1.21.3\",\"1.21.4\"]," +
                "\"loaders\":[\"paper\",\"purpur\",\"spigot\"],\"id\":\"38tkoocS\",\"project_id\":\"TcGxLk2t\",\"author_id\":\"3wPZj8Ix\"," +
                "\"featured\":false,\"name\":\"Realistic Plant Growth BETA-0.9.3\",\"version_number\":\"BETA-0.9.3\"," +
                "\"date_published\":\"2025-02-08T13:09:04.905604Z\",\"downloads\":323,\"version_type\":\"beta\"}" +
                "]";

        // Parse JSON to list of ModrinthVersion objects
        ModrinthVersion[] versionArray = gson.fromJson(jsonResponse, ModrinthVersion[].class);
        versions = Arrays.asList(versionArray);
    }

    @Test
    public void testDeserializationWorks() {
        // Test that deserialization works correctly
        assertNotNull(versions);
        assertEquals(2, versions.size());

        ModrinthVersion version = versions.getFirst();
        assertEquals("Realistic Plant Growth BETA-0.9.4", version.getName());
        assertEquals("BETA-0.9.4", version.getVersion_number());
        assertEquals(2, version.getGame_versions().size());
        assertTrue(version.getGame_versions().contains("1.21.4"));
        assertEquals(3, version.getLoaders().size());
        assertEquals(25, version.getDownloads());
        assertEquals("TcGxLk2t", version.getProject_id());
    }

    @Test
    public void testGetFilteredVersion() {
        ModrinthVersion version = new ModrinthVersion();

        // Test with BETA
        version.setVersion_number("BETA-0.9.4");
        assertEquals("0.9.4", version.getFilteredVersion());

        // Test with ALPHA
        version.setVersion_number("ALPHA-1.2.3");
        assertEquals("1.2.3", version.getFilteredVersion());

        // Test with SNAPSHOT
        version.setVersion_number("SNAPSHOT-2.0.0");
        assertEquals("2.0.0", version.getFilteredVersion());

        // Test with HOTFIX
        version.setVersion_number("BETA-1.1.5-HOTFIX");
        assertEquals("1.1.5", version.getFilteredVersion());

        // Test with multiple prefixes
        version.setVersion_number("BETA-0.8.2-SNAPSHOT");
        assertEquals("0.8.2", version.getFilteredVersion());
    }

    @Test
    public void testCompareToWithSameVersions() {
        ModrinthVersion v1 = new ModrinthVersion();
        ModrinthVersion v2 = new ModrinthVersion();

        v1.setVersion_number("BETA-1.0.0");
        v2.setVersion_number("BETA-1.0.0");

        assertEquals(0, v1.compareTo(v2));
    }

    @Test
    public void testCompareToWithDifferentVersions() {
        ModrinthVersion v1 = new ModrinthVersion();
        ModrinthVersion v2 = new ModrinthVersion();

        v1.setVersion_number("BETA-0.9.3");
        v2.setVersion_number("BETA-0.9.4");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testCompareToWithDifferentPrefixes() {
        ModrinthVersion v1 = new ModrinthVersion();
        ModrinthVersion v2 = new ModrinthVersion();

        v1.setVersion_number("ALPHA-1.0.0");
        v2.setVersion_number("BETA-1.0.0");

        // Should be equal since prefixes are filtered out
        assertEquals(0, v1.compareTo(v2));
    }

    @Test
    public void testCompareToWithDifferentLengths() {
        ModrinthVersion v1 = new ModrinthVersion();
        ModrinthVersion v2 = new ModrinthVersion();

        v1.setVersion_number("BETA-1.0");
        v2.setVersion_number("BETA-1.0.1");

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
    }

    @Test
    public void testSortVersions() {
        // Create multiple versions with different version numbers
        List<ModrinthVersion> versionList = new ArrayList<>();

        ModrinthVersion v1 = new ModrinthVersion();
        v1.setVersion_number("BETA-0.9.4");
        v1.setName("Version 0.9.4");
        versionList.add(v1);

        ModrinthVersion v2 = new ModrinthVersion();
        v2.setVersion_number("BETA-0.8.2");
        v2.setName("Version 0.8.2");
        versionList.add(v2);

        ModrinthVersion v3 = new ModrinthVersion();
        v3.setVersion_number("BETA-0.9.3");
        v3.setName("Version 0.9.3");
        versionList.add(v3);

        ModrinthVersion v4 = new ModrinthVersion();
        v4.setVersion_number("BETA-1.0.0");
        v4.setName("Version 1.0.0");
        versionList.add(v4);

        // Sort the list
        Collections.sort(versionList);

        // Check the order
        assertEquals("Version 0.8.2", versionList.get(0).getName());
        assertEquals("Version 0.9.3", versionList.get(1).getName());
        assertEquals("Version 0.9.4", versionList.get(2).getName());
        assertEquals("Version 1.0.0", versionList.get(3).getName());
    }

    @Test
    public void testVersionParsingFromFullJSON() {
        // This test would use real JSON from the paste.txt if needed
        assertNotNull(versions);
        assertFalse(versions.isEmpty());

        // Sort versions
        Collections.sort(versions);

        // Extract version numbers for verification
        List<String> sortedVersionNumbers = new ArrayList<>();
        for (ModrinthVersion version : versions) {
            sortedVersionNumbers.add(version.getFilteredVersion());
        }

        // Verify they're in ascending order
        for (int i = 0; i < sortedVersionNumbers.size() - 1; i++) {
            String current = sortedVersionNumbers.get(i);
            String next = sortedVersionNumbers.get(i + 1);

            String[] currentParts = current.split("\\.");
            String[] nextParts = next.split("\\.");

            boolean isLessThanOrEqual = true;
            for (int j = 0; j < Math.min(currentParts.length, nextParts.length); j++) {
                int currentPart = Integer.parseInt(currentParts[j]);
                int nextPart = Integer.parseInt(nextParts[j]);

                if (currentPart > nextPart) {
                    isLessThanOrEqual = false;
                    break;
                } else if (currentPart < nextPart) {
                    break;
                }
            }

            assertTrue(isLessThanOrEqual, "Versions are not sorted correctly: " + current + " should be before " + next);
        }
    }
}