package model;

import controller.OutlineController;
import view.LatexExportDialog;
import view.CFileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Der {@code XMLParser} ist für das Exportieren von Outlines im Latex-Format zuständig.
 *
 * <p>
 *   Der Grundaufbau des Dokuments ist \section \subsection \subsubsection für die drei möglichen Ebenen im Outliner.
 * </p>
 */
public class LATEXParser {

    private final OutlineController outlineController;
    private final JFrame mainWindow;
    private final TreeModel treeModel;

    /**
     * Erstellt einen neunen {@code LATEXParser}.
     * @param outlineController der Controller für den Zugriff auf die Outline-Daten
     * @param mainWindow das Hauptfenster der Anwendung
     * @param treeModel das TreeModel des JTrees
     */
    public LATEXParser(OutlineController outlineController, JFrame mainWindow, TreeModel treeModel) {
        this.outlineController = outlineController;
        this.mainWindow = mainWindow;
        this.treeModel = treeModel;
    }

    /**
     * Exportiert den Inhalt des {@link OutlineController} in LATEX-Format
     *
     * <p>
     *   Öffnet einen Dialog um zu ermitteln in welchem Modus das LATEX-Dokument generiert werden soll (siehe {@link LatexExportDialog}.
     *   Abhängig vom gewählten Modus wird das generierte LATEX-Dokument an dem ausgewählten Ort (siehe {@link #exportAsLatexDialog()}) gespeichert.
     * </p>
     *
     * @throws IOException wenn beim Schreiben auf der Festplatte ein fehler auftrit
     */
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

    /**
     * Erstellt einen String bestehend aus den Knoten des Outliners (siehe {@link #getNodes(OutlineNode)}).
     *
     * @param node der Wurzelknoten der zu parsenden Elemente
     * @return einen String nach dem oben beschriebenen Muster
     */
    private String toLatexString(OutlineNode node) {
        StringBuilder latexStr = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            latexStr.append(getNodes((OutlineNode) node.getChildAt(i)));
        }
        return latexStr.toString();
    }

    /**
     * Erstellt aus einem Knoten einen String im LATEX-Format.
     * <p>
     * Je nach Ebene des Knotens wird dieser als \section, \subsection oder \subsubsection dargestellt.
     * Der Titel des Knotens bildet dabei den Titel der Section und der Inhalt des Textfeldes bildet den Inhalt der Section.
     * </p>
     *
     * @param node Knoten der geparst werden soll
     * @return Knoten als LATEX-String im oben beschriebenen Format
     */
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

    /**
     * Öffnet einen Dialog, bei dem der Pfad zum Speichern des LATEX-Dokuments ermittelt wird.
     *
     * @return Ort an dem das Dokument gespeichert werden soll
     */
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

    /**
     * Erstellt das komplette LATEX-Dokument, abhängig von der Wahl des Benutzers
     * (siehe {@link #buildStandardLatexDocument(String)} und {@link #buildExpertLatexDocument(LatexExportDialog.LatexMetadata, String)}).
     *
     * @param metadata metadata aus dem {@link #exportAsLatexDialog()}, wenn metadata {@code null} ist, wird das Dokument im Standard Modus erzeugt.
     * @param body die Knoten im LATEX-Format
     *
     * @return den fertigen String im gewählten Modus
     */
    private String buildFullLatexDocument(LatexExportDialog.LatexMetadata metadata, String body) {

        if (metadata == null) {
            return buildStandardLatexDocument(body);
        }

        return buildExpertLatexDocument(metadata, body);
    }

    /**
     * Bau eines Standard-LATEX-Dokuments
     *
     * @param body Knoten als LATEX-String
     * @return String des fertigen Standard-Dokuments
     */
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

    /**
     * Erzeugt ein komplettes LATEX-Dokument mit Informationen wie Fachbereich, Abteilung, Autor, ... (siehe {@link LatexExportDialog}
     *
     * @param metadata allgemeine Informationen für das LATEX-Dokument
     * @param body die Knoten in LATEX-Form (siehe {@link #getNodes(OutlineNode)}
     * @return einen String mit einem kompletten LATEX-Dokument
     */
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

    /**
     * Erzeugt die Titelseite eines LATEX-Dokuments
     * @param metadata metadaten für das LATEX-Dokument
     * @return fertige Titelseite
     */
    private String buildTitlePage(LatexExportDialog.LatexMetadata metadata) {

        StringBuilder sb = new StringBuilder();

        sb.append("\\begin{titlepage}\n");
        sb.append("\\vspace{-0.5cm}\n");
        sb.append("\\hspace{-2.0cm}\n");
        sb.append("\\begin{tabular}{p{8.0cm} p{8.0cm}}\n");
        sb.append("  \\includegraphics[width = 6.0cm]{img/hsel-allgemein} &\n");
        sb.append("   \\parbox[b]{8.0cm}{\n");
        sb.append("     {\\large \t").append(escapeLatex(metadata.fachbereich())).append(" }\\\\\n");
        sb.append("     {\\large \t").append(escapeLatex(metadata.abteilung())).append(" }     \n");
        sb.append("    } \\\\\n");
        sb.append("   \\\\\n");
        sb.append("   \\hline\n");
        sb.append("\\end{tabular}\n");
        sb.append("%\n");
        sb.append("\\begin{center}\n");
        sb.append("\\vspace{2.5cm}%\n");
        sb.append("\\LARGE{\\textsc{ %\n");
        sb.append(escapeLatex(metadata.titel())).append("}}\\\\\n");
        sb.append("\\vspace{2.5cm}%\n");
        sb.append("\\LARGE{\\textsc{%\n");
        sb.append("{").append(escapeLatex(metadata.artDerArbeit())).append("} % ODER: Projektarbeit\n");
        sb.append("}}\\\\\n");
        sb.append("\\vspace{2cm}%\n");
        sb.append("\\large\n");
        sb.append("Vorgelegt von\\\\\n");

        for (String autor : metadata.autoren()) {
            sb.append(escapeLatex(autor)).append(" \\\\\n");
        }

        sb.append("\\vspace{1cm} \n");
        sb.append(escapeLatex(metadata.ort())).append(", \\today \\\\\n");
        sb.append("\\vspace{2.5cm}%\n");
        sb.append("Betreut von\\\\ ");

        for (int i = 0; i < metadata.betreuer().size(); i++) {
            sb.append(escapeLatex(metadata.betreuer().get(i)));
            if (i < metadata.betreuer().size() - 1) {
                sb.append(" \\\\ ");
            }
        }

        sb.append("\n\\end{center}\n");
        sb.append("\\normalsize\n");
        sb.append("\\end{titlepage}\n\n");

        return sb.toString();
    }

    /**
     * Ersetzt Textelemente die im LATEX-Dokument anders dargestellt werden müssen.
     *
     * @param input Text der in LATEX Form angepasst werden soll
     * @return angepasster Text
     */
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
}
