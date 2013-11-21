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
        TextElement columnList = new TextElement("id, what", new TextRange(18, 26));
        TextElement valuesList = new TextElement("10, \"wh\"", new TextRange(36, 44));

        testee = new CaretPositionToTextRangeMapper(columnList, valuesList);

        assertThatSelectionForCaretPosition(35).startsAt(0).endsAt(0);
        assertThatSelectionForCaretPosition(36).startsAt(18).endsAt(20);
        assertThatSelectionForCaretPosition(37).startsAt(18).endsAt(20);
        assertThatSelectionForCaretPosition(38).startsAt(18).endsAt(20);
        assertThatSelectionForCaretPosition(39).startsAt(21).endsAt(26);
        assertThatSelectionForCaretPosition(40).startsAt(21).endsAt(26);
        assertThatSelectionForCaretPosition(41).startsAt(21).endsAt(26);
        assertThatSelectionForCaretPosition(42).startsAt(21).endsAt(26);
        assertThatSelectionForCaretPosition(43).startsAt(21).endsAt(26);
        assertThatSelectionForCaretPosition(44).startsAt(21).endsAt(26);
        assertThatSelectionForCaretPosition(45).startsAt(0).endsAt(0);
    }

    private TextRangeAsserter assertThatSelectionForCaretPosition(int caretPosition) {
        return new TextRangeAsserter(testee.getNewColumnListSelection(caretPosition), caretPosition);
    }

    private class TextRangeAsserter {
        private final TextRange newSelection;
        private int caretPosition;

        public TextRangeAsserter(TextRange newSelection, int caretPosition) {
            this.newSelection = newSelection;
            this.caretPosition = caretPosition;
        }

        public TextRangeAsserter startsAt(int start) {

            return assertEquals("Selection for caretPosition=" + caretPosition + " should start at " + start + " but was " + newSelection.getStartOffset(),
                    start, newSelection.getStartOffset());
        }

        public TextRangeAsserter endsAt(int end) {
            return assertEquals("Selection for caretPosition=" + caretPosition + " should end at " + end + " but was " + newSelection.getEndOffset(),
                    end, newSelection.getEndOffset());
        }

        private TextRangeAsserter assertEquals(String text, int i, int offset) {
            assertThat(text, offset, is(i));
            return this;
        }
    }
}
