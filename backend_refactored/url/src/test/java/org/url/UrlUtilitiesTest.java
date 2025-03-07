/*
 * This source file was generated by the Gradle 'init' task
 */
package org.url;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UrlUtilitiesTest {

    private final UrlValidator urlValidator = new UrlValidator();
    private final UrlDecoder decoder = new UrlDecoder();

    @Test
    void testValidUrls() {
        String s1 = "https://youtube.com";
        String s2 = "https://github.com/ayhem18/Towards_Data_Science/blob/main/Programming_Tools/Databases/Practice/sqlpad";

        assertDoesNotThrow(() -> this.urlValidator.validateUrl(s1));
        assertDoesNotThrow(() -> this.urlValidator.validateUrl(s2));
    }

    @Test void testInvalidUrls() {
        String s1 = "https://github.com/ayhem 18"; // invalid because of the space
        String s2 = "https://github.c/ayhem18"; // invalid because of the 1 character top-domain

        assertThrows(UrlValidator.InvalidUrlException.class,() -> this.urlValidator.validateUrl(s1));
        assertThrows(UrlValidator.InvalidUrlException.class,() -> this.urlValidator.validateUrl(s2));
    }

    @Test
    void testUrlDecoderOneLevel() {
        List<String> oneLevelUrls = List.of("https://youtube.com",
                "https://www.github.edu",
                "http://another111site.eu",
                "http://well_yeye_here_we666.org");

        List<String> levelNames = List.of("youtube.com",
                "www.github.edu",
                "another111site.eu",
                "well_yeye_here_we666.org");

        for (int i = 0; i < 4; i++) {
            String url = oneLevelUrls.get(i);
            String level = levelNames.get(i);
            List<UrlLevelEntity> levels = decoder.breakdown(url);
            // make sure it is only one level
            assertEquals(levels.size(), 1);
            // make sure the level is extracted correctly
            UrlLevelEntity topLevel = levels.getFirst();
            assertEquals(new UrlLevelEntity(level, null, null, null), topLevel);
        }
    }

    @Test
    void testLongUrls() {
        // more tests are definitely needed !!!

        String u1 = "https://github.com/ayhem18/Towards_Data_Science/blob/main/Programming_Tools/Databases/Practice/sqlpad";
        List<UrlLevelEntity> e1 = List.of(
                new UrlLevelEntity("github.com", null, null, null),
                new UrlLevelEntity(null, "ayhem18", null, null),
                new UrlLevelEntity(null, "Towards_Data_Science", null, null),
                new UrlLevelEntity("blob", null, null, null),
                new UrlLevelEntity("main", null, null, null),
                new UrlLevelEntity(null, "Programming_Tools", null, null),
                new UrlLevelEntity("Databases", null, null, null),
                new UrlLevelEntity("Practice", null, null, null),
                new UrlLevelEntity("sqlpad", null, null, null)
        );

        List<UrlLevelEntity> l1 = decoder.breakdown(u1);
        assertEquals(l1, e1);

        String u2 = "https://github.com/ayhem18?tab=overview&from=2025-01-01&to=2025-01-22";

        List<UrlLevelEntity> e2 = List.of(
                new UrlLevelEntity("github.com", null, null, null),
                new UrlLevelEntity(null, "ayhem18",
                        List.of("tab", "from", "to"),
                        List.of("overview", "2025-01-01", "2025-01-22")
                        )
        );

        List<UrlLevelEntity> l2 = decoder.breakdown(u2);
        assertEquals(l2, e2);
    }
}
