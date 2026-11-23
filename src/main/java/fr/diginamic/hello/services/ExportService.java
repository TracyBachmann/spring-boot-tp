package fr.diginamic.hello.services;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import fr.diginamic.hello.dto.DepartementDto;
import fr.diginamic.hello.dto.VilleDto;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final VilleService villeService;
    private final DepartementService departementService;

    public ExportService(
            VilleService villeService,
            DepartementService departementService) {

        this.villeService = villeService;
        this.departementService = departementService;
    }

    /**
     * Exports cities above a population threshold to CSV.
     *
     * @param min minimum population, exclusive
     * @return CSV content encoded in UTF-8
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
                                (premier, second) -> premier
                        ));

        StringBuilder csv = new StringBuilder();

        // UTF-8 BOM for better compatibility with Excel.
        csv.append('\uFEFF');
        csv.append(
                "Nom de la ville;Nombre d'habitants;"
                        + "Code departement;Nom du departement\r\n"
        );

        for (VilleDto ville : villes) {

            String code = ville.getCodeDepartement();

            DepartementDto departement = code == null
                    ? null
                    : departements.get(code.toUpperCase(Locale.ROOT));

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
     * Escapes CSV values and prevents spreadsheet formulas.
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
     * Exports a department and its cities to PDF.
     *
     * @param code department code
     * @return generated PDF document
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
                        .filter(d -> d.getCode().equalsIgnoreCase(code.trim()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Département introuvable"
                        ));

        // Populations are non-negative in the census dataset.
        List<VilleDto> villes =
                villeService.villesAuDessusPopulation(
                        departement.getId(), -1
                );

        String nom = departement.getNom();

        if (nom == null || nom.isBlank()) {
            nom = "Département " + departement.getCode();
        }

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, sortie);
            document.open();

            Font titre = new Font(
                    Font.FontFamily.HELVETICA, 18, Font.BOLD
            );

            Font texte = new Font(
                    Font.FontFamily.HELVETICA, 11, Font.NORMAL
            );

            Font entete = new Font(
                    Font.FontFamily.HELVETICA, 11, Font.BOLD
            );

            document.add(new Paragraph(nom, titre));
            document.add(new Paragraph(" ", texte));

            document.add(new Paragraph(
                    "Code du département : "
                            + departement.getCode(), texte
            ));

            document.add(new Paragraph(
                    "Nom du département : " + nom, texte
            ));

            document.add(new Paragraph(
                    "Nombre de villes : " + villes.size(), texte
            ));

            document.add(new Paragraph(" ", texte));

            PdfPTable tableau = new PdfPTable(2);
            tableau.setWidthPercentage(100);
            tableau.setWidths(new float[]{4, 2});
            tableau.setHeaderRows(1);

            PdfPCell colonneNom =
                    new PdfPCell(new Phrase("Ville", entete));

            PdfPCell colonnePopulation =
                    new PdfPCell(new Phrase("Population", entete));

            colonneNom.setPadding(6);
            colonnePopulation.setPadding(6);

            tableau.addCell(colonneNom);
            tableau.addCell(colonnePopulation);

            for (VilleDto ville : villes) {
                PdfPCell celluleNom = new PdfPCell(
                        new Phrase(ville.getNom(), texte)
                );

                PdfPCell cellulePopulation = new PdfPCell(
                        new Phrase(
                                String.valueOf(ville.getPopulation()),
                                texte
                        )
                );

                celluleNom.setPadding(5);
                cellulePopulation.setPadding(5);

                tableau.addCell(celluleNom);
                tableau.addCell(cellulePopulation);
            }

            document.add(tableau);

        } catch (DocumentException exception) {
            throw new IllegalStateException(
                    "Erreur lors de la génération du PDF",
                    exception
            );
        } finally {
            document.close();
        }

        return sortie.toByteArray();
    }
}
