package dev.katsute.simplehttpserver.handler.file;

import org.junit.jupiter.api.Test;

import static dev.katsute.simplehttpserver.handler.file.ContextUtility.*;
import static org.junit.jupiter.api.Assertions.*;

final class ContextUtilityTests {

     @Test
     final void testContexts(){
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

     @Test
     final void testJoin(){
          assertEquals("testBlank", joinContexts(false, false, "testBlank", ""));
          assertEquals("/testBlank/", joinContexts(true, true, "testBlank", ""));
          assertEquals("testBlank", joinContexts(false, false, "", "testBlank"));
          assertEquals("/testBlank/", joinContexts(true, true, "", "testBlank"));
          assertEquals("", joinContexts(false, false, "", ""));
          assertEquals("/", joinContexts(true, true, "", ""));
          assertEquals("trailing/slash", joinContexts(false, false, "trailing/", "slash/"));
          assertEquals("/trailing/slash/", joinContexts(true, true, "trailing/", "slash/"));
          assertEquals("leading/slash", joinContexts(false, false, "leading/", "slash/"));
          assertEquals("/leading/slash/", joinContexts(true, true, "leading/", "slash/"));
          assertEquals("double/slash", joinContexts(false, false, "/double/", "/slash/"));
          assertEquals("/double/slash/", joinContexts(true, true, "/double/", "/slash/"));
          assertEquals("no/slash", joinContexts(false, false, "no", "slash"));
          assertEquals("/no/slash/", joinContexts(true, true, "no", "slash"));
          assertEquals("consecutive/slash", joinContexts(false, false, "//consecutive//", "//slash//"));
          assertEquals("/consecutive/slash/", joinContexts(true, true, "//consecutive//", "//slash//"));
          assertEquals("mixed/slash", joinContexts(false, false, "\\mixed\\", "//slash//"));
          assertEquals("/mixed/slash/", joinContexts(true, true, "\\mixed\\", "//slash//"));
     }

}
