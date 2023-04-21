package dev.katsute.simplehttpserver;

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

}