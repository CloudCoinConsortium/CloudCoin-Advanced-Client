package com.cloudcore.master.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableHeaderRenderer extends DefaultTableCellRenderer {

    private static Border border = BorderFactory.createMatteBorder(0, 0, 0, 2, (Color) UIManager.get("TableHeader.cellBorder"));
    private static Border borderPadding = BorderFactory.createEmptyBorder(0, 6, 2, 0);

    public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                            boolean selected, boolean focus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, col);

        setVerticalAlignment(SwingConstants.CENTER);
        setBackground((Color) UIManager.get("TableHeader.background"));
        setBorder(BorderFactory.createCompoundBorder(border, borderPadding));
        return c;
    }
}

