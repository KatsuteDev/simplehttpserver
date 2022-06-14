package dev.katsute.simplehttpserver;

import org.junit.jupiter.api.Test;

import static dev.katsute.simplehttpserver.ContextUtility.*;
import static org.junit.jupiter.api.Assertions.*;

final class ContextUtilityTests {

     @Test
     public final void testContexts(){
          assertEquals("", getContext("/", false, false));
          assertEquals("/", getContext("", true, false));
          assertEquals("/", getContext("", false, true ));
          assertEquals("/", getContext("", true, true ));
          assertEquals("a", getContext("a", false, false));
          assertEquals("/a", getContext("a", true, false));
          assertEquals("a/", getContext("a", false, true ));
          assertEquals("/a/", getContext("a", true, true ));
          assertEquals("testNone", getContext("/testNone/", false, false));
          assertEquals("/testLeading", getContext("testLeading", true, false));
          assertEquals("testTrailing/", getContext("testTrailing", false, true ));
          assertEquals("/testBoth/", getContext("testBoth", true, true ));
          assertEquals("testNoneBackSlash", getContext("\\testNoneBackSlash\\", false, false));
          assertEquals("/testBackSlash/", getContext("\\testBackSlash\\", true, true ));
          assertEquals("/testConsecutiveBackSlash/", getContext("\\\\testConsecutiveBackSlash\\\\", true, true ));
          assertEquals("/testConsecutiveForwardSlash/", getContext("//testConsecutiveForwardSlash//", true, true ));
          assertEquals("/testWhitespace/", getContext(" /testWhitespace/ ", true, true));
          assertEquals("/ testWhitespace /", getContext("/ testWhitespace /", true, true));
          assertEquals(" testWhitespace ", getContext("/ testWhitespace /", false, false));
          assertEquals("testWhitespace", getContext(" testWhitespace ", false, false));
          assertEquals("/testWhitespace/", getContext(" /testWhitespace/ ", true, true));
     }

}
