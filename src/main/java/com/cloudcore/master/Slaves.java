package com.cloudcore.master;

import com.cloudcore.master.core.FileSystem;
import com.cloudcore.master.gui.DesktopGui;
import com.cloudcore.master.server.Command;
import com.cloudcore.master.utils.Utils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Slaves {

    public static boolean createCommandBackupper(String account, File toPath) {
        if (toPath == null || !toPath.exists() || !toPath.isDirectory())
            return false;

        Command command = new Command();
        command.command = "backup";
        command.account = (account == null || account.length() == 0) ? "default" : account;
        command.toPath = toPath.getAbsolutePath();
        try {
            FileSystem.saveCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void createCommandEraser(String account) {
        Command command = new Command();
        command.command = "eraser";
        command.account = (account == null || account.length() == 0) ? "default" : account;
        try {
            FileSystem.saveCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createCommandExporter(String account, int amount, int type, String tag) {
        Command command = new Command();
        command.command = "exporter";
        command.account = (account == null || account.length() == 0) ? "default" : account;
        command.amount = amount;
        command.type = type;
        command.tag = (tag.length() == 0) ? null : tag;
        try {
            FileSystem.saveCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createCommandReauthenticator(String account) {
        Command command = new Command();
        command.command = "reauthenticator";
        command.account = (account == null || account.length() == 0) ? "default" : account;
        try {
            FileSystem.saveCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createCommandShowCoins(String account) {
        Command command = new Command();
        command.command = "showcoins";
        command.account = (account == null || account.length() == 0) ? "default" : account;
        try {
            FileSystem.saveCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createCommandTranslate(String language) {
        Command command = new Command();
        command.command = "translate";
        command.language = language;
        try {
            FileSystem.saveCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createCommandToVault(String commandType, String account, String cloudCoin, String passphrase) {
        Command command = new Command();
        command.commandName = "vaulter";
        command.command = commandType;
        command.account = (account == null || account.length() == 0) ? "default" : account;
        command.cloudcoin = cloudCoin;
        command.passphrase = passphrase;
        try {
            FileSystem.saveCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeWorkspaceFolder(JFrame topWindow) {
        File folder = FileSystem.folderChooser();
        if (folder == null || !folder.exists() || !folder.isDirectory())
            return;
        if (folder.getAbsolutePath().contains(" ")) {
            DesktopGui.createPopup(topWindow, "Cannot choose a directory containing a space.");
            return;
        }

        Utils.saveData("RootPath", folder.getAbsolutePath());
        FileSystem.changeRootPath(folder.getAbsolutePath());
    }
}
