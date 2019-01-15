package com.cloudcore.master;

import com.cloudcore.master.core.FileSystem;
import com.cloudcore.master.gui.DesktopGui;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Main {


    public static void main(String[] args) {
        new DesktopGui();
    }

    private static void setup() {
        FileSystem.createDirectories();
    }

    public static void showFolder() {
        showFolder(null);
    }
    public static void showFolder(String subfolder) {
        if (subfolder == null) subfolder = "";

        try {
            Desktop.getDesktop().open(new File(FileSystem.RootPath + subfolder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void showSpecificFolder(String folder) {
        if (folder == null)
            return;

        try {
            Desktop.getDesktop().open(new File(folder));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getParameterForLogging(String param) {
        return (null == param) ? ":NULL:" : param;
    }
}
