/*

KI generiert

 */
package model;

/**
 * Die möglichen Antworten auf die Export-Modus-Abfrage in
 * {@code view.LatexExportDialog#askMode}.
 *
 * <p>Hieß ursprünglich {@code LatexExportDialog.Mode}; beim Verschieben ins
 * {@code model}-Paket wurde der Name zu {@code LatexExportMode} präzisiert, da
 * ein bloßes {@code Mode} als Top-Level-Typ zu generisch wäre und mit anderen
 * Modus-Konzepten kollidieren könnte.</p>
 */
public enum LatexExportMode {

    /** Export mit Standard-Vorbelegung ohne weitere Rückfrage. */
    STANDARD,

    /** Export mit benutzerdefinierten Titelseiten-Angaben über {@code askMetadata}. */
    EXPERT,

    /** Der Export wurde abgebrochen. */
    CANCELLED
}