package controller;

import model.JSONParser;
import model.OutlineNode;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import view.LatexExportDialog;
import view.MainWindow;
import view.columns.ColumnHeader;
import view.columns.OutlineColumn;

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

            OutlineNode root = outlineController.getRootNode();

            root.removeAllChildren();
            root.setTitle("Outliner");
            root.setContent("");

            treeModel.reload();

            saveLocation = null;

            outlineController.saved();

            mainWindow.getStatusBar().setText(" Bereit");
        }
    }

    public void openDocument() {
        if (checkSavedStatus() != SaveStatus.SAVE_FAILED) {
            int result = openDialog();
            if (result == 1) {

                try {
                    importXML();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            } else if (result == 2) {
                try {
                    fromJson();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            outlineController.saved();
        }
    }

    public void saveDocument() throws Exception {
        /*System.out.println(outlineController.getHeaderTitle());
        for (ColumnHeader c :outlineController.getSpaltenNamen()) {
            System.out.println(c.getTitle());
        }
        for (OutlineColumn c : outlineController.getSpalten()) {
            System.out.println(c.getType());
        }*/


        if (saveLocation != null) {
            if (saveLocation.getName().toLowerCase().endsWith("xml")) {
                Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
            } else if (saveLocation.getName().toLowerCase().endsWith(".json")) {
                Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
            }
        } else {
            saveDialog();
        }
        if(saveLocation != null)
            updateLastSavedTime();
        outlineController.saved();
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

    private void saveDialog() throws Exception {
        CFileChooser fileChooser = new CFileChooser("Datei Speichern",
                new FileNameExtensionFilter("XML-Dateien (*.xml)", "xml"),
                new FileNameExtensionFilter("JSON-Dateien (*.json)", "json")
        );

        int result = fileChooser.getSaveResult(mainWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getFile() != null) {
                if (!fileChooser.getFile().getName().toLowerCase().endsWith(".json") && fileChooser.getFileFilter().getDescription().contains("JSON")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".json");
                    Files.writeString(saveLocation.toPath(), toJsonString(treeModel));
                } else if (!fileChooser.getFile().getName().toLowerCase().endsWith(".xml") && fileChooser.getFileFilter().getDescription().contains("XML")) {
                    saveLocation = new File(fileChooser.getFile().getAbsolutePath() + ".xml");
                    Files.writeString(saveLocation.toPath(), toXMLString(treeModel));
                } else{
                    JOptionPane.showMessageDialog(mainWindow, "Bitte einen gültigen Pfad angeben.", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (result != JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(mainWindow, "Fehler beim Speichern der Datei", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
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

    private static String toJsonString(DefaultTreeModel treeModel) {
        StringBuilder json = new StringBuilder("{\n");
        OutlineNode root = (OutlineNode) treeModel.getRoot();

        json.append(JsonChild(0, root));
        json.append("\n}");

        return json.toString();
    }

    private static StringBuilder JsonChild(int amountParents, OutlineNode node) {
        StringBuilder sb = new StringBuilder();

        // Spaces
        StringBuilder spaces = new StringBuilder("  ");
        spaces.repeat("  ", Math.max(0, amountParents));

        sb.append(spaces).append("\"title\" : \"").append(node.getTitle()).append("\",\n");
        sb.append(spaces).append("\"content\" : \"").append(node.getContent()).append("\",\n");
        sb.append(spaces).append("\"children\" : ").append("[ ");
        if (node.getChildCount() == 0) {
            sb.append("]");
            return sb;
        } else if (node.getChildCount() == 1) {
            sb.append("{\n");
            sb.append(JsonChild(++amountParents,(OutlineNode) node.getChildAt(0)));
            sb.append("\n").append(spaces).append("} ]");
        } else {
            amountParents++;
            for (int i = 0; i < node.getChildCount(); i++) {
                sb.append("{\n");
                if (node.getChildCount() -1 == i) {
                    sb.append(JsonChild(amountParents, (OutlineNode) node.getChildAt(i)));
                    sb.append("\n").append(spaces).append("} ]");
                } else {
                    sb.append(JsonChild(amountParents, (OutlineNode) node.getChildAt(i)));
                    sb.append("\n").append(spaces).append("}, ");
                }
            }
        }
        return sb;
    }

    private void fromJson() throws IOException {
        JSONParser jsonParser = new JSONParser(getJson());

        outlineController.cancelActiveEditing();

        OutlineNode parsedRoot = jsonParser.parse();
        replaceRootContents(parsedRoot);
    }

    private String getJson() throws IOException {
        StringBuilder json = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(saveLocation))) {
            String line = br.readLine();

            while (line != null) {
                json.append(line);
                json.append(System.lineSeparator());
                line = br.readLine();
            }
        }
        return json.toString();
    }

    public String toXMLString(DefaultTreeModel treeModel) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element rootElement = doc.createElement("outline");
        doc.appendChild(rootElement);

        OutlineNode rootNode = (OutlineNode) treeModel.getRoot();
        appendNode(doc, rootElement, rootNode);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    private void appendNode(Document doc, Element parentElement, OutlineNode node) {
        Element nodeElement = doc.createElement("node");
        nodeElement.setAttribute("title", node.getTitle());

        Element contentElement = doc.createElement("content");
        contentElement.appendChild(doc.createTextNode(node.getContent()));
        nodeElement.appendChild(contentElement);

        parentElement.appendChild(nodeElement);

        for (int i = 0; i < node.getChildCount(); i++) {
            OutlineNode child = (OutlineNode) node.getChildAt(i);
            appendNode(doc, nodeElement, child);
        }
    }

    public void importXML() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(saveLocation);

        Element rootElement = doc.getDocumentElement();
        Element rootNodeElement = (Element) rootElement.getElementsByTagName("node").item(0);

        if (rootNodeElement == null) {
            throw new IllegalArgumentException("Keine Root-Node gefunden");
        }

        outlineController.cancelActiveEditing();

        OutlineNode parsedRoot = parseNode(rootNodeElement);
        replaceRootContents(parsedRoot);
    }

    private void replaceRootContents(OutlineNode parsedRoot) {
        OutlineNode actualRoot = outlineController.getRootNode();

        actualRoot.removeAllChildren();
        actualRoot.setTitle(parsedRoot.getTitle());
        actualRoot.setContent(parsedRoot.getContent());

        while (parsedRoot.getChildCount() > 0) {
            OutlineNode child = (OutlineNode) parsedRoot.getChildAt(0);
            actualRoot.add(child);
        }

        treeModel.reload();
    }

    private OutlineNode parseNode(Element nodeElement) {
        String title = nodeElement.getAttribute("title");

        OutlineNode node = new OutlineNode(title);

        NodeList children = nodeElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (!(child instanceof Element element)) {
                continue;
            }

            switch (element.getTagName()) {
                case "content" -> node.setContent(element.getTextContent());

                case "node" -> {
                    OutlineNode childNode = parseNode(element);
                    node.add(childNode);
                }
            }
        }

        return node;
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
                    saveDocument();
                    return SaveStatus.SAVED;
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


