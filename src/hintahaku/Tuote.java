/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahaku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Tuupertunut
 */
public class Tuote {

    private final List<Hinta> hinnat = new ArrayList<>();
    private Hinta parasHinta;
    private final String nimi;
    private final String url;

    public Tuote(String url, Document dok) {
        this.url = url;

        nimi = dok.select(".hv-content-box-head-title").text();

        boolean tallennus = false;

        for (Element hintaEl : dok.select(".hv-table-list-tr")) {
            Elements nimiEl = hintaEl.select("[itemprop=\"seller\"]");
            String kaupanNimi = nimiEl.hasText() ? nimiEl.text() : nimiEl.attr("content");

            long hinta = Muunnin.stringToLong(hintaEl.select("[itemprop=\"price\"]").text());

            Elements kuluEl = hintaEl.select(".hv--delivery-fee");
            long postikulut = kuluEl.hasClass("hv--free") ? 0 : kuluEl.hasClass("hv--na") ? -1 : Muunnin.stringToLong(kuluEl.text().replace("(", "").replace(")", ""));

            String toimitusaika = hintaEl.select(".hv--delivery-time").text();

            Kauppa kauppa = Suodattimet.haeKauppa(kaupanNimi);
            if (kauppa == null) {
                kauppa = new Kauppa(kaupanNimi, false, false);
                Suodattimet.lisaaKauppa(kauppa);
                tallennus = true;
            }

            Hinta luotuHinta = new Hinta(kauppa, hinta, postikulut, toimitusaika);
            hinnat.add(luotuHinta);
        }
        laskeParasHinta();

        if (tallennus) {
            Suodattimet.tallenna();
        }
    }

    public List<Hinta> getHinnat() {
        return hinnat;
    }

    public Hinta getParasHinta() {
        return parasHinta;
    }

    public String getNimi() {
        return nimi;
    }

    public String getUrl() {
        return url;
    }

    public void laskeParasHinta() {
        try {
            Hinta hinta = Collections.min(hinnat);
            parasHinta = hinta.getSuodatettuHinta() == -1 ? null : hinta;
        } catch (NoSuchElementException ex) {
            parasHinta = null;
        }
    }
}
