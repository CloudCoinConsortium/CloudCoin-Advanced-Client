package com.cloudcore.master;

import com.cloudcore.master.core.FileSystem;
import com.cloudcore.master.utils.FileUtils;
import com.cloudcore.master.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class StatusParser {

    public static int[] getTotalBank() {
        int[] totals = new int[6];
        Path path = Paths.get(FileSystem.SystemLogsFolder + "ShowCoins" + File.separator);
        System.out.println(path.toString());
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
            return new int[0];
        }

        String[] files = FileUtils.selectFileNamesInFolder(path.toString());
        for (String file : files) {
            if (file.startsWith("Bank")) {
                System.out.println("splitting " + file);
                String[] amounts = file.split("\\.");
                System.out.println(Arrays.toString(amounts));
                for (int i = 1; i < 6; i++)
                    totals[i - 1] = Utils.parseInt(amounts[i]);
                break;
            }
        }
        return totals;
    }
}
