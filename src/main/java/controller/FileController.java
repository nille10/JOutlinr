package controller;

import model.JSONParser;
import model.OutlineNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;

import model.XMLParser;
import view.AppPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import view.LatexExportDialog;
import view.MainWindow;

import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class FileController {

    private final MainWindow mainWindow;
    private final DefaultTreeModel treeModel;
    private final OutlineController outlineController;
    private File saveLocation;
    private FileTime lastSavedTime;

    public FileController(OutlineController outlineController, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.treeModel = outlineController.getTreeModel();
        this.outlineController = outlineController;
    }

    // Platzhalter
    public void newDocument() {
        if (checkSavedStatus() != SaveStatus.SAVE_FAILED) {

            outlineController.cancelActiveEditing();
            outlineController.deleteAllColumns();

            OutlineNode root = outlineController.getRootNode();

            root.removeAllChildren();
            root.setTitle("Outliner");
            root.setContent("");

            treeModel.reload();

            saveLocation = null;

            outlineController.setHeaderTitle("Outliner");

            outlineController.saved();

            mainWindow.getStatusBar().setText(" Bereit");
        }
    }

    public void openDocument() {
        if (checkSavedStatus() != SaveStatus.SAVE_FAILED) {
            int result = openDialog();
            if (result == 1) {

                try {
                    outlineController.deleteAllColumns();
                    importXML(saveLocation);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if (result == 2) {
                try {
                    outlineController.deleteAllColumns();
                    importJson(saveLocation);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            outlineController.saved();
        }
    }

    public SaveStatus saveDocument() throws Exception {
        if (saveLocation != null) {
            if (saveLocation.getName().toLowerCase().endsWith("xml")) {
                Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
            } else if (saveLocation.getName().toLowerCase().endsWith(".json")) {
                Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
            }
        } else {
            return saveDialog();
        }
        if(saveLocation != null)
            updateLastSavedTime();
        outlineController.saved();
        return SaveStatus.SAVED;
    }

    public void saveDocumentAs() throws Exception {
        saveDialog();
        updateLastSavedTime();
        outlineController.saved();
    }

    public void close() {
        mainWindow.dispatchEvent(new WindowEvent(mainWindow, WindowEvent.WINDOW_CLOSING));
    }

    public void exportAsLatex() throws IOException {

        LatexExportDialog.Mode mode = LatexExportDialog.askMode(mainWindow);

        if (mode == LatexExportDialog.Mode.CANCELLED) {
            return;
        }

        LatexExportDialog.LatexMetadata metadata = null;

        if (mode == LatexExportDialog.Mode.EXPERT) {
            metadata = LatexExportDialog.askMetadata(mainWindow);
            if (metadata == null) {
                return; // Nutzer hat abgebrochen
            }
        }

        File saveLocation = exportAsLatexDialog();
        if (saveLocation == null) {
            return;
        }

        String body = toLatexString((OutlineNode) treeModel.getRoot());
        String fullDocument = buildFullLatexDocument(metadata, body);

        Files.writeString(saveLocation.toPath(), fullDocument);
    }

    // Helper Methods

    private File exportAsLatexDialog() {
        CFileChooser fileChooser = new CFileChooser("Export als Latex",
                new FileNameExtensionFilter("Latex_Dateien (*.tex", "tex")
        );

        int result = fileChooser.getSaveResult(mainWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            return new File(fileChooser.getFile().getAbsolutePath() + ".tex");

        }
        return null;
    }

    private String buildFullLatexDocument(LatexExportDialog.LatexMetadata metadata, String body) {

        if (metadata == null) {
            return buildStandardLatexDocument(body);
        }

        return buildExpertLatexDocument(metadata, body);
    }

    private String buildStandardLatexDocument(String body) {

        String rootTitle = outlineController.getRootNode().getTitle();

        String heute = "\\today";

        StringBuilder tex = new StringBuilder();

        tex.append("\\documentclass{article}\n");
        tex.append("\\usepackage{graphicx} % Required for inserting images\n");
        tex.append("\\title{").append(escapeLatex(rootTitle)).append("}\n");
        tex.append("\\author{EINFÜGEN}\n");
        tex.append("\\date{").append(heute).append("}\n\n");

        tex.append("\\begin{document}\n");
        tex.append("\\maketitle\n\n");

        tex.append(body);

        tex.append("\\end{document}\n");

        return tex.toString();
    }

    private String buildExpertLatexDocument(LatexExportDialog.LatexMetadata metadata, String body) {

        StringBuilder tex = new StringBuilder();

        tex.append("\\documentclass[a4paper,12pt]{article}\n\n");

        tex.append("% ---- Encoding & Sprache ----\n");
        tex.append("\\usepackage[utf8]{inputenc}\n");
        tex.append("\\usepackage[T1]{fontenc}\n");
        tex.append("\\usepackage[ngerman]{babel}\n\n");

        tex.append("% ---- Grafiken, Farben, Layout ----\n");
        tex.append("\\usepackage{graphicx}\n");
        tex.append("\\usepackage{xcolor}\n");
        tex.append("\\usepackage{geometry}\n");
        tex.append("\\usepackage{calc}\n");
        tex.append("\\geometry{a4paper, margin=2.5cm}\n\n");

        tex.append("% ---- Hintergrundbild (optional, siehe \\BackgroundWave) ----\n");
        tex.append("\\usepackage{eso-pic}\n");
        tex.append("\\newcommand\\BackgroundWave{%\n");
        tex.append("\t\\put(0,0){%\n");
        tex.append("\t\t\\parbox[b][\\paperheight]{\\paperwidth}{%\n");
        tex.append("\t\t\t\\vfill\n");
        tex.append("\t\t\t\\centering\n");
        tex.append("\t\t\t\\vspace{12.0cm}\n");
        tex.append("\t\t\t\\includegraphics[width=\\paperwidth]{logo/hsel-welle-grey}%\n");
        tex.append("\t\t\t\\vfill\n");
        tex.append("}}}\n\n");

        tex.append("% ---- Fancy Boxes für Beispiele ----\n");
        tex.append("\\usepackage[framemethod=TikZ]{mdframed}\n\n");
        tex.append("\\newcounter{exmp}[chapter]\\setcounter{exmp}{0}\n");
        tex.append("\\renewcommand{\\theexmp}{\\arabic{chapter}.\\arabic{exmp}}\n\n");
        tex.append("\\newenvironment{exmp}[2][]{%\n");
        tex.append("\t\\refstepcounter{exmp}\n");
        tex.append("\t\\ifstrempty{#1}%\n");
        tex.append("\t{\\mdfsetup{%\n");
        tex.append("\t\t\tframetitle={%\n");
        tex.append("\t\t\t\t\\tikz[baseline=(current bounding box.east),outer sep=0pt]\n");
        tex.append("\t\t\t\t\\node[anchor=east,rectangle,fill=blue!20]\n");
        tex.append("\t\t\t\t{\\strut Beispiel~\\theexmp};}\n");
        tex.append("\t\t}%\n");
        tex.append("\t}{\\mdfsetup{%\n");
        tex.append("\t\t\tframetitle={%\n");
        tex.append("\t\t\t\t\\tikz[baseline=(current bounding box.east),outer sep=0pt]\n");
        tex.append("\t\t\t\t\\node[anchor=east,rectangle,fill=blue!20]\n");
        tex.append("\t\t\t\t{\\strut Beispiel~\\theexmp:~#1};}%\n");
        tex.append("\t\t}%\n");
        tex.append("\t}%\n");
        tex.append("\t\\mdfsetup{%\n");
        tex.append("\t\tnobreak=true,\n");
        tex.append("\t\tinnertopmargin=4pt,linecolor=blue!20,%\n");
        tex.append("\t\tlinewidth=2pt,topline=true,%\n");
        tex.append("\t\tframetitleaboveskip=\\dimexpr-\\ht\\strutbox\\relax%\n");
        tex.append("\t}\n");
        tex.append("\t\\begin{mdframed}[]\\relax%\n");
        tex.append("\t\t\\label{#2}}{\\end{mdframed}}\n\n");

        tex.append("\\usepackage{sectsty}\n");
        tex.append("\\allsectionsfont{\\sffamily}\n\n");

        tex.append("% ---- Kopf-/Fußzeile ----\n");
        tex.append("\\usepackage{fancyhdr}\n");
        tex.append("\\pagestyle{fancy}\n");
        tex.append("\\fancyhead{}\n");
        tex.append("\\fancyfoot{}\n");
        tex.append("\\renewcommand{\\chaptermark}[1]{\\markboth{\\chaptername \\ \\thechapter.\\ #1}{}}\n");
        tex.append("\\renewcommand{\\sectionmark}[1]{\\markright{\\thesection.\\ #1}{}}\n");
        tex.append("\\fancyhead[RO,LE]{\\thepage}\n");
        tex.append("\\fancyhead[LO]{\\rightmark}\n");
        tex.append("\\fancyhead[RE]{\\leftmark}\n");
        tex.append("\\renewcommand{\\headrulewidth}{0.4pt}\n\n");
        tex.append("\\fancypagestyle{plain}{%\n");
        tex.append("\t\\fancyhf{}\n");
        tex.append("\t\\fancyhead[RO,LE]{\\thepage}\n");
        tex.append("\t\\renewcommand{\\headrulewidth}{0.4pt}\n");
        tex.append("\t\\renewcommand{\\footrulewidth}{0pt}}\n\n");

        tex.append("% ---- Listen-Spacing ----\n");
        tex.append("\\makeatletter\n");
        tex.append("\\let\\orig@Itemize =\\itemize\n");
        tex.append("\\let\\orig@Enumerate =\\enumerate\n");
        tex.append("\\let\\orig@Description =\\description\n");
        tex.append("\\def\\Nospacing{\\itemsep=0pt\\topsep=0pt\\partopsep=0pt%\n");
        tex.append("\\parskip=0pt\\parsep=0pt}\n\n");
        tex.append("\\def\\noitemsep{\n");
        tex.append("\\renewenvironment{itemize}{\\orig@Itemize\\Nospacing}{\\endlist}\n");
        tex.append("\\renewenvironment{enumerate}{\\orig@Enumerate\\Nospacing}{\\endlist}\n");
        tex.append("\\renewenvironment{description}{\\orig@Description\\Nospacing}%\n");
        tex.append("{\\endlist}\n}\n\n");
        tex.append("\\def\\doitemsep{\n");
        tex.append("\\renewenvironment{itemize}{\\orig@Itemize}{\\endlist}\n");
        tex.append("\\renewenvironment{enumerate}{\\orig@Enumerate}{\\endlist}\n");
        tex.append("\\renewenvironment{description}{\\orig@Description}{\\endlist}\n}\n");
        tex.append("\\makeatother\n\n");

        tex.append("% ---- Bildgrößen ----\n");
        tex.append("\\newlength{\\OneFig}\n");
        tex.append("\\setlength{\\OneFig}{\\textwidth -10mm}\n");
        tex.append("\\newlength{\\TwoFig}\n");
        tex.append("\\setlength{\\TwoFig}{(\\textwidth -10mm) /2}\n");
        tex.append("\\newlength{\\ThreeFig}\n");
        tex.append("\\setlength{\\ThreeFig}{(\\textwidth -10mm) /3}\n");
        tex.append("\\newlength{\\FourFig}\n");
        tex.append("\\setlength{\\FourFig}{(\\textwidth -10mm) /4}\n\n");

        tex.append("% ---- Listings ----\n");
        tex.append("\\usepackage{listings}\n");
        tex.append("\\usepackage{caption}\n");
        tex.append("\\usepackage{csquotes}\n");
        tex.append("\\usepackage{amsmath}\n");
        tex.append("\\usepackage{amssymb}\n");
        tex.append("\\usepackage{float}\n\n");
        tex.append("\\definecolor{dkgreen}{rgb}{0,.6,0}\n");
        tex.append("\\definecolor{dkblue}{rgb}{0,0,.6}\n");
        tex.append("\\definecolor{dkyellow}{cmyk}{0,0,.8,.3}\n");
        tex.append("\\lstset{\n");
        tex.append("\tlanguage=C++,\n");
        tex.append("\tframe=single,\n");
        tex.append("\tnumbers=left,\n");
        tex.append("\tnumberstyle=\\tiny\\color{gray!90!black},\n");
        tex.append("\tbasicstyle=\\ttfamily\\footnotesize,\n");
        tex.append("\ttabsize=4,\n");
        tex.append("\tshowstringspaces=false,\n");
        tex.append("\tcaptionpos=t,\n");
        tex.append("\trulecolor=\\color{lightgray!40},\n");
        tex.append("\tkeywordstyle=\\color{dkblue},\n");
        tex.append("\tstringstyle=\\color{red},\n");
        tex.append("\tcommentstyle=\\color{dkgreen}\n");
        tex.append("}\n");
        tex.append("\\DeclareCaptionFormat{listing}{\t\\par\\vskip1pt#1#2#3}\n");
        tex.append("\\captionsetup[lstlisting]\n{\n");
        tex.append("\tformat=listing,\n");
        tex.append("\tsinglelinecheck=false, \n");
        tex.append("\tmargin=1em, \t\n");
        tex.append("\tlabelsep=colon,\n");
        tex.append("\tjustification=centering\n}\n\n");

        tex.append("\\usepackage{hyperref}\n\n");

        tex.append("\\begin{document}\n\n");

        if (metadata != null) {
            tex.append(buildTitlePage(metadata));
            tex.append("\\newpage\n\n");
            tex.append("\\thispagestyle{empty}\n");
            tex.append("\\tableofcontents\n");
            tex.append("\\newpage\n\n");
            tex.append("\\setcounter{page}{1}\n\n");
        }

        tex.append(body);

        tex.append("\\end{document}\n");

        return tex.toString();
    }

    private String buildTitlePage(LatexExportDialog.LatexMetadata m) {

        StringBuilder sb = new StringBuilder();

        sb.append("\\begin{titlepage}\n");
        sb.append("\\vspace{-0.5cm}\n");
        sb.append("\\hspace{-2.0cm}\n");
        sb.append("\\begin{tabular}{p{8.0cm} p{8.0cm}}\n");
        sb.append("  \\includegraphics[width = 6.0cm]{img/hsel-allgemein} &\n");
        sb.append("   \\parbox[b]{8.0cm}{\n");
        sb.append("     {\\large \t").append(escapeLatex(m.fachbereich())).append(" }\\\\\n");
        sb.append("     {\\large \t").append(escapeLatex(m.abteilung())).append(" }     \n");
        sb.append("    } \\\\\n");
        sb.append("   \\\\\n");
        sb.append("   \\hline\n");
        sb.append("\\end{tabular}\n");
        sb.append("%\n");
        sb.append("\\begin{center}\n");
        sb.append("\\vspace{2.5cm}%\n");
        sb.append("\\LARGE{\\textsc{ %\n");
        sb.append(escapeLatex(m.titel())).append("}}\\\\\n");
        sb.append("\\vspace{2.5cm}%\n");
        sb.append("\\LARGE{\\textsc{%\n");
        sb.append("{").append(escapeLatex(m.artDerArbeit())).append("} % ODER: Projektarbeit\n");
        sb.append("}}\\\\\n");
        sb.append("\\vspace{2cm}%\n");
        sb.append("\\large\n");
        sb.append("Vorgelegt von\\\\\n");

        for (String autor : m.autoren()) {
            sb.append(escapeLatex(autor)).append(" \\\\\n");
        }

        sb.append("\\vspace{1cm} \n");
        sb.append(escapeLatex(m.ort())).append(", \\today \\\\\n");
        sb.append("\\vspace{2.5cm}%\n");
        sb.append("Betreut von\\\\ ");

        for (int i = 0; i < m.betreuer().size(); i++) {
            sb.append(escapeLatex(m.betreuer().get(i)));
            if (i < m.betreuer().size() - 1) {
                sb.append(" \\\\ ");
            }
        }

        sb.append("\n\\end{center}\n");
        sb.append("\\normalsize\n");
        sb.append("\\end{titlepage}\n\n");

        return sb.toString();
    }

    private String escapeLatex(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\textbackslash{}")
                .replace("&", "\\&")
                .replace("%", "\\%")
                .replace("$", "\\$")
                .replace("#", "\\#")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("~", "\\textasciitilde{}")
                .replace("^", "\\textasciicircum{}");
    }

    private SaveStatus saveDialog() throws Exception {
        CFileChooser fileChooser = new CFileChooser("Datei Speichern",
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"),
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json")
        );

        if (!outlineController.getHeaderTitle().isEmpty()) {
            fileChooser.setSelectedFile(new File(outlineController.getHeaderTitle()));
        }

        int result = fileChooser.getSaveResult(mainWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile() != null) {
                if (!fileChooser.getFile().getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".json");
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                    return SaveStatus.SAVED;
                } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                    return SaveStatus.SAVED;
                }else if (!fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".xml");
                    Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
                    return SaveStatus.SAVED;
                } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                    Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
                    return SaveStatus.SAVED;
                }else{
                    JOptionPane.showMessageDialog(mainWindow, "Bitte einen gültigen Pfad angeben.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return SaveStatus.SAVE_FAILED;
                }
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(mainWindow, "Fehler beim Speichern der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
            return SaveStatus.SAVE_FAILED;
        }
        return SaveStatus.SAVE_FAILED;
    }

    private int openDialog() {
        CFileChooser fileChooser = new CFileChooser("Datei Öffnen",
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"),
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json")
                );

        int result = fileChooser.getOpenResult(mainWindow);

        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile().getName().toLowerCase().endsWith(".xml")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return 1;
            } else if (fileChooser.getFile().getName().toLowerCase().endsWith(".json")) {
                saveLocation = new File(fileChooser.getFile().getAbsolutePath());
                return 2;
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(mainWindow, "Fehler beim Öffnen der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        return -1;
    }

    private void importJson(File file) throws Exception {
        JSONParser jsonParser = new JSONParser(outlineController);
        jsonParser.importJson(file);
    }

    private String toJsonString(DefaultTreeModel treeModel) {
        JSONParser jsonParser = new JSONParser(outlineController);
        return jsonParser.toJsonString(treeModel);
    }


    private void importXML(File file) throws Exception {
        XMLParser xmlParser = new XMLParser(outlineController);
        xmlParser.importXML(file);
    }

    private String toXMLString(DefaultTreeModel treeModel) throws Exception {
        XMLParser xmlParser = new XMLParser(outlineController);
        return xmlParser.toXMLString(treeModel);
    }

    private String toLatexString(OutlineNode node) {
        StringBuilder latexStr = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            latexStr.append(getNodes((OutlineNode) node.getChildAt(i)));
        }
        return latexStr.toString();
    }

    private String getNodes(OutlineNode node) {
        StringBuilder sb = new StringBuilder();
        if (node.getLevel() == 1)
            sb.append("\\section{").append(node.getTitle()).append("}\n");
        else if (node.getLevel() == 2)
            sb.append("\\subsection{").append(node.getTitle()).append("}\n");
        else
            sb.append("\\subsubsection{").append(node.getTitle()).append("}\n");

        if (!node.getContent().isEmpty() && !node.getContent().isBlank())
            sb.append(node.getContent()).append("\n");
        sb.append("\n");

        for (int i = 0; i < node.getChildCount(); i++) {
            sb.append(getNodes((OutlineNode) node.getChildAt(i)));
        }
        return sb.toString();
    }

    private void updateLastSavedTime() throws IOException {
        lastSavedTime = Files.getLastModifiedTime(saveLocation.toPath());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(lastSavedTime.toInstant(), ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        mainWindow.getStatusBar().setText("Last Saved: " + localDateTime.format(dateTimeFormatter));
    }

    public SaveStatus checkSavedStatus() {
        if (outlineController.isDirty()) {
            int result = JOptionPane.showConfirmDialog(
                    mainWindow,
                    "Änderungen speichern?",
                    "Unsaved changes",
                    JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                try {
                    return saveDocument();
                } catch (Exception ignored) {
                    return SaveStatus.SAVE_FAILED;
                }
            } else if (result == JOptionPane.NO_OPTION) {
                return SaveStatus.DONT_SAVE;
            } else {
                return SaveStatus.SAVE_FAILED;
            }
        }
        return SaveStatus.NO_CHANGES;
    }

    public void saveLastPath() {
        if (saveLocation != null) {
            AppPreferences.setLastFile(saveLocation.toString());
        }
    }

    public void tryImportLastEditedFile() {
        String lastPath = AppPreferences.getLastFile();

        if (!lastPath.isEmpty()) {
            if (lastPath.endsWith("xml")) {
                try {
                    importXML(new File(lastPath));
                    saveLocation = new File(lastPath);
                } catch (Exception ignored) {}
            } else if (lastPath.endsWith("json")) {
                try {
                    importJson(new File(lastPath));
                    new File(lastPath);
                } catch (Exception ignored) {}
            }
        }
    }
}

class CFileChooser extends JFileChooser {

    CFileChooser(String dialogTitle, FileNameExtensionFilter... filters) {
        this.setDialogTitle(dialogTitle);
        for (FileNameExtensionFilter filter : filters)
            this.addChoosableFileFilter(filter);
        this.setAcceptAllFileFilterUsed(false);
    }

    public int getSaveResult(JFrame mainFrame) {
        return this.showSaveDialog(mainFrame);
    }

    public int getOpenResult(JFrame mainFrame) {return this.showOpenDialog(mainFrame); }

    public File getFile(){
        return this.getSelectedFile();
    }


}


