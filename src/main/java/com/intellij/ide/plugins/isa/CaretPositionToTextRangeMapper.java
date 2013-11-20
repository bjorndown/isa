package com.intellij.ide.plugins.isa;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.TextRange;

import java.util.*;

/**
 *  Maps position of caret in VALUES list to text range of corresponding column.
 */
public class CaretPositionToTextRangeMapper {

    private final Map<Integer, TextRange> columnsStartOffsetToColumnNumberMap;
    private final Map<Integer, TextRange> valuesStartOffsetToColumnNumberMap;

    public CaretPositionToTextRangeMapper(final TextElement columnsText, final TextElement valuesText) {
        final List<String> columnsList = splitTextByComma(columnsText);
        final List<String> valuesList = splitTextByComma(valuesText);

        List<TextRange> columnsTextRanges = buildTextRangeList(columnsList, columnsText);
        List<TextRange> valuesTextRanges = buildTextRangeList(valuesList, valuesText);

        columnsStartOffsetToColumnNumberMap = createStartOffsetToTextRangeMap(valuesText, columnsTextRanges);
        valuesStartOffsetToColumnNumberMap = createStartOffsetToTextRangeMap(columnsText, valuesTextRanges);
    }

    private List<TextRange> buildTextRangeList(final List<String> columnsList, final TextElement columnsText) {
        int currentOffset = columnsText.getStartOffset();

        List<TextRange> list = new ArrayList<TextRange>();

        for (int index = 0; index < columnsList.size(); index++) {
            list.add(new TextRange(currentOffset, currentOffset + columnsList.get(index).length()));
            currentOffset += columnsList.get(index).length() + 1;
        }

        list.add(new TextRange(columnsText.getEndOffset() + 1, columnsText.getEndOffset() + 1));

        return list;
    }

    private Map<Integer, TextRange> createStartOffsetToTextRangeMap(final TextElement textRange, final List<TextRange> columns) {
        int startOffset = textRange.getStartOffset();
        final Map<Integer,TextRange> startOffsetToTextRangeMap = new HashMap<Integer, TextRange>();

        for (int index = 0; index < columns.size(); index++) {
            final TextRange currentTextRange = columns.get(index);
            startOffsetToTextRangeMap.put(startOffset, currentTextRange);
            startOffset += currentTextRange.getLength() + 1;
        }

        return startOffsetToTextRangeMap;
    }

    private List<String> splitTextByComma(final TextElement textElement) {
        return Arrays.asList(textElement.getText().split(","));
    }

    public TextRange getNewColumnListSelection(final int caretPosition) {
        final int nextLowestStartOffset = getNextLowestStartOffsetFor(caretPosition);
        final TextRange textRange = columnsStartOffsetToColumnNumberMap.get(nextLowestStartOffset);
        return nextLowestStartOffset == 0 ? new TextRange(0, 0) : textRange;
    }

    private int getNextLowestStartOffsetFor(final int caretPosition) {
        final List<Integer> startOffsets = Lists.newArrayList(columnsStartOffsetToColumnNumberMap.keySet());
        Collections.sort(startOffsets);

        for (int i = 0; i < startOffsets.size() - 1; i++) {
            final Integer currentStartOffset = startOffsets.get(i);
            final Integer nextStartOffset = startOffsets.get(i + 1);

            if (caretPosition < currentStartOffset) {
                return 0;
            }

            if (caretPosition >= currentStartOffset && caretPosition < nextStartOffset) {
                return currentStartOffset;
            }
        }
        return 0;
    }

}
