package org.apache.commons.jcs.engine.match;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/** Unit tests for the key matcher. */
public class KeyMatcherPatternImpllUnitTest
    extends TestCase
{
    /**
     * Verify that the matching method works.
     */
    public void testGetMatchingKeysFromArray_AllMatch()
    {
        // SETUP
        int numToInsertPrefix1 = 10;
        Set<String> keyArray = new HashSet<String>();

        String keyprefix1 = "MyPrefixC";

        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            keyArray.add(keyprefix1 + String.valueOf( i ));
        }

        KeyMatcherPatternImpl<String> keyMatcher = new KeyMatcherPatternImpl<String>();

        // DO WORK
        Set<String> result1 = keyMatcher.getMatchingKeysFromArray( keyprefix1 + ".", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
    }

    /**
     * Verify that the matching method works.
     */
    public void testGetMatchingKeysFromArray_AllMatchFirstNull()
    {
        // SETUP
        int numToInsertPrefix1 = 10;
        Set<String> keyArray = new HashSet<String>();

        String keyprefix1 = "MyPrefixC";

        // insert with prefix1
        for ( int i = 1; i < numToInsertPrefix1 + 1; i++ )
        {
            keyArray.add(keyprefix1 + String.valueOf( i ));
        }

        KeyMatcherPatternImpl<String> keyMatcher = new KeyMatcherPatternImpl<String>();

        // DO WORK
        Set<String> result1 = keyMatcher.getMatchingKeysFromArray( keyprefix1 + "\\S+", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
    }

    /**
     * Verify that the matching method works.
     */
    public void testGetMatchingKeysFromArray_TwoTypes()
    {
        // SETUP
        int numToInsertPrefix1 = 10;
        int numToInsertPrefix2 = 50;
        Set<String> keyArray = new HashSet<String>();

        String keyprefix1 = "MyPrefixA";
        String keyprefix2 = "MyPrefixB";

        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            keyArray.add(keyprefix1 + String.valueOf( i ));
        }

        // insert with prefix2
        for ( int i = numToInsertPrefix1; i < numToInsertPrefix2 + numToInsertPrefix1; i++ )
        {
            keyArray.add(keyprefix2 + String.valueOf( i ));
        }

        KeyMatcherPatternImpl<String> keyMatcher = new KeyMatcherPatternImpl<String>();

        // DO WORK
        Set<String> result1 = keyMatcher.getMatchingKeysFromArray( keyprefix1 + ".+", keyArray );
        Set<String> result2 = keyMatcher.getMatchingKeysFromArray( keyprefix2 + ".+", keyArray );

        // VERIFY
        assertEquals( "Wrong number returned 1: " + result1, numToInsertPrefix1, result1.size() );
        assertEquals( "Wrong number returned 2: " + result2, numToInsertPrefix2, result2.size() );
    }
}
