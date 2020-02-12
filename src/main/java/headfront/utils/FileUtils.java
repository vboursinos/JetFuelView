package headfront.utils;

import headfront.guiwidgets.PopUpDialog;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Deepak on 26/07/2016.
 */
public class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    private static File lastUsedFile = null;
    private static File lastUsedDir = null;

    public static String readFile() {
        File selectedFile = null;
        try {
            FileChooser fileChooser = getFileChooser();
            fileChooser.setTitle("Open File.");
            selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                List<String> strings = Files.readAllLines(Paths.get(selectedFile.getAbsolutePath()));
                StringBuilder builder = new StringBuilder();
                strings.forEach(text -> {
                    builder.append(text);
                    builder.append("\n");
                });
                lastUsedFile = selectedFile;
                lastUsedDir = selectedFile.getParentFile();
                return builder.toString();
            }
        } catch (IOException e) {
            LOG.error("Unable to read file " + selectedFile.getAbsolutePath(), e);
            PopUpDialog.showWarningPopup("Unable to read file", e.getMessage() + " File " + selectedFile);
        }
        return null;
    }

    public static Properties readProperties() {
        final String text = readFile();
        if (text != null) {
            Properties prop = new Properties();
            try {
                prop.load(new StringReader(text));
            } catch (IOException e) {
                LOG.error("Unable to properties from text " + text, e);
            }
            return prop;
        }
        return null;


    }

    public static void saveFile(String allLines, boolean ack) {
        FileChooser fileChooser = getFileChooser();
        fileChooser.setTitle("Save File.");
        final File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            lastUsedFile = selectedFile;
            lastUsedDir = selectedFile.getParentFile();
            new Thread(() -> {
                try {
                    List<String> linesToWrite = new ArrayList<>();
                    String[] lines = allLines.split("\n");
                    for (String line : lines) {
                        linesToWrite.add(line);
                    }
                    Files.write(Paths.get(selectedFile.getAbsolutePath()), linesToWrite);
                    if (ack) {
                        PopUpDialog.showInfoPopup("Saved file", "File " + selectedFile + " saved.");
                    }
                } catch (IOException e) {
                    LOG.error("Unable to save file " + selectedFile.getAbsolutePath(), e);
                    PopUpDialog.showWarningPopup("Unable to save file", e.getMessage() + " File " + selectedFile);
                }
            }).start();
        }
    }

    public static void saveFile(File selectedFile, List linesToWrite, boolean ack) {
        if (selectedFile != null) {
            lastUsedFile = selectedFile;
            lastUsedDir = selectedFile.getParentFile();
            new Thread(() -> {
                try {
                    Files.write(Paths.get(selectedFile.getAbsolutePath()), linesToWrite);
                    if (ack) {
                        PopUpDialog.showInfoPopup("Saved file", "File " + selectedFile + " saved.");
                    }
                } catch (IOException e) {
                    LOG.error("Unable to save file " + selectedFile.getAbsolutePath(), e);
                    PopUpDialog.showWarningPopup("Unable to save file", e.getMessage() + " File " + selectedFile);
                }
            }).start();
        }
    }


    public static void saveFile(Properties properties, boolean ack) {
        FileChooser fileChooser = getFileChooser();
        fileChooser.setTitle("Save File.");
        final File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            lastUsedFile = selectedFile;
            lastUsedDir = selectedFile.getParentFile();
            new Thread(() -> {
                try {
                    properties.store(new FileOutputStream(selectedFile), "JetFuel Filter file");
                    if (ack) {
                        PopUpDialog.showInfoPopup("Saved file", "File " + selectedFile + " saved.");
                    }
                } catch (IOException e) {
                    LOG.error("Unable to save file " + selectedFile.getAbsolutePath(), e);
                    PopUpDialog.showWarningPopup("Unable to save file", e.getMessage() + " File " + selectedFile);
                }
            }).start();
        }
    }

    public static List<Path> getFiles(String folder, String filter) {
        final List<Path> files = new ArrayList<>();
        Path path = Paths.get(folder);
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            for (Path entry : stream) {
                File file = entry.toFile();
                if (file.isFile()) {
                    if (file.getAbsolutePath().endsWith(filter)) {
                        files.add(entry);
                    }
                }
            }
            stream.close();
        } catch (IOException e) {
            LOG.error("Unable to get files from  " + folder, e);
            PopUpDialog.showWarningPopup("Unable to get files from " + folder, e.getMessage());
        }
        return files;
    }

    private static FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser();
        if (lastUsedFile != null) {
            fileChooser.setInitialFileName(lastUsedFile.getName());
            fileChooser.setInitialDirectory(lastUsedDir);
        }
        return fileChooser;
    }
}
