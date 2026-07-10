package controller;

import action.file.SaveFileAction;
import model.OutlineNode;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileController {

    private final JFrame jFrame;
    private final TreeModel treeModel;
    private File saveLocation;

    public FileController(TreeModel treeModel, JFrame jFrame) {
        this.jFrame = jFrame;
        this.treeModel = treeModel;
    }

    // Platzhalter
    public void newDocument()    { JOptionPane.showMessageDialog(jFrame, "Neu – noch nicht implementiert.");            }

    public void openDocument()   { JOptionPane.showMessageDialog(jFrame, "Öffnen – noch nicht implementiert.");         }

    public void saveDocument() throws Exception {
        String jsonString = export((OutlineNode) treeModel.getRoot());

        if (saveLocation != null) {
            Files.writeString(saveLocation.toPath(), jsonString);
        } else {
            saveDialog(jsonString);
        }
    }

    public void saveDocumentAs() throws Exception {
        String jsonString = export((OutlineNode) treeModel.getRoot());
        saveDialog(jsonString);
    }

    private void saveDialog(String jsonString) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Datei Speichern");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON-Datein (*.json", "json"));

        int result = fileChooser.showSaveDialog(jFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            saveLocation = fileChooser.getSelectedFile();
            if (!saveLocation.getName().toLowerCase().endsWith(".json")) {
                saveLocation = new File(saveLocation.getAbsolutePath() + ".json");
                Files.writeString(saveLocation.toPath(), jsonString);
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(jFrame, "Fehler beim Speichern der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }



    private static ObjectNode toJsonNode(OutlineNode treeNode, ObjectMapper mapper) {

        ObjectNode node = mapper.createObjectNode();

        node.put("title", treeNode.getTitle());
        node.put("content", treeNode.getContent());

        ArrayNode children = mapper.createArrayNode();

        for (int i = 0; i < treeNode.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) treeNode.getChildAt(i);

            children.add(toJsonNode(child, mapper));
        }

        node.set("children", children);

        return node;
    }

    public static String export(OutlineNode root) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode jsonRoot = toJsonNode(root, mapper);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonRoot);
    }
}