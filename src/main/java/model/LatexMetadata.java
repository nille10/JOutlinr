/*

KI generiert

 */
package model;

import java.util.List;

/**
 * Die vom Benutzer eingegebenen Angaben für die Titelseite des LaTeX-Exports.
 *
 * <p>Reine Daten ohne jeden Bezug zur Darstellung; wird in
 * {@code view.LatexExportDialog#askMetadata} über einen Swing-Dialog befüllt und
 * anschließend vom Export (z. B. einem Controller) ausgelesen.</p>
 *
 * @param fachbereich  der Fachbereich
 * @param abteilung    die Abteilung
 * @param titel        der Titel der Arbeit
 * @param artDerArbeit die Art der Arbeit, z. B. "Bericht" oder "Projektarbeit"
 * @param autoren      die Liste der Autoren, eine pro eingegebener Zeile
 * @param ort          der Ort
 * @param betreuer     die Liste der Betreuer, eine pro eingegebener Zeile
 */
public record LatexMetadata(
        String fachbereich,
        String abteilung,
        String titel,
        String artDerArbeit,   // z.B. "Bericht", "Projektarbeit"
        List<String> autoren,
        String ort,
        List<String> betreuer
) {}