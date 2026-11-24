
package fr.diginamic.hello.services;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.dto.VilleDto;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private static final BaseColor NAVY =
            new BaseColor(23, 39, 64);

    private static final BaseColor TEAL =
            new BaseColor(0, 151, 157);

    private static final BaseColor SLATE =
            new BaseColor(91, 107, 125);

    private static final BaseColor LIGHT =
            new BaseColor(242, 247, 249);

    private static final BaseColor WHITE =
            BaseColor.WHITE;

    private static final BaseColor BORDER =
            new BaseColor(218, 228, 232);

    private static final Locale FRENCH = Locale.FRANCE;

    private final VilleService villeService;
    private final DepartementService departementService;

    public ExportService(
            VilleService villeService,
            DepartementService departementService) {

        this.villeService = villeService;
        this.departementService = departementService;
    }

    /**
     * Exports cities above a population threshold as UTF-8 CSV.
     *
     * @param min exclusive minimum population
     * @return CSV file content
     */
    public byte[] exporterVillesCsv(int min) {

        List<VilleDto> villes =
                villeService.rechercherParPopulation(min);

        Map<String, DepartementDto> departements =
                departementService.getDepartements()
                        .stream()
                        .filter(d -> d.getCode() != null)
                        .collect(Collectors.toMap(
                                d -> d.getCode().toUpperCase(Locale.ROOT),
                                Function.identity(),
                                (first, second) -> first
                        ));

        StringBuilder csv = new StringBuilder();

        // UTF-8 BOM improves compatibility with Excel.
        csv.append('\uFEFF');
        csv.append(
                "Nom de la ville;Nombre d'habitants;"
                        + "Code departement;Nom du departement\r\n"
        );

        for (VilleDto ville : villes) {

            String code = ville.getCodeDepartement();

            DepartementDto departement = code == null
                    ? null
                    : departements.get(
                    code.toUpperCase(Locale.ROOT)
            );

            String nomDepartement = departement == null
                    ? ""
                    : departement.getNom();

            csv.append(echapperCsv(ville.getNom()))
                    .append(';')
                    .append(ville.getPopulation())
                    .append(';')
                    .append(echapperCsv(code))
                    .append(';')
                    .append(echapperCsv(nomDepartement))
                    .append("\r\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Escapes CSV values and limits spreadsheet formula injection.
     */
    private String echapperCsv(String valeur) {

        if (valeur == null) {
            return "";
        }

        String texte = valeur;

        if (!texte.isEmpty()
                && "=+-@\t\r".indexOf(texte.charAt(0)) >= 0) {
            texte = "'" + texte;
        }

        return "\"" + texte.replace("\"", "\"\"") + "\"";
    }

    /**
     * Creates a styled PDF containing the department and its cities.
     *
     * @param code official department code
     * @return PDF file content
     */
    public byte[] exporterDepartementPdf(String code) {

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le code du département est obligatoire"
            );
        }

        DepartementDto departement =
                departementService.getDepartements()
                        .stream()
                        .filter(d -> d.getCode() != null)
                        .filter(d -> d.getCode().equalsIgnoreCase(
                                code.trim()
                        ))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Département introuvable"
                        ));

        // Retrieve every city belonging to this department.
        List<VilleDto> villes =
                villeService.villesAuDessusPopulation(
                                departement.getId(), -1
                        ).stream()
                        .sorted(
                                Comparator.comparingInt(
                                                VilleDto::getPopulation
                                        ).reversed()
                                        .thenComparing(
                                                VilleDto::getNom,
                                                Comparator.nullsLast(
                                                        String.CASE_INSENSITIVE_ORDER
                                                )
                                        )
                        )
                        .toList();

        String nomDepartement = departement.getNom();

        if (nomDepartement == null
                || nomDepartement.isBlank()) {
            nomDepartement =
                    "Département " + departement.getCode();
        }

        long populationTotale = villes.stream()
                .mapToLong(VilleDto::getPopulation)
                .sum();

        ByteArrayOutputStream sortie =
                new ByteArrayOutputStream();

        // Compact margins leave more room for the city table.
        Document document = new Document(
                PageSize.A4,
                48,
                48,
                78,
                52
        );

        try {
            PdfWriter writer =
                    PdfWriter.getInstance(document, sortie);

            writer.setPageEvent(new RapportPageEvent());

            document.addTitle(
                    "Recensement - " + nomDepartement
            );
            document.addSubject(
                    "Fiche départementale et liste des villes"
            );
            document.addAuthor("API Recensement");

            document.open();

            ajouterIntroduction(
                    document,
                    nomDepartement,
                    departement.getCode()
            );

            ajouterIndicateurs(
                    document,
                    departement.getCode(),
                    villes.size(),
                    populationTotale
            );

            ajouterTableauVilles(document, villes);

        } catch (DocumentException exception) {
            throw new IllegalStateException(
                    "Erreur lors de la génération du PDF",
                    exception
            );
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return sortie.toByteArray();
    }

    /**
     * Adds the report title and department identity.
     */
    private void ajouterIntroduction(
            Document document,
            String nom,
            String code) throws DocumentException {

        Font eyebrowFont = new Font(
                Font.FontFamily.HELVETICA,
                9,
                Font.BOLD,
                TEAL
        );

        Font titleFont = new Font(
                Font.FontFamily.HELVETICA,
                27,
                Font.BOLD,
                NAVY
        );

        Font subtitleFont = new Font(
                Font.FontFamily.HELVETICA,
                10,
                Font.NORMAL,
                SLATE
        );

        Paragraph eyebrow = new Paragraph(
                "FICHE DÉPARTEMENTALE  /  CODE " + code,
                eyebrowFont
        );
        eyebrow.setSpacingAfter(8);
        document.add(eyebrow);

        Paragraph titre = new Paragraph(nom, titleFont);
        titre.setLeading(32);
        titre.setSpacingAfter(8);
        document.add(titre);

        String date = LocalDate.now().format(
                DateTimeFormatter.ofPattern(
                        "dd MMMM yyyy",
                        FRENCH
                )
        );

        Paragraph sousTitre = new Paragraph(
                "Panorama des villes et de leur population"
                        + "  |  Édition du " + date,
                subtitleFont
        );

        sousTitre.setSpacingAfter(18);
        document.add(sousTitre);
    }

    /**
     * Adds the three summary indicators.
     */
    private void ajouterIndicateurs(
            Document document,
            String code,
            int nombreVilles,
            long populationTotale
    ) throws DocumentException {

        PdfPTable indicateurs = new PdfPTable(3);
        indicateurs.setWidthPercentage(100);
        indicateurs.setWidths(
                new float[]{1, 1, 1.4f}
        );
        indicateurs.setSpacingAfter(19);

        indicateurs.addCell(creerIndicateur(
                "CODE DÉPARTEMENT",
                code
        ));

        indicateurs.addCell(creerIndicateur(
                "VILLES RECENSÉES",
                formaterNombre(nombreVilles)
        ));

        indicateurs.addCell(creerIndicateur(
                "POPULATION TOTALE",
                formaterNombre(populationTotale)
        ));

        document.add(indicateurs);
    }

    /**
     * Creates a summary indicator with a label and a value.
     */
    private PdfPCell creerIndicateur(
            String libelle,
            String valeur) {

        Font labelFont = new Font(
                Font.FontFamily.HELVETICA,
                8,
                Font.BOLD,
                SLATE
        );

        Font valueFont = new Font(
                Font.FontFamily.HELVETICA,
                19,
                Font.BOLD,
                NAVY
        );

        PdfPCell cellule = new PdfPCell();
        cellule.setBackgroundColor(LIGHT);
        cellule.setBorderColor(WHITE);
        cellule.setBorderWidth(4);
        cellule.setPadding(11);

        Paragraph label = new Paragraph(
                libelle,
                labelFont
        );
        label.setSpacingAfter(6);

        Paragraph value = new Paragraph(
                valeur,
                valueFont
        );
        value.setLeading(23);

        cellule.addElement(label);
        cellule.addElement(value);

        return cellule;
    }

    /**
     * Adds the city table with alternating row colors.
     */
    private void ajouterTableauVilles(
            Document document,
            List<VilleDto> villes
    ) throws DocumentException {

        Font sectionFont = new Font(
                Font.FontFamily.HELVETICA,
                15,
                Font.BOLD,
                NAVY
        );

        Font descriptionFont = new Font(
                Font.FontFamily.HELVETICA,
                9,
                Font.NORMAL,
                SLATE
        );

        Paragraph section = new Paragraph(
                "Villes du département",
                sectionFont
        );
        section.setSpacingAfter(4);
        document.add(section);

        Paragraph description = new Paragraph(
                "Classement par population décroissante",
                descriptionFont
        );
        description.setSpacingAfter(10);
        document.add(description);

        PdfPTable tableau = new PdfPTable(3);
        tableau.setWidthPercentage(100);
        tableau.setWidths(
                new float[]{0.7f, 4.2f, 1.8f}
        );
        tableau.setHeaderRows(1);
        tableau.setSpacingAfter(0);
        tableau.setSplitRows(true);
        tableau.setSplitLate(false);

        tableau.addCell(creerEntete(
                "#",
                Element.ALIGN_CENTER
        ));

        tableau.addCell(creerEntete(
                "VILLE",
                Element.ALIGN_LEFT
        ));

        tableau.addCell(creerEntete(
                "HABITANTS",
                Element.ALIGN_RIGHT
        ));

        int rang = 1;

        for (VilleDto ville : villes) {

            BaseColor fond =
                    rang % 2 == 0 ? LIGHT : WHITE;

            tableau.addCell(creerCellule(
                    String.valueOf(rang),
                    fond,
                    Element.ALIGN_CENTER,
                    false
            ));

            tableau.addCell(creerCellule(
                    ville.getNom() == null
                            ? "Ville sans nom"
                            : ville.getNom(),
                    fond,
                    Element.ALIGN_LEFT,
                    true
            ));

            tableau.addCell(creerCellule(
                    formaterNombre(ville.getPopulation()),
                    fond,
                    Element.ALIGN_RIGHT,
                    false
            ));

            rang++;
        }

        if (villes.isEmpty()) {
            PdfPCell vide = new PdfPCell(
                    new Phrase("Aucune ville recensée")
            );
            vide.setColspan(3);
            vide.setPadding(12);
            vide.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );
            tableau.addCell(vide);
        }

        document.add(tableau);
    }

    /**
     * Creates a dark table header cell.
     */
    private PdfPCell creerEntete(
            String texte,
            int alignement) {

        Font font = new Font(
                Font.FontFamily.HELVETICA,
                9,
                Font.BOLD,
                WHITE
        );

        PdfPCell cellule = new PdfPCell(
                new Phrase(texte, font)
        );

        cellule.setBackgroundColor(NAVY);
        cellule.setBorder(Rectangle.NO_BORDER);
        cellule.setPaddingTop(8);
        cellule.setPaddingBottom(8);
        cellule.setPaddingLeft(10);
        cellule.setPaddingRight(10);
        cellule.setHorizontalAlignment(alignement);
        cellule.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        return cellule;
    }

    /**
     * Creates a compact, styled city table cell.
     */
    private PdfPCell creerCellule(
            String texte,
            BaseColor fond,
            int alignement,
            boolean important) {

        Font font = new Font(
                Font.FontFamily.HELVETICA,
                9,
                important ? Font.BOLD : Font.NORMAL,
                NAVY
        );

        PdfPCell cellule = new PdfPCell(
                new Phrase(texte, font)
        );

        cellule.setBackgroundColor(fond);
        cellule.setBorder(Rectangle.BOTTOM);
        cellule.setBorderColor(BORDER);
        cellule.setBorderWidth(0.4f);

        // Compact row padding avoids unnecessary page breaks.
        cellule.setPaddingTop(6);
        cellule.setPaddingBottom(6);
        cellule.setPaddingLeft(10);
        cellule.setPaddingRight(10);

        cellule.setHorizontalAlignment(alignement);
        cellule.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        return cellule;
    }

    /**
     * Formats population values with PDF-compatible spaces.
     */
    private String formaterNombre(long valeur) {

        String resultat = NumberFormat
                .getIntegerInstance(FRENCH)
                .format(valeur);

        // Built-in Helvetica may not render Unicode no-break spaces.
        return resultat
                .replace('\u202F', ' ')
                .replace('\u00A0', ' ');
    }

    /**
     * Draws consistent headers and footers on every PDF page.
     */
    private static class RapportPageEvent
            extends PdfPageEventHelper {

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document) {

            PdfContentByte canvas =
                    writer.getDirectContent();

            Rectangle page = document.getPageSize();

            float left = document.left();
            float right =
                    page.getRight()
                            - document.rightMargin();

            float top = page.getTop();
            float bottom = page.getBottom();

            // Draw the top navigation band.
            canvas.saveState();
            canvas.setColorFill(NAVY);
            canvas.rectangle(
                    page.getLeft(),
                    top - 57,
                    page.getWidth(),
                    57
            );
            canvas.fill();

            // Draw the turquoise accent line.
            canvas.setColorFill(TEAL);
            canvas.rectangle(
                    page.getLeft(),
                    top - 61,
                    page.getWidth(),
                    4
            );
            canvas.fill();
            canvas.restoreState();

            Font headerFont = new Font(
                    Font.FontFamily.HELVETICA,
                    10,
                    Font.BOLD,
                    WHITE
            );

            Font footerFont = new Font(
                    Font.FontFamily.HELVETICA,
                    8,
                    Font.NORMAL,
                    SLATE
            );

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_LEFT,
                    new Phrase(
                            "RECENSEMENT  /  RAPPORT TERRITORIAL",
                            headerFont
                    ),
                    left,
                    top - 35,
                    0
            );

            // Draw the footer separator.
            canvas.saveState();
            canvas.setColorStroke(BORDER);
            canvas.setLineWidth(0.7f);
            canvas.moveTo(
                    left,
                    bottom + 45
            );
            canvas.lineTo(
                    right,
                    bottom + 45
            );
            canvas.stroke();
            canvas.restoreState();

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_LEFT,
                    new Phrase(
                            "API RECENSEMENT  -  DOCUMENT GÉNÉRÉ",
                            footerFont
                    ),
                    left,
                    bottom + 28,
                    0
            );

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            "PAGE " + writer.getPageNumber(),
                            footerFont
                    ),
                    right,
                    bottom + 28,
                    0
            );
        }
    }
}
