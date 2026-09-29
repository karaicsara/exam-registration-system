package org.example.backend.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.example.backend.dto.KandidatUTerminuDto;
import org.example.backend.dto.TerminDetaljDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Component
public class TerminPdfGenerator {

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final float MARGIN = 40f;
    private static final float RED_VISINE = 16f;
    private static final float[] SIRINE_KOLONA = {25f, 115f, 150f, 65f, 60f, 55f, 55f};
    private static final String[] NASLOVI_KOLONA = {
            "#", "Ime i prezime", "Korisničko ime / email", "Telefon", "Prijava", "Status prijave", "Uplata"
    };

    public byte[] generisiSpisakKandidata(TerminDetaljDto detalji) {
        try (PDDocument document = new PDDocument()) {
            PDFont fontRegular = ucitajFont(document, "Arial.ttf");
            PDFont fontBold = ucitajFont(document, "Arial-Bold.ttf");

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream sadrzaj = new PDPageContentStream(document, page);

            float y = page.getMediaBox().getHeight() - MARGIN;
            y = upisiZaglavlje(sadrzaj, fontRegular, fontBold, detalji, y);
            y = upisiZaglavljeTabele(sadrzaj, fontBold, y);

            if (detalji.kandidati().isEmpty()) {
                y -= RED_VISINE;
                upisiCeliju(sadrzaj, fontRegular, 11f, "Za ovaj termin trenutno nema prijavljenih kandidata.",
                        MARGIN, y, ukupnaSirina());
            } else {
                for (KandidatUTerminuDto kandidat : detalji.kandidati()) {
                    if (y - RED_VISINE < MARGIN) {
                        sadrzaj.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        sadrzaj = new PDPageContentStream(document, page);
                        y = page.getMediaBox().getHeight() - MARGIN;
                        y = upisiZaglavljeTabele(sadrzaj, fontBold, y);
                    }
                    y = upisiRedKandidata(sadrzaj, fontRegular, kandidat, y);
                }
            }
            sadrzaj.close();

            ByteArrayOutputStream izlaz = new ByteArrayOutputStream();
            document.save(izlaz);
            return izlaz.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Generisanje PDF dokumenta nije uspelo", e);
        }
    }

    private float upisiZaglavlje(PDPageContentStream sadrzaj, PDFont fontRegular, PDFont fontBold,
                                  TerminDetaljDto detalji, float y) throws IOException {
        sadrzaj.beginText();
        sadrzaj.setFont(fontBold, 16f);
        sadrzaj.newLineAtOffset(MARGIN, y);
        sadrzaj.showText("Spisak kandidata za probni prijemni ispit");
        sadrzaj.endText();
        y -= 26f;

        String[] linije = {
                "Oblast ispita: " + detalji.tipIspitaNaziv(),
                "Datum i vreme: " + detalji.datumOdrzavanja().format(DATUM_FORMAT) + " u " + detalji.vremePocetka(),
                "Mesto održavanja: " + detalji.mestoNaziv() + ", " + detalji.mestoAdresa(),
                "Ukupan broj prijavljenih kandidata: " + detalji.brojPrijavljenih(),
                "Datum generisanja dokumenta: " + LocalDate.now().format(DATUM_FORMAT)
        };
        for (String linija : linije) {
            sadrzaj.beginText();
            sadrzaj.setFont(fontRegular, 11f);
            sadrzaj.newLineAtOffset(MARGIN, y);
            sadrzaj.showText(linija);
            sadrzaj.endText();
            y -= 16f;
        }
        return y - 8f;
    }

    private float upisiZaglavljeTabele(PDPageContentStream sadrzaj, PDFont fontBold, float y) throws IOException {
        povuciLiniju(sadrzaj, y);
        y -= 13f;

        float x = MARGIN;
        for (int i = 0; i < NASLOVI_KOLONA.length; i++) {
            upisiCeliju(sadrzaj, fontBold, 8.5f, NASLOVI_KOLONA[i], x, y, SIRINE_KOLONA[i]);
            x += SIRINE_KOLONA[i];
        }
        y -= 5f;
        povuciLiniju(sadrzaj, y);
        return y - RED_VISINE + 3f;
    }

    private float upisiRedKandidata(PDPageContentStream sadrzaj, PDFont font, KandidatUTerminuDto k, float y) throws IOException {
        float x = MARGIN;
        String[] vrednosti = {
                String.valueOf(k.redniBroj()),
                k.ime() + " " + k.prezime(),
                k.username() + (prazno(k.email()) ? "" : " / " + k.email()),
                prazno(k.telefon()) ? "-" : k.telefon(),
                k.datumKreiranjaPrijave() != null ? k.datumKreiranjaPrijave().format(DATUM_FORMAT) : "-",
                prevediStatusPrijave(k.statusPrijave()),
                prevediStatusUplate(k.statusUplate())
        };
        for (int i = 0; i < vrednosti.length; i++) {
            upisiCeliju(sadrzaj, font, 8.5f, vrednosti[i], x, y, SIRINE_KOLONA[i]);
            x += SIRINE_KOLONA[i];
        }
        return y - RED_VISINE;
    }

    private void povuciLiniju(PDPageContentStream sadrzaj, float y) throws IOException {
        sadrzaj.setLineWidth(0.75f);
        sadrzaj.moveTo(MARGIN, y);
        sadrzaj.lineTo(MARGIN + ukupnaSirina(), y);
        sadrzaj.stroke();
    }

    private void upisiCeliju(PDPageContentStream sadrzaj, PDFont font, float velicina, String tekst,
                              float x, float y, float sirina) throws IOException {
        String prikaz = skratiTekst(font, velicina, tekst == null ? "" : tekst, sirina - 4f);
        sadrzaj.beginText();
        sadrzaj.setFont(font, velicina);
        sadrzaj.newLineAtOffset(x + 2f, y);
        sadrzaj.showText(prikaz);
        sadrzaj.endText();
    }

    private String skratiTekst(PDFont font, float velicina, String tekst, float maxSirina) throws IOException {
        if (sirinaTeksta(font, velicina, tekst) <= maxSirina) {
            return tekst;
        }
        String skraceno = tekst;
        while (!skraceno.isEmpty() && sirinaTeksta(font, velicina, skraceno + "…") > maxSirina) {
            skraceno = skraceno.substring(0, skraceno.length() - 1);
        }
        return skraceno.isEmpty() ? "" : skraceno + "…";
    }

    private float sirinaTeksta(PDFont font, float velicina, String tekst) throws IOException {
        return font.getStringWidth(tekst) / 1000f * velicina;
    }

    private float ukupnaSirina() {
        float ukupno = 0f;
        for (float sirina : SIRINE_KOLONA) {
            ukupno += sirina;
        }
        return ukupno;
    }

    private boolean prazno(String tekst) {
        return tekst == null || tekst.isBlank();
    }

    private String prevediStatusPrijave(String status) {
        return switch (status) {
            case "KREIRANA" -> "Kreirana";
            case "PRIJAVLJENA" -> "Prijavljena";
            case "OTKAZANA" -> "Otkazana";
            default -> status;
        };
    }

    private String prevediStatusUplate(String status) {
        return switch (status) {
            case "NA_CEKANJU" -> "Na čekanju";
            case "ODOBRENA" -> "Odobrena";
            case "ODBIJENA" -> "Odbijena";
            default -> status;
        };
    }

    private PDFont ucitajFont(PDDocument document, String naziv) throws IOException {
        try (InputStream ulaz = new ClassPathResource("fonts/" + naziv).getInputStream()) {
            return PDType0Font.load(document, ulaz);
        }
    }
}
