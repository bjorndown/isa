package com.intellij.ide.plugins.isa;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.TextRange;

import java.util.*;

/**
 *  Maps position of caret in VALUES list to text range of corresponding column.
 */
public class CaretPositionToTextRangeMapper {

    private final Map<Integer, TextRange> valuesStartOffsetToColumnsTextRangeMap;

    public CaretPositionToTextRangeMapper(final TextElement columnsText, final TextElement valuesText) {
        final List<String> columnsList = splitTextByComma(columnsText);
        final List<String> valuesList = splitTextByComma(valuesText);

        valuesStartOffsetToColumnsTextRangeMap = createStartOffsetToTextRangeMap(valuesText, valuesList, columnsText, columnsList);
    }

    private Map<Integer, TextRange> createStartOffsetToTextRangeMap(final TextElement sourceTextRange, final List<String> sourceElementList,
                                                                    final TextElement targetTextRange, final List<String> targetElementList) {
        if (sourceElementList.size() != targetElementList.size()) {
            throw new IllegalArgumentException("Element lists not of same size.");
        }

        final Map<Integer,TextRange> sourceStartOffsetToTargetTextRangeMap = new HashMap<Integer, TextRange>();

        int currentSourceOffset = sourceTextRange.getStartOffset();
        int currentTargetOffset = targetTextRange.getStartOffset();

        for (int index = 0; index < sourceElementList.size(); index++) {
            final int currentTargetElementLength = targetElementList.get(index).length();

            final TextRange currentTextRange = new TextRange(currentTargetOffset, currentTargetOffset + currentTargetElementLength);

            sourceStartOffsetToTargetTextRangeMap.put(currentSourceOffset, currentTextRange);

            currentSourceOffset += sourceElementList.get(index).length() + 1;
            currentTargetOffset += currentTargetElementLength + 1;
        }

        sourceStartOffsetToTargetTextRangeMap.put(currentSourceOffset, new TextRange(0, 0));

        return sourceStartOffsetToTargetTextRangeMap;
    }

    private List<String> splitTextByComma(final TextElement textElement) {
        return Arrays.asList(textElement.getText().split(","));
    }

    public TextRange getNewColumnListSelection(final int caretPosition) {
        final int nextLowestStartOffset = getNextLowestStartOffsetFor(caretPosition);
        final TextRange textRange = valuesStartOffsetToColumnsTextRangeMap.get(nextLowestStartOffset);
        return nextLowestStartOffset == 0 ? new TextRange(0, 0) : textRange;
    }

    private int getNextLowestStartOffsetFor(final int caretPosition) {
        final List<Integer> startOffsets = Lists.newArrayList(valuesStartOffsetToColumnsTextRangeMap.keySet());
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
