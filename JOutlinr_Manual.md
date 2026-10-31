# JOutlinr -- Benutzerhandbuch

> **Version:** 1.0\
> **Dokumenttyp:** Benutzerhandbuch\
> **Sprache:** Deutsch

## Inhaltsverzeichnis

-   [1. Über JOutlinr](#1-über-joutlinr)
-   [2. Das Hauptfenster](#2-das-hauptfenster)
-   [3. Mit Gliederungen arbeiten](#3-mit-gliederungen-arbeiten)
-   [4. Menübefehle](#4-menübefehle)
    -   [Datei](#datei)
    -   [Bearbeiten](#bearbeiten)
    -   [Gliederung](#gliederung)
    -   [Ansicht](#ansicht)
    -   [Einstellungen](#einstellungen)
    -   [Hilfe](#hilfe)
-   [5. Tastaturkürzel](#5-tastaturkürzel)
-   [6. Zusatzspalten](#6-zusatzspalten)
-   [7. Textbereich und Statusanzeige](#7-textbereich-und-statusanzeige)
-   [8. Dateien und Speichern](#8-dateien-und-speichern)

## 1. Über JOutlinr

JOutlinr ist eine Desktop-Anwendung zur Bearbeitung hierarchischer
Gliederungen. Inhalte werden als Baum aus Einträgen organisiert.
Einträge können auf unterschiedlichen Ebenen angeordnet und ein- oder
ausgeklappt werden. Zusätzlich lassen sich Spalten verwalten; ein
separater Textbereich dient zur Bearbeitung ergänzender Inhalte zum
ausgewählten Eintrag.

Dieses Handbuch beschreibt die Bedienoberfläche und die Befehle, die
sich aus dem bereitgestellten Quellcode nachvollziehen lassen. Die
konkrete Beschriftung von Toolbar-Schaltflächen sowie einige Details zu
Dateiformaten und Dialogen hängen von weiteren Klassen ab und werden
deshalb hier nicht vorausgesetzt.

## 2. Das Hauptfenster

Das Hauptfenster besteht aus folgenden Bereichen:

  -----------------------------------------------------------------------
  Bereich                             Beschreibung
  ----------------------------------- -----------------------------------
  Menüleiste                          Enthält Datei-, Bearbeitungs-,
                                      Gliederungs- und Ansichtsbefehle
                                      sowie Einstellungen und Hilfe.

  Toolbar                             Bietet Schaltflächen für neue
                                      Wurzel-, normale und untergeordnete
                                      Einträge, Löschen, Ein-/Ausrücken,
                                      Auf-/Zuklappen und neue Spalten.

  Gliederungsbereich                  Zeigt Einträge hierarchisch in
                                      einer Baumansicht.

  Spaltenbereich                      Zeigt zusätzliche Spaltenwerte
                                      zeilenweise passend zu den
                                      sichtbaren Gliederungseinträgen.

  Textbereich (`TextWindow`)          Separater Bereich für den Inhalt,
                                      der mit dem ausgewählten Eintrag
                                      verbunden ist.

  Statusleiste                        Zeigt beim Start den Status
                                      „Bereit" an.
  -----------------------------------------------------------------------

Das Fenster startet mit einer Größe von 950 × 640 Pixeln. Die
Mindestgröße beträgt 600 × 400 Pixel. Der obere Gliederungsbereich und
der darunterliegende Textbereich sind durch einen verschiebbaren Trenner
voneinander getrennt. Der Trenner ist beim Start ungefähr bei 60 % der
verfügbaren Höhe positioniert.

### 2.1 Gliederungsbereich

Die Gliederung wird als Baum dargestellt. Einträge können eingerückt
sein, um ihre Eltern-Kind-Beziehung anzuzeigen. Die Wurzel des internen
Baummodells ist in der Benutzeroberfläche ausgeblendet; dadurch steht
der eigentliche Inhalt im Vordergrund.

Einträge lassen sich direkt im Baum bearbeiten. Laut Implementierung
wird die Bearbeitung durch einen Doppelklick gestartet. Die Eingabe wird
außerdem beim Wechsel zu Menüaktionen beendet, wenn gerade ein Eintrag
bearbeitet wird.

### 2.2 Auf- und Zuklappen

Einträge mit untergeordneten Einträgen können aufgeklappt oder
zugeklappt werden. Dadurch ändert sich, welche Zeilen sichtbar sind. Die
Zusatzspalten werden anschließend an die aktuell sichtbaren Baumzeilen
angepasst.

### 2.3 Werkzeugleiste

Die Werkzeugleiste befindet sich im Hauptfenster und bleibt an ihrer
Position fixiert. Sie ist nicht abtrennbar. Jede Schaltfläche löst direkt
die zugehörige Gliederungsaktion aus.

| Schaltfläche | Funktion |
|---|---|
| **Root Eintrag** | Erstellt einen neuen Wurzeleintrag. |
| **Eintrag** | Erstellt einen neuen normalen Gliederungseintrag. |
| **Untereintrag** | Erstellt einen untergeordneten Eintrag. |
| **Löschen** | Löscht den ausgewählten Eintrag. |
| **Promote** | Rückt den Eintrag hierarchisch nach außen. |
| **Demote** | Rückt den Eintrag hierarchisch nach innen. |
| **Aufklappen** | Klappt alle Einträge auf. |
| **Zuklappen** | Klappt alle Einträge zu. |
| **Neue Spalte** | Fügt eine Zusatzspalte hinzu. |

Die Schaltflächen zeigen ein Icon links neben dem Text. Die Icons werden
aus den Ressourcen unter `/icons/` geladen und auf 30 × 30 Pixel
skaliert. Falls eine Icon-Datei fehlt oder nicht geladen werden kann,
wird die Schaltfläche trotzdem mit ihrer Textbeschriftung angelegt.

### 2.4 Zeilenhervorhebung

Wenn sich der Mauszeiger über einer Baumzeile befindet, wird der
Hover-Zustand an die zugehörigen Spaltenzellen weitergegeben. Dadurch
kann die Zeile über den Baum und den Spaltenbereich hinweg gemeinsam
hervorgehoben werden. Beim Verlassen des Baums wird die Hervorhebung
zurückgesetzt.

## 3. Mit Gliederungen arbeiten

### 3.1 Einträge auswählen und umbenennen

Wählen Sie einen Eintrag im Baum aus, um mit ihm zu arbeiten. Zum
Umbenennen kann der Eintrag doppelt angeklickt werden. Alternativ
startet bei fokussiertem Baum die Tastenkombination `Strg/Cmd + Enter`
die Umbenennung; die genaue Taste für den Menü-Shortcut hängt vom
Betriebssystem ab.

### 3.2 Hierarchie ändern

Die Menüs und Tastaturbindungen enthalten Befehle zum Ein- und Ausrücken
von Einträgen. Einrücken ordnet einen Eintrag hierarchisch tiefer ein;
Ausrücken hebt ihn auf eine höhere Ebene. Die konkreten Regeln,
insbesondere das Verhalten bei Geschwistereinträgen, richten sich nach
der jeweiligen Aktion im Programm.

### 3.3 Einträge verschieben

Die Anwendung unterstützt Drag-and-drop im Baum. Einträge können an eine
andere Position oder zwischen andere Einträge verschoben werden. Beim
Verschieben wird der Baum aktualisiert und der Spaltenbereich mit den
sichtbaren Zeilen synchronisiert.

## 4. Menübefehle

Die folgenden Abschnitte dokumentieren die Menüstruktur, wie sie in der
Klasse `Menubar` aufgebaut ist. Nicht alle Einträge haben im
vorliegenden Code ein fest zugewiesenes Tastaturkürzel.

### Datei

Das Menü **Datei** bündelt die Dateioperationen.

  -----------------------------------------------------------------------
  Befehl                              Funktion
  ----------------------------------- -----------------------------------
  Neu                                 Startet die Aktion zum Erstellen
                                      einer neuen Datei bzw. eines neuen
                                      Dokuments.

  Öffnen                              Startet das Öffnen einer
                                      vorhandenen Datei.

  Speichern                           Startet das Speichern des aktuellen
                                      Dokuments.

  Speichern unter                     Startet das Speichern unter einem
                                      anderen Namen oder Pfad.

  Als LaTeX exportieren               Startet den LaTeX-Export.

  Schließen                           Prüft vor dem Schließen den
                                      Speicherstatus. Bei erfolgreicher
                                      bzw. nicht fehlgeschlagener
                                      Speicherprüfung wird der zuletzt
                                      verwendete Pfad gesichert und das
                                      Fenster geschlossen.
  -----------------------------------------------------------------------

Die genaue Dateiauswahl, die unterstützten Importformate und die
Exportdetails werden durch `FileActions` und `FileController` bestimmt.

### Bearbeiten

Das Menü **Bearbeiten** enthält allgemeine Bearbeitungsbefehle sowie die
Verwaltung zusätzlicher Spalten.

  -----------------------------------------------------------------------
  Befehl                              Funktion
  ----------------------------------- -----------------------------------
  Rückgängig                          Macht die letzte unterstützte
                                      Änderung rückgängig.

  Wiederholen                         Stellt eine zuvor rückgängig
                                      gemachte Änderung wieder her.

  Spalte hinzufügen                   Fügt eine neue Spalte hinzu.

  Ausschneiden                        Führt die Ausschneiden-Aktion für
                                      die aktuelle Auswahl aus.

  Kopieren                            Führt die Kopieren-Aktion für die
                                      aktuelle Auswahl aus.

  Einfügen                            Führt die Einfügen-Aktion aus.
  -----------------------------------------------------------------------

„Spalte hinzufügen" ist im Menü mit einem Tastaturkürzel belegt, das die
Menü-Shortcut-Taste und `Umschalt + N` kombiniert. Die konkrete Taste
für den Menü-Shortcut hängt vom Betriebssystem ab.

### Gliederung

Das Menü **Gliederung** enthält Befehle zur Struktur der Baumansicht.

  -----------------------------------------------------------------------
  Befehl                              Funktion
  ----------------------------------- -----------------------------------
  Neuer Eintrag                       Erstellt einen neuen
                                      Gliederungseintrag.

  Neuer Untereintrag                  Erstellt einen untergeordneten
                                      Eintrag.

  Eintrag löschen                     Löscht den ausgewählten Eintrag.

  Eintrag ausrücken                   Verschiebt den Eintrag in der
                                      Hierarchie nach außen.

  Eintrag einrücken                   Verschiebt den Eintrag in der
                                      Hierarchie nach innen.
  -----------------------------------------------------------------------

Die genaue Wirkung und die Voraussetzungen der einzelnen Aktionen werden
durch `OutlineActions` und `OutlineController` bestimmt.

### Ansicht

Das Menü **Ansicht** enthält Befehle zum Auf- und Zuklappen von
Einträgen.

  -----------------------------------------------------------------------
  Befehl                              Status im bereitgestellten Code
  ----------------------------------- -----------------------------------
  Eintrag aufklappen                  Der Menüeintrag wird erstellt und
                                      der Aktion zugeordnet.

  Eintrag zuklappen                   Der Eintrag wird erstellt, ist im
                                      Menü aber derzeit auskommentiert
                                      und daher nicht sichtbar.

  Alle aufklappen                     Der Befehl wird dem Menü
                                      hinzugefügt.

  Alle zuklappen                      Der Eintrag wird erstellt, ist im
                                      Menü aber derzeit auskommentiert
                                      und daher nicht sichtbar.
  -----------------------------------------------------------------------

Die Tastaturbindung `Enter` klappt den ausgewählten Eintrag auf oder zu.
Die Kombination aus Menü-Shortcut und `Umschalt + Enter` klappt alle
Einträge auf, wenn derzeit keiner aufgeklappt ist, oder klappt alle zu,
sobald mindestens ein Eintrag aufgeklappt ist.

### Einstellungen

Der Menüpunkt **Einstellungen...** öffnet einen modalen Dialog mit den
Tabs **Allgemein** und **Farben**. Die Änderungen werden sofort
angewendet; einen separaten „Übernehmen“-Button gibt es nicht. Die
Einstellungen werden gespeichert und bleiben laut Implementierung über
Programmstarts hinweg erhalten. Der Dialog kann über **Schließen**
geschlossen werden.

Der Menüpunkt besitzt das Tastaturkürzel `Menü-Shortcut + Komma`.

#### Tab „Allgemein“

Im Abschnitt **Darstellung** stehen folgende Optionen zur Verfügung:

- **Dark Mode:** Schaltet das dunkle Erscheinungsbild ein oder aus.
- **Vollbild:** Maximiert das Hauptfenster oder stellt es auf den
  normalen Fensterzustand zurück.

Im Abschnitt **Verhalten** befinden sich diese Optionen:

- **Letzten Eintrag nach Löschvorgang auswählen:** Legt fest, ob nach
  dem Löschen der letzte Eintrag ausgewählt werden soll.
- **Icon anzeigen, wenn Eintrag Text enthält:** Aktiviert oder
  deaktiviert die entsprechende Icon-Anzeige im Baum. Nach der Änderung
  wird die Baumansicht aktualisiert.

#### Tab „Farben“

Der Tab **Farben** enthält drei anpassbare Akzentfarben:

- **Akzentfarbe**
- **Akzent (dunkel)**
- **Akzent (hell)**

Für jede Farbe werden eine Farbvorschau und der Hex-Code im Format
`#RRGGBB` angezeigt. Klicken Sie auf die Farbvorschau, um den
Farbauswahldialog zu öffnen. Eine gewählte Farbe wird sofort auf die
Anwendung angewendet.

Mit **Auf Standard zurücksetzen** lassen sich die Akzentfarben auf die
Standardwerte zurücksetzen.

### Hilfe

Das Menü **Hilfe** enthält den Eintrag **Über...**. Dieser öffnet einen
Informationsdialog mit dem Programmnamen, dem Hinweis auf Java Swing und
einer Auflistung zentraler Klassen.

Der Hilfe-Bereich ist im Quellcode als noch nicht vollständig umgesetzt
gekennzeichnet.

## 5. Tastaturkürzel

Die Anwendung registriert die folgenden Tastaturbindungen direkt am
Baum. `Menü-Shortcut` bezeichnet die plattformabhängige
Modifikatortaste, die Java über `getMenuShortcutKeyMaskEx()` ermittelt.
Auf macOS ist dies normalerweise `⌘`; auf Windows und vielen
Linux-Desktops normalerweise `Strg`.

  ------------------------------------------------------------------------------------
  Tastenkombination                    Funktion                Hinweis
  ------------------------------------ ----------------------- -----------------------
  `Menü-Shortcut + ←`                  Eintrag ausrücken       Am fokussierten Baum
                                                               registriert.

  `Menü-Shortcut + →`                  Eintrag einrücken       Am fokussierten Baum
                                                               registriert.

  `Enter`                              Ausgewählten Eintrag    Wenn kein Eintrag
                                       auf- oder zuklappen     ausgewählt ist,
                                                               geschieht nichts.

  `Menü-Shortcut + Umschalt + Enter`   Alle Einträge auf- oder Klappt alle auf, wenn
                                       zuklappen               keiner aufgeklappt ist;
                                                               andernfalls alle zu.

  `Menü-Shortcut + X`                  Ausgewählten Eintrag    Am fokussierten Baum
                                       ausschneiden            registriert.

  `Menü-Shortcut + C`                  Ausgewählten Eintrag    Am fokussierten Baum
                                       kopieren                registriert.

  `Menü-Shortcut + V`                  Eintrag einfügen        Am fokussierten Baum
                                                               registriert.

  `Rücktaste` (`Backspace`)            Ausgewählten Eintrag    Am fokussierten Baum
                                       löschen                 registriert.

  `Menü-Shortcut + Enter`              Ausgewählten Eintrag    Startet die Bearbeitung
                                       umbenennen              des Eintrags.

  `Menü-Shortcut + Umschalt + N`       Neue Spalte hinzufügen  Im Menü „Bearbeiten"
                                                               definiert.

  `Menü-Shortcut + Komma`              Einstellungen öffnen    Im Menü „Einstellungen"
                                                               definiert.
  ------------------------------------------------------------------------------------

## 6. Zusatzspalten

Zusatzspalten zeigen Werte neben den Einträgen der Gliederung an. Der
Spaltenkopf wird im `HeaderPanel` dargestellt; die zugehörigen Zellen
befinden sich im `ColumnPanel`. Die Spaltenverwaltung wird über einen
`ColumnController` abgewickelt.

### 6.1 Spalte hinzufügen

Verwenden Sie **Bearbeiten → Spalte hinzufügen**, um eine neue Spalte
anzulegen. Nach dem Hinzufügen wird der Spaltenbereich eingeblendet,
falls er zuvor nicht sichtbar war. Anschließend werden die Zellen an die
sichtbaren Einträge im Baum angepasst.

### 6.2 Zeilensynchronisierung

Die Zahl und Reihenfolge der sichtbaren Baumzeilen bestimmt, welche
Zeilen im Spaltenbereich angezeigt werden. Wenn Einträge hinzugefügt,
geändert, entfernt, verschoben oder auf- bzw. zugeklappt werden, wird
die Spaltenansicht aktualisiert.

Beim endgültigen Löschen eines Eintrags werden die zugehörigen
Spaltenwerte über die dafür vorgesehenen Controller-Operationen
bereinigt. Ein bloßes Entfernen während eines Verschiebevorgangs darf
die Werte nicht löschen.

### 6.3 Spaltenüberschriften und Zellansichten

Die Spaltenüberschriften werden im nicht mitscrollenden Kopfbereich
angezeigt. Die Zellen selbst scrollen gemeinsam mit dem
Gliederungsinhalt. Die konkreten Zelltypen und deren
Editiermöglichkeiten hängen vom jeweiligen `ColumnType` und den
zugehörigen View-Klassen ab.

## 7. Textbereich und Statusanzeige

### 7.1 Textbereich

Der Bereich `TextWindow` befindet sich unterhalb des Gliederungs- und
Spaltenbereichs. Er ist über einen horizontalen Trenner abgeteilt,
dessen Position verändert werden kann.

Wenn sich der Inhalt des Textbereichs ändert, benachrichtigt er die
Anwendung. Ist ein Gliederungseintrag ausgewählt, wird der entsprechende
Knoten im Baummodell als geändert markiert.

### 7.2 Statusleiste

Am unteren Fensterrand befindet sich eine Statusleiste. Beim Start zeigt
sie **Bereit** an. Weitere Statusmeldungen werden angezeigt.

## 8. Dateien und Speichern

Beim Start versucht die Anwendung, die zuletzt bearbeitete Datei erneut
zu importieren. Beim Schließen des Fensters wird zunächst der
Speicherstatus geprüft. Wenn die Prüfung nicht `SAVE_FAILED` zurückgibt,
wird der zuletzt verwendete Pfad gespeichert und das Fenster
geschlossen.

Der Quellcode zeigt außerdem einen Aufruf, der den aktuellen Zustand
nach dem initialen Import als gespeichert markiert. Die genaue
Behandlung von Speicherfehlern, Dateiformaten und Rückfragen hängt von
`FileController`, `FileActions` und den zugehörigen Dialogen ab.