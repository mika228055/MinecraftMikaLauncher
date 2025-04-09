package net.mika;

import java.nio.file.Path;
import java.util.*;
import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModulesAndClasses {
    public static void findJarFiles(File directory, List<String> path, boolean isForgeModules) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) findJarFiles(file, path, isForgeModules);
                    else if (file.isFile() && file.getName().endsWith(".jar")) {
                        if (!path.contains(file.getAbsolutePath())) {
                            if (isForgeModules && !isForgeModule(file)) continue;
                            else path.add(file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    public static boolean isForgeModule(File file) {
        String[] requiredJars = {"bootstraplauncher", "securejarhandler", "asm-commons", "asm-util", "asm-analysis", "asm-tree", "asm"};
        for (String jar : requiredJars) {
            if (file.getName().contains(jar)) return true;
        }
        return false;
    }
}
