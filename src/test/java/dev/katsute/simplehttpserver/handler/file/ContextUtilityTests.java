package dev.katsute.simplehttpserver.handler.file;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

final class ContextUtilityTests {

     @ParameterizedTest
     @MethodSource("contextArgs")
     final void testContexts(final String expected, final String input, final boolean leading, final boolean trailing){
          Assertions.assertEquals(expected, ContextUtility.getContext(input, leading, trailing));
     }

     static Stream<Arguments> contextArgs(){
          return Stream.of(
               Arguments.of("", "/", false, false),
               Arguments.of("/", "", true, false),
               Arguments.of("/", "", false, true ),
               Arguments.of("/", "", true, true ),
               Arguments.of("a", "a", false, false),
               Arguments.of("/a", "a", true, false),
               Arguments.of("a/", "a", false, true ),
               Arguments.of("/a/", "a", true, true ),
               Arguments.of("testNone", "/testNone/", false, false),
               Arguments.of("/testLeading", "testLeading", true, false),
               Arguments.of("testTrailing/", "testTrailing", false, true ),
               Arguments.of("/testBoth/", "testBoth", true, true ),
               Arguments.of("testNoneBackSlash", "\\testNoneBackSlash\\", false, false),
               Arguments.of("/testBackSlash/", "\\testBackSlash\\", true, true ),
               Arguments.of("/testConsecutiveBackSlash/", "\\\\testConsecutiveBackSlash\\\\", true, true ),
               Arguments.of("/testConsecutiveForwardSlash/", "//testConsecutiveForwardSlash//", true, true ),
               Arguments.of("/testWhitespace/", " /testWhitespace/ ", true, true),
               Arguments.of("/ testWhitespace /", "/ testWhitespace /", true, true),
               Arguments.of(" testWhitespace ", "/ testWhitespace /", false, false),
               Arguments.of("testWhitespace", " testWhitespace ", false, false),
               Arguments.of("/testWhitespace/", " /testWhitespace/ ", true, true)
          );
     }

     @ParameterizedTest
     @MethodSource("joinArgs")
     final void testJoin(final String expected, final boolean leading, final boolean trailing, final String input, final String join){
          Assertions.assertEquals(expected, ContextUtility.joinContexts(leading, trailing, input, join));
     }

     static Stream<Arguments> joinArgs(){
          return Stream.of(
               Arguments.of("testBlank", false, false, "testBlank", ""),
               Arguments.of("/testBlank/", true, true, "testBlank", ""),
               Arguments.of("testBlank", false, false, "", "testBlank"),
               Arguments.of("/testBlank/", true, true, "", "testBlank"),
               Arguments.of("", false, false, "", ""),
               Arguments.of("/", true, true, "", ""),
               Arguments.of("trailing/slash", false, false, "trailing/", "slash/"),
               Arguments.of("/trailing/slash/", true, true, "trailing/", "slash/"),
               Arguments.of("leading/slash", false, false, "leading/", "slash/"),
               Arguments.of("/leading/slash/", true, true, "leading/", "slash/"),
               Arguments.of("double/slash", false, false, "/double/", "/slash/"),
               Arguments.of("/double/slash/", true, true, "/double/", "/slash/"),
               Arguments.of("no/slash", false, false, "no", "slash"),
               Arguments.of("/no/slash/", true, true, "no", "slash"),
               Arguments.of("consecutive/slash", false, false, "//consecutive//", "//slash//"),
               Arguments.of("/consecutive/slash/", true, true, "//consecutive//", "//slash//"),
               Arguments.of("mixed/slash", false, false, "\\mixed\\", "//slash//"),
               Arguments.of("/mixed/slash/", true, true, "\\mixed\\", "//slash//")
          );
     }

}
