/*
 * This source file was generated by the Gradle 'init' task
 */
package org.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CustomComponentsTest {
    private final CustomGenerator customGenerator = new CustomGenerator();

    @Test
    void testCustomGenerateIdOrder1() {
        int asciiOfA = 'a';

        assertEquals("b", customGenerator.generateId(1));

        for (int i = 0; i <= 25; i++) {
            char c = (char) (asciiOfA + i);
            String id = customGenerator.generateId(i);
            assertEquals(Character.toString(c), id, "the id generation does not work for one number from 0 to 25");
        }

    }

    @Test
    void testCustomGenerateIdOrder2() {
        int asciiOfA = 'a';

        int i1 = 10;
        int i2 = 4;

        for (int j = 1; j < 26; j++) {
            int order = 26 * j + i1;
            String id = customGenerator.generateId(order);
            char c1 = (char) (asciiOfA + j);

            char c2 = (char) (asciiOfA + i1);
            assertEquals(c1 + Character.toString(c2), id);

            order = 26 * j + i2;
            id = customGenerator.generateId(order);
            char c3 = (char) (asciiOfA + i2);
            assertEquals(c1 + Character.toString(c3), id);
        }
    }

    @Test
    // TODO: make the verify_power_26 private and access it through reflection in the tests...
    void testCustomerGeneratorPowers26() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (int i = 1; i <= 6; i++) {
            long power26 = (long) Math.pow(26, i);

            Method m = customGenerator.getClass().getDeclaredMethod("verify_power_26", long.class);
            m.setAccessible(true);
            assertEquals(i , (int) m.invoke(customGenerator, power26));

            String id = customGenerator.generateId(power26);
            String realId = "b" + "a".repeat(i);
            assertEquals(realId, id, "it does not work with powers of 26...");
        }
    }

    @Test
    // the test takes some time to run: however the current implementation passes
    void testIdNumberConversion() {
        for (long i = 0; i <= (long) Math.pow(26,3); i++) {
            String id = customGenerator.generateId(i);
            long numberId = customGenerator.orderFromId(id);
            assertEquals(i, numberId);
        }
    }

    @Test
    void testRandomStringExclude() {
        // Test with various character exclusion sets and string lengths
        
        // 1. Test with common characters excluded
        for (int testRun = 0; testRun < 50; testRun++) {
            List<Character> excludedChars = List.of('a', 'e', 'i', 'o', 'u', '0', '1', '2', '3');
            
            // Test with different string lengths
            for (int length = 5; length < 30; length += 5) {
                String result = customGenerator.randomStringExclude(excludedChars, length);
                
                // Verify length
                assertEquals(length, result.length(), "Generated string should have specified length");
                
                // Verify no excluded characters present
                for (Character excludedChar : excludedChars) {
                    assertFalse(result.contains(String.valueOf(excludedChar)), 
                            "Generated string should not contain excluded character: " + excludedChar);
                }
            }
        }
        
        // 2. Test with random exclusion sets
        Random random = new Random();
        for (int testRun = 0; testRun < 25; testRun++) {
            // Create random exclusion set of varying size (1-20 characters)
            int exclusionSetSize = random.nextInt(1, 20);
            List<Character> randomExcludedChars = new ArrayList<>();
            
            // Populate with random characters from CHARS
            String allChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%&";
            for (int i = 0; i < exclusionSetSize; i++) {
                char c = allChars.charAt(random.nextInt(allChars.length()));
                if (!randomExcludedChars.contains(c)) {
                    randomExcludedChars.add(c);
                }
            }
            
            // Generate and test string
            int length = random.nextInt(10, 40);
            String result = customGenerator.randomStringExclude(randomExcludedChars, length);
            
            // Verify length
            assertEquals(length, result.length(), "Generated string should have specified length");
            
            // Convert excluded chars to a HashSet for faster lookups
            HashSet<Character> excludedSet = new HashSet<>(randomExcludedChars);
            
            // Check each character in result
            for (char c : result.toCharArray()) {
                assertFalse(excludedSet.contains(c), 
                        "Generated string should not contain excluded character: " + c);
            }
        }
        
        // 3. Test excluding specific character classes
        List<List<Character>> characterClasses = List.of(
            // Exclude all digits
            stringToCharList("0123456789"),
            // Exclude all uppercase
            stringToCharList("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
            // Exclude all lowercase
            stringToCharList("abcdefghijklmnopqrstuvwxyz"),
            // Exclude all special chars
            stringToCharList("!@#$%&")
        );
        
        for (List<Character> excludedClass : characterClasses) {
            for (int length = 10; length <= 30; length += 10) {
                String result = customGenerator.randomStringExclude(excludedClass, length);
                assertEquals(length, result.length(), "Generated string should have specified length");
                
                HashSet<Character> excludedSet = new HashSet<>(excludedClass);
                for (char c : result.toCharArray()) {
                    assertFalse(excludedSet.contains(c), 
                            "Generated string should not contain any character from excluded class");
                }
            }
        }
        
        // 4. Edge case: Exclude almost all characters (leave just a few)
        List<Character> allButFew = stringToCharList("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk");
        String result = customGenerator.randomStringExclude(allButFew, 15);
        assertEquals(15, result.length(), "Generated string should have specified length");
        
        HashSet<Character> excludedSet = new HashSet<>(allButFew);
        for (char c : result.toCharArray()) {
            assertFalse(excludedSet.contains(c), 
                    "Generated string should not contain any character from large excluded set");
        }
        
        // 5. Test exception for excluding all characters
        List<Character> allChars = stringToCharList("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%&");
        assertThrows(IllegalArgumentException.class, () -> {
            customGenerator.randomStringExclude(allChars, 10);
        }, "Should throw exception when all characters are excluded");
    }

    // Helper method to convert string to list of characters
    private List<Character> stringToCharList(String str) {
        List<Character> chars = new ArrayList<>();
        for (char c : str.toCharArray()) {
            chars.add(c);
        }
        return chars;
    }

}
