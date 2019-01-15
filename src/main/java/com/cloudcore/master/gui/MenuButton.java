package com.cloudcore.master.gui;

import javax.swing.*;
import java.awt.*;

public class MenuButton extends JButton {

    private Color colorHover = null;
    private Color colorPressed = null;

    public MenuButton() {
        this(null);
    }

    public MenuButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
    }

    public MenuButton(String text, Color colorPressed, Color colorHover) {
        super(text);
        super.setContentAreaFilled(false);
        this.colorPressed = colorPressed;
        this.colorHover = colorHover;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isPressed() && colorPressed != null) {
            g.setColor(colorPressed);
        } else if (getModel().isRollover() && colorHover != null) {
            g.setColor(colorHover);
        } else {
            g.setColor(getBackground());
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public void setContentAreaFilled(boolean b) {
    }

    public Color getColorHover() {
        return colorHover;
    }

    public void setColorHover(Color colorHover) {
        this.colorHover = colorHover;
    }

    public Color getColorPressed() {
        return colorPressed;
    }

    public void setColorPressed(Color colorPressed) {
        this.colorPressed = colorPressed;
    }
}
