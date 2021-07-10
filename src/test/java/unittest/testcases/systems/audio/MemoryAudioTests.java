package unittest.testcases.systems.audio;

import tech.fastj.math.Maths;

import tech.fastj.systems.audio.AudioManager;
import tech.fastj.systems.audio.MemoryAudio;
import tech.fastj.systems.audio.state.PlaybackState;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import unittest.EnvironmentHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class MemoryAudioTests {

    private static final Path TestAudioPath = Path.of("src/test/resources/test_audio.wav");

    @BeforeAll
    public static void onlyRunIfAudioOutputIsSupported() {
        assumeTrue(EnvironmentHelper.DoesEnvironmentSupportAudioOutput);
    }

    @Test
    void checkLoadMemoryAudioInstance_shouldMatchExpectedValues() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);

        assertEquals(TestAudioPath, audio.getAudioPath(), "After loading the audio into memory, the gotten audio should have the same path object as the one used to load it in.");
        assertEquals(PlaybackState.Stopped, audio.getCurrentPlaybackState(), "After loading the audio into memory, the gotten audio should be in the \"stopped\" playback state.");
        assertEquals(PlaybackState.Stopped, audio.getPreviousPlaybackState(), "After loading the audio into memory, the gotten audio's previous playback state should also be \"stopped\".");
        assertEquals(0L, audio.getPlaybackPosition(), "After loading the audio into memory, the gotten audio should be at the very beginning with playback position.");
    }

    @Test
    void checkSetLoopPoints() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);

        int expectedLoopStart = MemoryAudio.LoopFromStart;
        int expectedLoopEnd = MemoryAudio.LoopAtEnd;
        audio.setLoopPoints(expectedLoopStart, expectedLoopEnd);

        assertEquals(expectedLoopStart, audio.getLoopStart(), "The audio loop points should have been set.");
        assertEquals(expectedLoopEnd, audio.getLoopEnd(), "The audio loop points should have been set.");
        assertTrue(audio.shouldLoop(), "Setting the loop points should cause the audio to need to loop.");
    }

    @Test
    void trySetLoopPoints_butLoopEndIsLessThanNegativeOne() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);

        int expectedLoopStart = MemoryAudio.LoopFromStart;
        int invalidLoopEnd = -1337;

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> audio.setLoopPoints(expectedLoopStart, invalidLoopEnd));
        String expectedExceptionMessage = "The endpoint for the loop should not be less than -1.";
        assertEquals(expectedExceptionMessage, exception.getMessage(), "The expected error message should match the actual error message.");
    }

    @Test
    void trySetLoopPoints_butLoopStartIsLargerThanLoopEnd() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);

        int invalidLoopStart = 37;
        int invalidLoopEnd = 13;

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> audio.setLoopPoints(invalidLoopStart, invalidLoopEnd));
        String expectedExceptionMessage = "The loop starting point should be less than or equal to the loop ending point.";
        assertEquals(expectedExceptionMessage, exception.getMessage(), "The expected error message should match the actual error message.");
    }

    @Test
    void checkSetLoopCount_toRandomInteger() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);

        int randomLoopCount = Maths.randomInteger(13, 37);
        audio.setLoopCount(randomLoopCount);

        assertEquals(randomLoopCount, audio.getLoopCount(), "The audio loop count should be set.");
        assertTrue(audio.shouldLoop(), "Setting the loop to a value that is not \"Audio.StopLooping\" should cause the audio to need to loop.");
    }

    @Test
    void checkSetLoopCount_toAudioStopLooping() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);

        int expectedLoopCount = MemoryAudio.StopLooping;
        audio.setLoopCount(expectedLoopCount);

        assertEquals(expectedLoopCount, audio.getLoopCount(), "The audio loop count should be set.");
        assertFalse(audio.shouldLoop(), "Setting the loop to \"Audio.StopLooping\" should cause the audio to not need to loop.");
    }

    @Test
    void trySetLoopCount_toInvalidValue() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);

        int invalidLoopCount = -13;

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> audio.setLoopCount(invalidLoopCount));
        String expectedExceptionMessage = "The loop count should not be less than -1.";
        assertEquals(expectedExceptionMessage, exception.getMessage(), "The expected error message should match the actual error message.");
    }

    @Test
    void checkGetAudioAfterUnloading() {
        MemoryAudio audio = AudioManager.loadMemoryAudioInstance(TestAudioPath);
        assertNotNull(AudioManager.getMemoryAudio(audio.getID()), "The audio should have been loaded into the audio manager successfully.");

        AudioManager.unloadMemoryAudioInstance(audio.getID());
        assertNull(AudioManager.getMemoryAudio(audio.getID()), "After unloading the audio, it should not be present in the audio manager.");
    }
}
