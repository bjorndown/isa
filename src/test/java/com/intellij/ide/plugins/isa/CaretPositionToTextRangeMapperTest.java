package com.intellij.ide.plugins.isa;

import com.intellij.openapi.util.TextRange;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class CaretPositionToTextRangeMapperTest {
    private CaretPositionToTextRangeMapper testee;

    @Test
    public void testName() throws Exception {
        final TextElement columnList = new TextElement("id, what", new TextRange(18, 26));
        final TextElement valuesList = new TextElement("10, \"wh\"", new TextRange(36, 44));

        testee = new CaretPositionToTextRangeMapper(columnList, valuesList);

        assertThatSelectionForCaretPosition(35).startsAt(0).endsAt(0);
        assertThatSelectionForCaretPosition(36).startsAt(0).endsAt(2);
        assertThatSelectionForCaretPosition(37).startsAt(0).endsAt(2);
        assertThatSelectionForCaretPosition(38).startsAt(0).endsAt(2);
        assertThatSelectionForCaretPosition(39).startsAt(3).endsAt(8);
        assertThatSelectionForCaretPosition(40).startsAt(3).endsAt(8);
        assertThatSelectionForCaretPosition(41).startsAt(3).endsAt(8);
        assertThatSelectionForCaretPosition(42).startsAt(3).endsAt(8);
        assertThatSelectionForCaretPosition(43).startsAt(3).endsAt(8);
        assertThatSelectionForCaretPosition(44).startsAt(3).endsAt(8);
        assertThatSelectionForCaretPosition(45).startsAt(0).endsAt(0);
    }

    private TextRangeAsserter assertThatSelectionForCaretPosition(final int caretPosition) {
        return new TextRangeAsserter(testee.getNewColumnListSelection(caretPosition), caretPosition);
    }

    private class TextRangeAsserter {
        private final TextRange actualSelection;
        private final int caretPosition;

        public TextRangeAsserter(final TextRange actualSelection, final int caretPosition) {
            this.actualSelection = actualSelection;
            this.caretPosition = caretPosition;
        }

        public TextRangeAsserter startsAt(final int expectedStartOffset) {
            return assertEquals("start", expectedStartOffset, actualSelection.getStartOffset());
        }

        public TextRangeAsserter endsAt(final int expectedEndOffset) {
            return assertEquals("end", expectedEndOffset, actualSelection.getEndOffset());
        }

        private TextRangeAsserter assertEquals(final String text, final int expectedOffset, final int actualOffset) {
            final String reason = String.format("Expected %s offset for caretPosition=%d should be %d but was %d",
                    text, caretPosition, expectedOffset, actualOffset);
            assertThat(reason, actualOffset, is(expectedOffset));
            return this;
        }
    }
}
