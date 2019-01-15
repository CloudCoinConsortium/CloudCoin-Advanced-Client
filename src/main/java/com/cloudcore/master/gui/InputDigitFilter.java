package com.cloudcore.master.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Pattern;

public class InputDigitFilter extends DocumentFilter {

    private Pattern regex = Pattern.compile("\\d+");

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet a) throws BadLocationException {
        if (null == text || regex.matcher(text).matches())
            return;

        super.insertString(fb, offset, text, a);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet a) throws BadLocationException {
        if (null == text || !regex.matcher(text).matches())
            return;

        super.replace(fb, offset, length, text, a);
    }
}
