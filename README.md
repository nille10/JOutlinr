# JOutlinr

![JOutlinr Logo](src/main/resources/images/joutliner_logo.png)

**JOutlinr** ist eine schlanke, desktopbasierte Outliner-Anwendung, mit der sich Ideen, Notizen und Gliederungen hierarchisch strukturieren lassen. Die Anwendung eignet sich insbesondere zur Planung und Organisation von Texten und Dokumenten, beispielsweise für wissenschaftliche Arbeiten in LaTeX.

Das Projekt befindet sich derzeit in der **Beta-Phase** und wird aktiv weiterentwickelt.

**Repository:** [github.com/nille10/JOutlinr](https://github.com/nille10/JOutlinr)

![JOutlinr – Programmübersicht](src/main/resources/images/screenshot_overview.png)

## ✨ Funktionen

- **Hierarchische Gliederungen:** Inhalte in einer übersichtlichen Baumstruktur mit bis zu drei Gliederungsebenen organisieren.
- **Flexible Bearbeitung:** Einträge erstellen, umbenennen, löschen und innerhalb der Gliederung verschieben.
- **Drag-and-drop:** Einträge und ganze Teilbäume intuitiv an eine andere Position verschieben.
- **Mehrfachauswahl:** Mehrere Einträge per Strg-Klick auswählen und gemeinsam bearbeiten.
- **Einträge ein- und ausklappen:** Auch umfangreiche Gliederungen übersichtlich halten.
- **Eigene Spalten:** Zusätzliche Spalten mit Kontrollkästchen oder Textfeldern für individuelle Informationen verwenden.
- **Speichern und Laden:** Gliederungen im XML- oder JSON-Format speichern und wieder öffnen.
- **Automatische Wiederherstellung:** Das zuletzt verwendete Dokument wird beim nächsten Start wiederhergestellt.
- **Rückgängig und Wiederholen:** Unterstützte Änderungen an der Struktur lassen sich rückgängig machen und wiederherstellen.
- **LaTeX-Export:** Gliederungen in LaTeX-Code umwandeln – mit Standardmodus und Expertenmodus für zusätzliche Einstellungen, etwa zur Titelseite und Dokumentmetadaten.
- **Anpassbare Oberfläche:** Helles oder dunkles Design sowie verschiedene Akzentfarben auswählen.
- **Tastenkürzel:** Wichtige Aktionen direkt über die Tastatur ausführen.

## 🖥️ Plattformen und Startmöglichkeiten

JOutlinr basiert auf Java und kann daher auf **Windows, Linux und macOS** ausgeführt werden.

Es gibt mehrere Möglichkeiten, die Anwendung zu starten:

### Windows: Direkt starten

Für Windows ist zusätzlich eine ausführbare `.exe` mit einer mitgelieferten Java-Laufzeitumgebung (JRE) vorgesehen. Damit lässt sich JOutlinr direkt starten, ohne Java separat installieren zu müssen.

### Alle Plattformen: JAR-Datei ausführen

Alternativ kann die JAR-Datei mit einer geeigneten Java-Laufzeitumgebung gestartet werden. Dafür muss Java installiert sein.

Beispiel:

```bash
java -jar JOutlinr-1.0-SNAPSHOT.jar
```

Der Dateiname hängt vom jeweiligen Build ab.

### Selbst kompilieren

Wer lieber den Quellcode verwenden oder das Projekt selbst bauen möchte, kann das Repository klonen und JOutlinr mit Maven kompilieren. Diese Möglichkeit steht unabhängig vom Betriebssystem zur Verfügung.

## 🛠️ Voraussetzungen

Für den eigenständigen Build werden benötigt:

- **JDK 21 oder neuer**
- **Apache Maven**
- Git (optional, zum Klonen des Repositorys)

Die Java-Version des Builds ist in der Maven-Konfiguration festgelegt. Für die Ausführung einer fertigen JAR-Datei wird eine dazu kompatible Java-Laufzeitumgebung benötigt.

## 🔧 Selbst bauen

### 1. Repository klonen

```bash
git clone https://github.com/nille10/JOutlinr.git
cd JOutlinr
```

### 2. Projekt kompilieren und paketieren

```bash
mvn clean package
```

Maven lädt die benötigten Abhängigkeiten herunter und erstellt das Anwendungspaket.

### 3. Anwendung starten

Führe die erzeugte JAR-Datei mit Java aus. Je nach Projektkonfiguration und Build-Version kann der genaue Dateiname abweichen.

```bash
java -jar target/JOutlinr-1.0-SNAPSHOT.jar
```

Alternativ kann das Projekt in einer Java-IDE wie Eclipse oder IntelliJ IDEA importiert und von dort gestartet werden.

## 📄 LaTeX-Export

JOutlinr unterstützt den Export strukturierter Gliederungen nach LaTeX. So können Einträge aus der Planung in die eigentliche Dokumentstruktur übernommen werden.

Dabei stehen zwei Modi zur Verfügung:

- **Standardmodus:** Für einen unkomplizierten Export der Gliederung.
- **Expertenmodus:** Für zusätzliche Konfigurationsmöglichkeiten, unter anderem im Zusammenhang mit Titelseite und Dokumentmetadaten.

Der Export soll den Übergang von der Planung zur Ausarbeitung erleichtern und wiederkehrende Strukturarbeit reduzieren.

## 🚧 Projektstatus

JOutlinr befindet sich aktuell in der **Beta-Phase**. Die zentralen Funktionen zur Gliederungsverwaltung, Speicherung und zum LaTeX-Export sind vorhanden; dennoch können sich Bedienung und Funktionsumfang im Laufe der Weiterentwicklung ändern.

Fehlerberichte, Verbesserungsvorschläge und konstruktives Feedback sind willkommen. Bitte nutze dafür die [Issues im GitHub-Repository](https://github.com/nille10/JOutlinr/issues).

## 🧰 Verwendete Technologien

- **Java / Java Swing** – plattformübergreifende Desktop-Anwendung
- **FlatLaf** – modernes Erscheinungsbild für Swing
- **Apache Maven** – Build- und Abhängigkeitsverwaltung
- **MVC-Architektur** – Trennung von Modell, Oberfläche und Steuerungslogik
- **XML und JSON** – Dateiformate für die Speicherung
- **Maven Shade Plugin** – Erstellung eines gebündelten Anwendungspakets

## 📜 Lizenz

JOutlinr wird unter der **MIT-Lizenz** veröffentlicht. Die genauen Lizenzbedingungen findest du in der Datei [`LICENSE`](LICENSE) im Repository.

## 👥 Mitwirkung

JOutlinr ist ein Open-Source-Projekt. Rückmeldungen, Fehlermeldungen und Ideen zur Verbesserung sind willkommen.

- Repository: [https://github.com/nille10/JOutlinr](https://github.com/nille10/JOutlinr)
- Issues und Fehlermeldungen: [GitHub Issues](https://github.com/nille10/JOutlinr/issues)

---

*JOutlinr – Gliederungen erstellen, Gedanken ordnen und den Weg zum fertigen Dokument vereinfachen.*
