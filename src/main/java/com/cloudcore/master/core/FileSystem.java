package com.cloudcore.master.core;

import com.cloudcore.master.server.Command;
import com.cloudcore.master.utils.FileUtils;
import com.cloudcore.master.utils.SimpleLogger;
import com.cloudcore.master.utils.Utils;
import com.sun.javafx.application.PlatformImpl;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class FileSystem {


    /* Fields */

    //public static String RootPath = Paths.get("").toAbsolutePath().toString() + File.separator;
    public static String RootPath = "C:\\Users\\Public\\Documents\\CloudCoin\\Accounts\\DefaultUser\\";
    public static String RootPathNoAccount = "C:\\Users\\Public\\Documents\\CloudCoin\\";
    // TODO: Don't upload these lines!

    private static final DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static String ExportPath = File.separator + Config.TAG_EXPORT + File.separator;


    public static String DetectedFolder = RootPath + Config.TAG_DETECTED + File.separator;
    public static String ExportFolder = RootPath + Config.TAG_EXPORT + File.separator;
    public static String ImportFolder = RootPath + Config.TAG_IMPORT + File.separator;
    public static String SuspectFolder = RootPath + Config.TAG_SUSPECT + File.separator;

    public static String BankFolder = RootPath + Config.TAG_BANK + File.separator;
    public static String FrackedFolder = RootPath + Config.TAG_FRACKED + File.separator;
    public static String CounterfeitFolder = RootPath + Config.TAG_COUNTERFEIT + File.separator;
    public static String LostFolder = RootPath + Config.TAG_LOST + File.separator;

    public static String CommandFolder = RootPath + Config.TAG_COMMAND + File.separator;
    public static String LogsFolder = RootPath + Config.TAG_LOGS + File.separator;
    public static String TemplateFolder = RootPath + Config.TAG_TEMPLATES + File.separator;

    public static String SystemLogsFolder = RootPathNoAccount + Config.TAG_LOGS + File.separator;


    /* Methods */

    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(DetectedFolder));
            Files.createDirectories(Paths.get(ExportFolder));
            Files.createDirectories(Paths.get(ImportFolder));
            Files.createDirectories(Paths.get(SuspectFolder));

            Files.createDirectories(Paths.get(BankFolder));
            Files.createDirectories(Paths.get(FrackedFolder));
            Files.createDirectories(Paths.get(CounterfeitFolder));
            Files.createDirectories(Paths.get(LostFolder));

            Files.createDirectories(Paths.get(CommandFolder));
            Files.createDirectories(Paths.get(LogsFolder));
            Files.createDirectories(Paths.get(TemplateFolder));

            Files.createDirectories(Paths.get(SystemLogsFolder));
        } catch (Exception e) {
            SimpleLogger.QuickLog("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void changeRootPath(String rootPath) {
        if (!rootPath.endsWith(File.separator))
            rootPath += File.separator;
        if (rootPath.contains("\\\\")) // Turn double-slashes into single-slashes
            rootPath = rootPath.replaceAll("\\\\\\\\", "\\\\");

        FileSystem.RootPath = rootPath;

        DetectedFolder = RootPath + Config.TAG_DETECTED + File.separator;
        ExportFolder = RootPath + Config.TAG_EXPORT + File.separator;
        ImportFolder = RootPath + Config.TAG_IMPORT + File.separator;
        SuspectFolder = RootPath + Config.TAG_SUSPECT + File.separator;
        BankFolder = RootPath + Config.TAG_BANK + File.separator;
        FrackedFolder = RootPath + Config.TAG_FRACKED + File.separator;
        CounterfeitFolder = RootPath + Config.TAG_COUNTERFEIT + File.separator;
        LostFolder = RootPath + Config.TAG_LOST + File.separator;
        CommandFolder = RootPath + Config.TAG_COMMAND + File.separator;
        LogsFolder = RootPath + Config.TAG_LOGS + File.separator;
        TemplateFolder = RootPath + Config.TAG_TEMPLATES + File.separator;

        SystemLogsFolder = RootPathNoAccount + Config.TAG_LOGS + File.separator;

        createDirectories();
    }

    public static void setDefaultRootPath() {
        String username = System.getProperty("user.name");
        RootPathNoAccount = "C:\\Users\\" + username + "\\Documents\\CloudCoin\\";
        RootPath = RootPathNoAccount + "Accounts\\DefaultUser\\";
        changeRootPath(RootPath);
        Utils.saveData("RootPath", RootPath);
        System.out.println(RootPath);
    }

    /**
     * Writes an array of CloudCoins to a single Stack file.
     *
     * @param fileData the file data.
     * @param filePath the absolute filepath of the CloudCoin file, without the extension.
     */
    public static boolean saveFile(byte[] fileData, String filePath) {
        try {
            Files.write(Paths.get(filePath), fileData, StandardOpenOption.CREATE_NEW);
            SimpleLogger.QuickLog("saved file " + filePath);
            return true;
        } catch (IOException e) {
            SimpleLogger.QuickLog(e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static int[] getTotalCoinsBank() {
        return getTotalCoinsBank(BankFolder);
    }
    public static int[] getTotalCoinsBank(String accountFolder) {
        int[] totals = new int[6];

        int[] bankTotals = FileUtils.countCoins(accountFolder);

        totals[5] = bankTotals[0];
        totals[0] = bankTotals[1];
        totals[1] = bankTotals[2];
        totals[2] = bankTotals[3];
        totals[3] = bankTotals[4];
        totals[4] = bankTotals[5];

        return totals;
    }

    public static File folderChooser() {
        AtomicReference<File> file = new AtomicReference<>();
        AtomicReference<Boolean> fileSelectFinished = new AtomicReference<>();
        fileSelectFinished.set(false);

        PlatformImpl.startup(() -> {
            DirectoryChooser d = new DirectoryChooser();
            d.setTitle("Choose Folder");
            d.setInitialDirectory(new File(RootPath));
            file.set(d.showDialog(null));
            fileSelectFinished.set(true);
        });

        while (fileSelectFinished.get() == false) {}

        return file.get();
    }

    public static File[] fileChooser() {
        return fileChooser(null);
    }
    public static File[] fileChooser(String filepath) {
        FileDialog dialog = new java.awt.FileDialog((java.awt.Frame) null);
        dialog.setDirectory(filepath);
        dialog.setVisible(true);
        dialog.setMultipleMode(true);
        File[] result = dialog.getFiles();
        if (result == null)
            return null;
        else
            return result;
    }

    public static void moveFile(String fileName, String sourceFolder, String targetFolder, boolean replaceCoins) {
        String newFilename = fileName;
        if (!replaceCoins) {
            String[] suspectFileNames = FileUtils.selectFileNamesInFolder(targetFolder);
            for (String suspect : suspectFileNames) {
                SimpleLogger.QuickLog(suspect + " == " + newFilename);
                if (suspect.equals(fileName)) {
                    newFilename = FileUtils.ensureFilenameUnique(fileName, ".stack", targetFolder);
                    break;
                }
            }
        }

        try {
            Files.move(Paths.get(sourceFolder + fileName), Paths.get(targetFolder + newFilename), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            SimpleLogger.QuickLog(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public static void saveCommand(Command command) throws IOException {
        String commandName = (command.commandName != null) ? command.commandName : command.command;
        String filename = Utils.ensureFilenameUnique(commandName + LocalDateTime.now().format(timestampFormat),
                "", CommandFolder);
        Files.createDirectories(Paths.get(CommandFolder));
        Files.write(Paths.get(CommandFolder + filename), Utils.createGson().toJson(command).getBytes());
    }

    /**
     * Loads all CloudCoins from a specific folder.
     *
     * @param folder the folder to search for CloudCoin files.
     * @return an ArrayList of all CloudCoins in the specified folder.
     */
    public static ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] filenames = FileUtils.selectFileNamesInFolder(folder);
        for (String filename : filenames) {
            int index = filename.lastIndexOf('.');
            if (index == -1) continue;

            String extension = filename.substring(index + 1);

            switch (extension) {
                case "stack":
                    ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(folder, filename);
                    folderCoins.addAll(coins);
                    break;
            }
        }

        return folderCoins;
    }
}

