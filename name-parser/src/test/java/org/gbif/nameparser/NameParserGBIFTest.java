package org.gbif.nameparser;

import com.google.common.collect.Iterables;
import org.apache.commons.io.LineIterator;
import org.gbif.nameparser.api.*;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;

import static org.gbif.nameparser.api.NamePart.INFRASPECIFIC;
import static org.gbif.nameparser.api.NamePart.SPECIFIC;
import static org.gbif.nameparser.api.NameType.*;
import static org.gbif.nameparser.api.NomCode.BOTANICAL;
import static org.gbif.nameparser.api.NomCode.ZOOLOGICAL;
import static org.gbif.nameparser.api.Rank.*;
import static org.junit.Assert.*;

/**
 *
 */
public class NameParserGBIFTest {
  private static Logger LOG = LoggerFactory.getLogger(NameParserGBIFTest.class);
  private static final boolean DEBUG = ManagementFactory.getRuntimeMXBean()
      .getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

  static final NameParser parser = new NameParserGBIF(DEBUG ? 99999999 : 1000);

  @Test
  public void species() throws Exception {
    assertName("Zophosis persis (Chatanay 1914)", "Zophosis persis")
        .species("Zophosis", "persis")
        .basAuthors("1914", "Chatanay")
        .nothingElse();

    assertName("Abies alba Mill.", "Abies alba")
        .species("Abies", "alba")
        .combAuthors(null, "Mill.")
        .nothingElse();

    assertName("Alstonia vieillardii Van Heurck & Müll.Arg.", "Alstonia vieillardii")
        .species("Alstonia", "vieillardii")
        .combAuthors(null, "Van Heurck", "Müll.Arg.")
        .nothingElse();

    assertName("Angiopteris d'urvilleana de Vriese", "Angiopteris d'urvilleana")
        .species("Angiopteris", "d'urvilleana")
        .combAuthors(null, "de Vriese")
        .nothingElse();

    assertName("Agrostis hyemalis (Walter) Britton, Sterns, & Poggenb.", "Agrostis hyemalis")
        .species("Agrostis", "hyemalis")
        .combAuthors(null, "Britton", "Sterns", "Poggenb.")
        .basAuthors(null, "Walter")
        .nothingElse();
  }

  @Test
  public void capitalAuthors() throws Exception {
    assertName("Anniella nigra FISCHER 1885", "Anniella nigra")
        .species("Anniella", "nigra")
        .combAuthors("1885", "Fischer")
        .nothingElse();
  }

  @Test
  public void infraSpecies() throws Exception {

    assertName("Festuca ovina L. subvar. gracilis Hackel", "Festuca ovina subvar. gracilis")
        .infraSpecies("Festuca", "ovina", SUBVARIETY, "gracilis")
        .combAuthors(null, "Hackel")
        .nothingElse();

    assertName("Abies alba ssp. alpina Mill.", "Abies alba subsp. alpina")
        .infraSpecies("Abies", "alba", SUBSPECIES, "alpina")
        .combAuthors(null, "Mill.")
        .nothingElse();

    assertName("Pseudomonas syringae pv. aceris (Ark, 1939) Young, Dye & Wilkie, 1978", "Pseudomonas syringae pv. aceris")
        .infraSpecies("Pseudomonas", "syringae", PATHOVAR, "aceris")
        .combAuthors("1978", "Young", "Dye", "Wilkie")
        .basAuthors("1939", "Ark");

    assertName("Agaricus compactus sarcocephalus (Fr.) Fr. ", "Agaricus compactus sarcocephalus")
        .infraSpecies("Agaricus", "compactus", INFRASPECIFIC_NAME, "sarcocephalus")
        .combAuthors(null, "Fr.")
        .basAuthors(null, "Fr.")
        .nothingElse();

    assertName("Baccharis microphylla Kunth var. rhomboidea Wedd. ex Sch. Bip. (nom. nud.)", "Baccharis microphylla var. rhomboidea")
        .infraSpecies("Baccharis", "microphylla", VARIETY, "rhomboidea")
        .combAuthors(null, "Sch.Bip.")
        .combExAuthors("Wedd.")
        .nomNote("nom.nud.")
        .nothingElse();

  }

  @Test
  public void exAuthors() throws Exception {
    assertName("Acacia truncata (Burm. f.) hort. ex Hoffmanns.", "Acacia truncata")
        .species("Acacia", "truncata")
        .basAuthors(null, "Burm.f.")
        .combExAuthors("hort.")
        .combAuthors(null, "Hoffmanns.")
        .nothingElse();

    // In botany (99% of ex author use) the ex author comes first, see https://en.wikipedia.org/wiki/Author_citation_(botany)#Usage_of_the_term_.22ex.22
    assertName("Gymnocalycium eurypleurumn Plesn¡k ex F.Ritter", "Gymnocalycium eurypleurumn")
        .species("Gymnocalycium", "eurypleurumn")
        .combAuthors(null, "F.Ritter")
        .combExAuthors("Plesnik")
        .doubtful()
        .nothingElse();

    assertName("Abutilon bastardioides Baker f. ex Rose", "Abutilon bastardioides")
        .species("Abutilon", "bastardioides")
        .combAuthors(null, "Rose")
        .combExAuthors("Baker f.")
        .nothingElse();

    assertName("Baccharis microphylla Kunth var. rhomboidea Wedd. ex Sch. Bip. (nom. nud.)", "Baccharis microphylla var. rhomboidea")
        .infraSpecies("Baccharis", "microphylla", VARIETY, "rhomboidea")
        .combAuthors(null, "Sch.Bip.")
        .combExAuthors("Wedd.")
        .nomNote("nom.nud.")
        .nothingElse();

    assertName("Abies brevifolia hort. ex Dallim.", "Abies brevifolia")
        .species("Abies", "brevifolia")
        .combExAuthors("hort.")
        .combAuthors(null, "Dallim.")
        .nothingElse();

    assertName("Abies brevifolia cv. ex Dallim.", "Abies brevifolia")
        .species("Abies", "brevifolia")
        .combExAuthors("hort.")
        .combAuthors(null, "Dallim.")
        .nothingElse();

    assertName("Abutilon ×hybridum cv. ex Voss", "Abutilon × hybridum")
        .species("Abutilon", "hybridum")
        .notho(SPECIFIC)
        .combExAuthors("hort.")
        .combAuthors(null, "Voss")
        .nothingElse();

    // "Abutilon bastardioides Baker f. ex Rose"
    // "Aukuba ex Koehne 'Thunb'   "
    // "Crepinella subgen. Marchal ex Oliver  "
    // "Echinocereus sect. Triglochidiata ex Bravo"
    // "Hadrolaelia sect. Sophronitis ex Chiron & V.P.Castro"
  }

  @Test
  public void fourPartedNames() throws Exception {
    assertName("Poa pratensis kewensis primula (L.) Rouy, 1913", "Poa pratensis primula")
        .infraSpecies("Poa", "pratensis", INFRASUBSPECIFIC_NAME, "primula")
        .combAuthors("1913", "Rouy")
        .basAuthors(null, "L.")
        .nothingElse();

    assertName("Bombus sichelii alticola latofasciatus", "Bombus sichelii latofasciatus")
        .infraSpecies("Bombus", "sichelii", INFRASUBSPECIFIC_NAME, "latofasciatus")
        .nothingElse();

    assertName("Acipenser gueldenstaedti colchicus natio danubicus Movchan, 1967", "Acipenser gueldenstaedti natio danubicus")
        .infraSpecies("Acipenser", "gueldenstaedti", NATIO, "danubicus")
        .combAuthors("1967", "Movchan")
        .code(ZOOLOGICAL)
        .nothingElse();
  }

  @Test
  public void monomial() throws Exception {

    assertName("Acripeza Guérin-Ménéville 1838", "Acripeza")
        .monomial("Acripeza")
        .combAuthors("1838", "Guérin-Ménéville")
        .nothingElse();

  }

  @Test
  public void infraGeneric() throws Exception {
    assertName("Arrhoges (Antarctohoges)", SUBGENUS,"Arrhoges subgen. Antarctohoges")
        .infraGeneric("Arrhoges", SUBGENUS, "Antarctohoges")
        .nothingElse();




    assertName("Echinocereus sect. Triglochidiata Bravo", "Echinocereus sect. Triglochidiata")
        .infraGeneric("Echinocereus", SECTION, "Triglochidiata")
        .combAuthors(null, "Bravo")
        .code(BOTANICAL)
        .nothingElse();

    assertName("Zignoella subgen. Trematostoma Sacc.", "Zignoella subgen. Trematostoma")
        .infraGeneric("Zignoella", SUBGENUS, "Trematostoma")
        .combAuthors(null, "Sacc.")
        .nothingElse();

    assertName("subgen. Trematostoma Sacc.", "Trematostoma")
        .monomial("Trematostoma", SUBGENUS)
        .combAuthors(null, "Sacc.")
        .nothingElse();

    assertName("Polygonum subgen. Bistorta (L.) Zernov", "Polygonum subgen. Bistorta")
        .infraGeneric("Polygonum", SUBGENUS, "Bistorta")
        .combAuthors(null, "Zernov")
        .basAuthors(null, "L.")
        .nothingElse();

    assertName("Arrhoges (Antarctohoges)", "Arrhoges")
        .monomial("Arrhoges")
        .basAuthors(null, "Antarctohoges")
        .nothingElse();

    assertName("Arrhoges (Antarctohoges)", SUBGENUS,"Arrhoges subgen. Antarctohoges")
        .infraGeneric("Arrhoges", SUBGENUS, "Antarctohoges")
        .nothingElse();

    assertName("Festuca subg. Schedonorus (P. Beauv. ) Peterm.","Festuca subgen. Schedonorus")
        .infraGeneric("Festuca", SUBGENUS, "Schedonorus")
        .combAuthors(null, "Peterm.")
        .basAuthors(null, "P.Beauv.")
        .nothingElse();

    assertName("Catapodium subg.Agropyropsis  Trab.", "Catapodium subgen. Agropyropsis")
        .infraGeneric("Catapodium", SUBGENUS, "Agropyropsis")
        .combAuthors(null, "Trab.")
        .nothingElse();

    assertName(" Gnaphalium subg. Laphangium Hilliard & B. L. Burtt", "Gnaphalium subgen. Laphangium")
        .infraGeneric("Gnaphalium", SUBGENUS, "Laphangium")
        .combAuthors(null, "Hilliard", "B.L.Burtt")
        .nothingElse();

    assertName("Woodsiaceae (Hooker) Herter", "Woodsiaceae")
        .monomial("Woodsiaceae", FAMILY)
        .combAuthors(null, "Herter")
        .basAuthors(null, "Hooker")
        .nothingElse();
  }

  @Test
  public void notNames() throws Exception {
    assertName("Diatrypella favacea var. favacea (Fr.) Ces. & De Not.", "Diatrypella favacea var. favacea")
        .infraSpecies("Diatrypella", "favacea", VARIETY, "favacea")
        .combAuthors(null, "Ces.", "De Not.")
        .basAuthors(null, "Fr.")
        .nothingElse();

    assertName("Protoventuria rosae (De Not.) Berl. & Sacc.", "Protoventuria rosae")
        .species("Protoventuria", "rosae")
        .combAuthors(null, "Berl.", "Sacc.")
        .basAuthors(null, "De Not.")
        .nothingElse();

    assertName("Hormospora De Not.", "Hormospora")
        .monomial("Hormospora")
        .combAuthors(null, "De Not.")
        .nothingElse();
  }

  /**
   * https://github.com/gbif/checklistbank/issues/48
   */
  @Test
  public void novPlaceholder() throws Exception {
    assertName("Gen.nov.", null)
        .type(PLACEHOLDER)
        .rank(Rank.GENUS)
        .nomNote("Gen.nov.")
        .nothingElse();

    assertName("Gen.nov. sp.nov.", null)
        .type(PLACEHOLDER)
        .rank(Rank.SPECIES)
        .nomNote("Gen.nov. sp.nov.")
        .nothingElse();
  }

  /**
   * http://dev.gbif.org/issues/browse/POR-2459
   */
  @Test
  public void unparsablePlaceholder() throws Exception {
    assertUnparsable("[unassigned] Cladobranchia", PLACEHOLDER);
    assertUnparsable("Biota incertae sedis", PLACEHOLDER);
    assertUnparsable("Mollusca not assigned", PLACEHOLDER);
    assertUnparsable("Unaccepted", PLACEHOLDER);
    assertUnparsable("uncultured Verrucomicrobiales bacterium", PLACEHOLDER);
    assertUnparsable("uncultured Vibrio sp.", PLACEHOLDER);
    assertUnparsable("uncultured virus", PLACEHOLDER);
    // ITIS placeholders:
    assertUnparsable("Temp dummy name", PLACEHOLDER);
  }

  @Test
  public void placeholder() throws Exception {
    assertName("denheyeri Eghbalian, Khanjani and Ueckermann in Eghbalian, Khanjani & Ueckermann, 2017", "? denheyeri")
        .species("?", "denheyeri")
        .combAuthors("2017", "Eghbalian", "Khanjani", "Ueckermann")
        .type(PLACEHOLDER)
        .nothingElse();

    assertName("\"? gryphoidis", "? gryphoidis")
        .species("?", "gryphoidis")
        .type(PLACEHOLDER)
        .nothingElse();

    assertName("\"? gryphoidis (Bourguignat 1870) Schoepf. 1909", "? gryphoidis")
        .species("?", "gryphoidis")
        .basAuthors("1870", "Bourguignat")
        .combAuthors("1909", "Schoepf.")
        .type(PLACEHOLDER)
        .nothingElse();

    assertName("Missing penchinati Bourguignat, 1870", "? penchinati")
        .species("?", "penchinati")
        .combAuthors("1870", "Bourguignat")
        .type(PLACEHOLDER)
        .nothingElse();
  }

  @Test
  public void sanctioned() throws Exception {
    // sanctioning authors not supported
    // https://github.com/GlobalNamesArchitecture/gnparser/issues/409
    assertName("Boletus versicolor L. : Fr.", "Boletus versicolor")
        .species("Boletus", "versicolor")
        .combAuthors(null, "L.")
        .sanctAuthor("Fr.")
        .nothingElse();

    assertName("Agaricus compactus sarcocephalus (Fr. : Fr.) Fr. ", "Agaricus compactus sarcocephalus")
        .infraSpecies("Agaricus", "compactus", INFRASPECIFIC_NAME, "sarcocephalus")
        .combAuthors(null, "Fr.")
        .basAuthors(null, "Fr.")
        .nothingElse();

    assertName("Agaricus compactus sarcocephalus (Fr. : Fr.) Fr. ", "Agaricus compactus sarcocephalus")
        .infraSpecies("Agaricus", "compactus", INFRASPECIFIC_NAME, "sarcocephalus")
        .combAuthors(null, "Fr.")
        .basAuthors(null, "Fr.")
        .nothingElse();
  }

  @Test
  public void nothotaxa() throws Exception {
    // https://github.com/GlobalNamesArchitecture/gnparser/issues/410
    assertName("Iris germanica nothovar. florentina", "Iris germanica nothovar. florentina")
        .infraSpecies("Iris", "germanica", VARIETY, "florentina")
        .notho(INFRASPECIFIC)
        .nothingElse();

    assertName("Abies alba var. ×alpina L.", "Abies alba nothovar. alpina")
        .infraSpecies("Abies", "alba", VARIETY, "alpina")
        .notho(INFRASPECIFIC)
        .combAuthors(null, "L.")
        .nothingElse();
  }

  @Test
  public void simpleBinomial() throws Exception {
    assertName("Abies alba", "Abies alba")
        .species("Abies", "alba")
        .nothingElse();
  }

  @Test
  public void candidatus() throws Exception {
    assertName("\"Candidatus Endowatersipora\" Anderson and Haygood, 2007", "\"Candidatus Endowatersipora\"")
        .monomial("Endowatersipora")
        .candidatus()
        .combAuthors("2007", "Anderson", "Haygood")
        .nothingElse();

    assertName("Candidatus Phytoplasma allocasuarinae", "\"Candidatus Phytoplasma allocasuarinae\"")
        .species("Phytoplasma", "allocasuarinae")
        .candidatus()
        .nothingElse();

    assertName("Ca. Phytoplasma allocasuarinae", "\"Candidatus Phytoplasma allocasuarinae\"")
        .species("Phytoplasma", "allocasuarinae")
        .candidatus()
        .nothingElse();

    assertName("Ca. Phytoplasma", "\"Candidatus Phytoplasma\"")
        .monomial("Phytoplasma")
        .candidatus()
        .nothingElse();

    assertName("'Candidatus Nicolleia'", "\"Candidatus Nicolleia\"")
        .monomial("Nicolleia")
        .candidatus()
        .nothingElse();

    assertName("\"Candidatus Riegeria\" Gruber-Vodicka et al., 2011", "\"Candidatus Riegeria\"")
        .monomial("Riegeria")
        .combAuthors("2011", "Gruber-Vodicka", "al.")
        .candidatus()
        .nothingElse();

    assertName("Candidatus Endobugula", "\"Candidatus Endobugula\"")
        .monomial("Endobugula")
        .candidatus()
        .nothingElse();

    // not candidate names
    assertName("Centropogon candidatus Lammers", "Centropogon candidatus")
        .species("Centropogon", "candidatus")
        .combAuthors(null, "Lammers")
        .nothingElse();
  }

  @Test
  @Ignore
  public void strains() throws Exception {
    assertName("Endobugula sp. JYr4", "Endobugula sp. JYr4")
        .species("Endobugula", null)
        .strain("sp. JYr4")
        .nothingElse();

    // avoid author & year to be accepted as strain
    assertName("Anniella nigra FISCHER 1885", "Anniella nigra")
        .species("Anniella", "nigra")
        .combAuthors("1885", "Fischer")
        .nothingElse();
  }

  @Test
  public void norwegianRadiolaria() throws Exception {
    assertName("Actinomma leptodermum longispinum Cortese & Bjørklund 1998", "Actinomma leptodermum longispinum")
        .infraSpecies("Actinomma", "leptodermum", INFRASPECIFIC_NAME, "longispinum")
        .combAuthors("1998", "Cortese", "Bjørklund")
        .nothingElse();

    assertName("Arachnosphaera dichotoma  Jørgensen, 1900", "Arachnosphaera dichotoma")
        .species("Arachnosphaera", "dichotoma")
        .combAuthors("1900", "Jørgensen")
        .nothingElse();

    assertName("Hexaconthium pachydermum forma legitime Cortese & Bjørklund 1998","Hexaconthium pachydermum f. legitime")
        .infraSpecies("Hexaconthium", "pachydermum", FORM, "legitime")
        .combAuthors("1998", "Cortese", "Bjørklund")
        .nothingElse();

    assertName("Hexaconthium pachydermum form A Cortese & Bjørklund 1998","Hexaconthium pachydermum f. A")
        .infraSpecies("Hexaconthium", "pachydermum", FORM, "A")
        .combAuthors("1998", "Cortese", "Bjørklund")
        .type(INFORMAL)
        .nothingElse();

    assertName("Trisulcus aff. nana  (Popofsky, 1913), Petrushevskaya, 1971", "Trisulcus nana")
        .species("Trisulcus", "nana")
        .basAuthors("1913", "Popofsky")
        .combAuthors("1971", "Petrushevskaya")
        .type(INFORMAL)
        .nothingElse();

    assertName("Tripodiscium gephyristes  (Hülseman, 1963) BJ&KR-Atsdatabanken", "Tripodiscium gephyristes")
        .species("Tripodiscium", "gephyristes")
        .basAuthors("1963", "Hülseman")
        .combAuthors(null, "BJ", "KR-Atsdatabanken")
        .nothingElse();

    assertName("Protocystis xiphodon  (Haeckel, 1887), Borgert, 1901", "Protocystis xiphodon")
        .species("Protocystis", "xiphodon")
        .basAuthors("1887", "Haeckel")
        .combAuthors("1901", "Borgert")
        .nothingElse();

    assertName("Acrosphaera lappacea  (Haeckel, 1887) Takahashi, 1991", "Acrosphaera lappacea")
        .species("Acrosphaera", "lappacea")
        .basAuthors("1887", "Haeckel")
        .combAuthors("1991", "Takahashi")
        .nothingElse();
  }

  @Test
  public void cultivars() throws Exception {
    assertName("Abutilon 'Kentish Belle'", "Abutilon 'Kentish Belle'")
        .cultivar("Abutilon", "Kentish Belle")
        .nothingElse();



    assertName("Acer campestre L. cv. 'nanum'", "Acer campestre 'nanum'")
        .cultivar("Acer", "campestre", "nanum")
        .combAuthors(null, "L.")
        .nothingElse();

    assertName("Verpericola megasoma \"Dall\" Pils.", "Verpericola megasoma 'Dall'")
        .cultivar("Verpericola", "megasoma", "Dall")
        .combAuthors(null, "Pils.")
        .nothingElse();

    assertName("Abutilon 'Kentish Belle'", "Abutilon 'Kentish Belle'")
        .cultivar("Abutilon", "Kentish Belle")
        .nothingElse();

    assertName("Abutilon 'Nabob'", "Abutilon 'Nabob'")
        .cultivar("Abutilon", "Nabob")
        .nothingElse();

    assertName("Sorbus americana Marshall cv. 'Belmonte'", "Sorbus americana 'Belmonte'")
        .cultivar("Sorbus", "americana", "Belmonte")
        .combAuthors(null, "Marshall")
        .nothingElse();

    assertName("Sorbus hupehensis C.K.Schneid. cv. 'November pink'", "Sorbus hupehensis 'November pink'")
        .cultivar("Sorbus", "hupehensis", "November pink")
        .combAuthors(null, "C.K.Schneid.")
        .nothingElse();

    assertName("Symphoricarpos albus (L.) S.F.Blake cv. 'Turesson'", "Symphoricarpos albus 'Turesson'")
        .cultivar("Symphoricarpos", "albus", CULTIVAR, "Turesson")
        .basAuthors(null, "L.")
        .combAuthors(null, "S.F.Blake")
        .nothingElse();

    assertName("Symphoricarpos sp. cv. 'mother of pearl'", "Symphoricarpos 'mother of pearl'")
        .cultivar("Symphoricarpos", CULTIVAR, "mother of pearl")
        .nothingElse();

    assertName("Primula Border Auricula Group", "Primula Border Auricula Group")
        .cultivar("Primula", CULTIVAR_GROUP, "Border Auricula")
        .nothingElse();

    assertName("Rhododendron boothii Mishmiense Group", "Rhododendron boothii Mishmiense Group")
        .cultivar("Rhododendron", "boothii", CULTIVAR_GROUP, "Mishmiense")
        .nothingElse();

    assertName("Paphiopedilum Sorel grex", "Paphiopedilum Sorel gx")
        .cultivar("Paphiopedilum", GREX, "Sorel")
        .nothingElse();

    assertName("Cattleya Prince John gx", "Cattleya Prince John gx")
        .cultivar("Cattleya", GREX, "Prince John")
        .nothingElse();
  }

  @Test
  public void hybridFormulas() throws Exception {
    assertName("Polypodium  x vulgare nothosubsp. mantoniae (Rothm.) Schidlay", "Polypodium vulgare nothosubsp. mantoniae")
        .infraSpecies("Polypodium", "vulgare", SUBSPECIES, "mantoniae")
        .basAuthors(null, "Rothm.")
        .combAuthors(null, "Schidlay")
        .notho(INFRASPECIFIC)
        .nothingElse();

    assertHybridFormula("Asplenium rhizophyllum DC. x ruta-muraria E.L. Braun 1939");
    assertHybridFormula("Arthopyrenia hyalospora X Hydnellum scrobiculatum");
    assertHybridFormula("Arthopyrenia hyalospora (Banker) D. Hall X Hydnellum scrobiculatum D.E. Stuntz");
    assertHybridFormula("Arthopyrenia hyalospora × ? ");
    assertHybridFormula("Agrostis L. × Polypogon Desf. ");
    assertHybridFormula("Agrostis stolonifera L. × Polypogon monspeliensis (L.) Desf. ");
    assertHybridFormula("Asplenium rhizophyllum X A. ruta-muraria E.L. Braun 1939");
    assertHybridFormula("Asplenium rhizophyllum DC. x ruta-muraria E.L. Braun 1939");
    assertHybridFormula("Asplenium rhizophyllum x ruta-muraria");
    assertHybridFormula("Salix aurita L. × S. caprea L.");
    assertHybridFormula("Mentha aquatica L. × M. arvensis L. × M. spicata L.");
    assertHybridFormula("Polypodium vulgare subsp. prionodes (Asch.) Rothm. × subsp. vulgare");
    assertHybridFormula("Tilletia caries (Bjerk.) Tul. × T. foetida (Wallr.) Liro.");
  }

  private void assertHybridFormula(String name) {
    assertUnparsable(name, HYBRID_FORMULA);
  }

  @Test
  public void oTU() throws Exception {
    assertName("BOLD:ACW2100", "BOLD:ACW2100")
        .monomial("BOLD:ACW2100", Rank.SPECIES)
        .type(OTU)
        .nothingElse();

    assertName("Festuca sp. BOLD:ACW2100", "BOLD:ACW2100")
        .monomial("BOLD:ACW2100", Rank.SPECIES)
        .type(OTU)
        .nothingElse();

    // no OTU names
    assertName("Boldenaria", "Boldenaria")
        .monomial("Boldenaria")
        .nothingElse();

    assertName("Boldea", "Boldea")
        .monomial("Boldea")
        .nothingElse();

    assertName("Boldiaceae", "Boldiaceae")
        .monomial("Boldiaceae", Rank.FAMILY)
        .nothingElse();

    assertName("Boldea vulgaris", "Boldea vulgaris")
        .species("Boldea", "vulgaris")
        .nothingElse();
  }

  @Test
  public void hybridAlikeNames() throws Exception {
    assertName("Huaiyuanella Xing, Yan & Yin, 1984", "Huaiyuanella")
        .monomial("Huaiyuanella")
        .combAuthors("1984", "Xing", "Yan", "Yin")
        .nothingElse();

    assertName("Caveasphaera Xiao & Knoll, 2000", "Caveasphaera")
        .monomial("Caveasphaera")
        .combAuthors("2000", "Xiao", "Knoll")
        .nothingElse();
  }

  @Test
  @Ignore("Need to evaluate and implement these alpha/beta/gamme/theta names. Comes from cladistics?")
  public void alphaBetaThetaNames() {
    // 11383509 | VARIETY | Trianosperma ficifolia var. βrigida Cogn.
    // 11599666 |         | U. caerulea var. β
    // 12142976 | CLASS   | γ Proteobacteria
    // 12142978 | CLASS   | γ-proteobacteria
    // 16220269 |         | U. caerulea var. β
    // 1297218  | SPECIES | Bacteriophage Qβ
    // 307122   | SPECIES | Agaricus collinitus β mucosus (Bull.) Fr.
    // 313460   | SPECIES | Agaricus muscarius β regalis Fr. (1821)
    // 315162   | SPECIES | Agaricus personatus β saevus
    // 1774875  | VARIETY | Caesarea albiflora var. βramosa Cambess.
    // 3164679  | VARIETY | Cyclotus amethystinus var. α Guppy, 1868 (in part)
    // 3164681  | VARIETY | Cyclotus amethystinus var. β Guppy, 1868
    // 6531344  | SPECIES | Lycoperdon pyriforme β tessellatum Pers. (1801)
    // 7487686  | VARIETY | Nephilengys malabarensis var. β
    // 9665391  | VARIETY | Ranunculus purshii Hook. var. repens(-δ) Hook.
  }

  @Test
  public void hybridNames() throws Exception {
    assertName("+ Pyrocrataegus willei L.L.Daniel", "× Pyrocrataegus willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.GENERIC)
        .nothingElse();

    assertName("×Pyrocrataegus willei L.L. Daniel", "× Pyrocrataegus willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.GENERIC)
        .nothingElse();

    assertName(" × Pyrocrataegus willei  L. L. Daniel", "× Pyrocrataegus willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.GENERIC)
        .nothingElse();

    assertName(" X Pyrocrataegus willei L. L. Daniel", "× Pyrocrataegus willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.GENERIC)
        .nothingElse();

    assertName("Pyrocrataegus ×willei L. L. Daniel", "Pyrocrataegus × willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.SPECIFIC)
        .nothingElse();

    assertName("Pyrocrataegus × willei L. L. Daniel", "Pyrocrataegus × willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.SPECIFIC)
        .nothingElse();

    assertName("Pyrocrataegus x willei L. L. Daniel", "Pyrocrataegus × willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.SPECIFIC)
        .nothingElse();

    assertName("Pyrocrataegus X willei L. L. Daniel", "Pyrocrataegus × willei")
        .species("Pyrocrataegus", "willei")
        .combAuthors(null, "L.L.Daniel")
        .notho(NamePart.SPECIFIC)
        .nothingElse();

    assertName("Pyrocrataegus willei ×libidi  L.L.Daniel", "Pyrocrataegus willei × libidi")
        .infraSpecies("Pyrocrataegus", "willei", INFRASPECIFIC_NAME, "libidi")
        .combAuthors(null, "L.L.Daniel")
        .notho(INFRASPECIFIC)
        .nothingElse();

    assertName("Pyrocrataegus willei nothosubsp. libidi  L.L.Daniel", "Pyrocrataegus willei nothosubsp. libidi")
        .infraSpecies("Pyrocrataegus", "willei", SUBSPECIES, "libidi")
        .combAuthors(null, "L.L.Daniel")
        .notho(INFRASPECIFIC)
        .nothingElse();

    assertName("+ Pyrocrataegus willei nothosubsp. libidi  L.L.Daniel", "Pyrocrataegus willei nothosubsp. libidi")
        .infraSpecies("Pyrocrataegus", "willei", SUBSPECIES, "libidi")
        .combAuthors(null, "L.L.Daniel")
        .notho(INFRASPECIFIC)
        .nothingElse();

    //TODO: impossible name. should this not be a generic hybrid as its the highest rank crossed?
    assertName("×Pyrocrataegus ×willei ×libidi L.L.Daniel", "Pyrocrataegus willei × libidi")
        .infraSpecies("Pyrocrataegus", "willei", INFRASPECIFIC_NAME, "libidi")
        .combAuthors(null, "L.L.Daniel")
        .notho(INFRASPECIFIC)
        .nothingElse();

  }

  @Test
  public void authorVariations() throws Exception {
    assertName("Cirsium creticum d'Urv.", "Cirsium creticum")
        .species("Cirsium", "creticum")
        .combAuthors(null, "d'Urv.")
        .nothingElse();

    // TODO: autonym authors are the species authors !!!
    assertName("Cirsium creticum d'Urv. subsp. creticum", "Cirsium creticum subsp. creticum")
        .infraSpecies("Cirsium", "creticum", SUBSPECIES, "creticum")
        //.combAuthors(null, "d'Urv.")
        .autonym()
        .nothingElse();

    assertName("Cirsium creticum Balsamo M Fregni E Tongiorgi P", "Cirsium creticum")
        .species("Cirsium", "creticum")
        .combAuthors(null, "M.Balsamo", "E.Fregni", "P.Tongiorgi")
        .nothingElse();

    assertName("Cirsium creticum Balsamo M Todaro MA", "Cirsium creticum")
        .species("Cirsium", "creticum")
        .combAuthors(null, "M.Balsamo", "M.A.Todaro")
        .nothingElse();

    assertName("Bolivina albatrossi Cushman Em. Sellier de Civrieux, 1976", "Bolivina albatrossi")
        .species("Bolivina", "albatrossi")
        .combAuthors("1976", "Cushman Em.Sellier de Civrieux")
        .nothingElse();

    // http://dev.gbif.org/issues/browse/POR-101
    assertName("Cribbia pendula la Croix & P.J.Cribb", "Cribbia pendula")
        .species("Cribbia", "pendula")
        .combAuthors(null, "la Croix", "P.J.Cribb")
        .nothingElse();

    assertName("Cribbia pendula le Croix & P.J.Cribb", "Cribbia pendula")
        .species("Cribbia", "pendula")
        .combAuthors(null, "le Croix", "P.J.Cribb")
        .nothingElse();

    assertName("Cribbia pendula de la Croix & le P.J.Cribb", "Cribbia pendula")
        .species("Cribbia", "pendula")
        .combAuthors(null, "de la Croix", "le P.J.Cribb")
        .nothingElse();

    assertName("Cribbia pendula Croix & de le P.J.Cribb", "Cribbia pendula")
        .species("Cribbia", "pendula")
        .combAuthors(null, "Croix", "de le P.J.Cribb")
        .nothingElse();

    assertName("Navicula ambigua f. craticularis Istv?nffi, 1898, 1897", "Navicula ambigua f. craticularis")
        .infraSpecies("Navicula", "ambigua", Rank.FORM, "craticularis")
        .combAuthors("1898", "Istvnffi")
        .doubtful()
        .nothingElse();

    assertName("Cestodiscus gemmifer F.S.Castracane degli Antelminelli", "Cestodiscus gemmifer")
        .species("Cestodiscus", "gemmifer")
        .combAuthors(null, "F.S.Castracane degli Antelminelli")
        .nothingElse();

    assertName("Hieracium scorzoneraefolium De la Soie", "Hieracium scorzoneraefolium")
        .species("Hieracium", "scorzoneraefolium")
        .combAuthors(null, "De la Soie")
        .nothingElse();

    assertName("Calycostylis aurantiaca Hort. ex Vilmorin", "Calycostylis aurantiaca")
        .species("Calycostylis", "aurantiaca")
        .combAuthors(null, "Vilmorin")
        .combExAuthors("hort.")
        .nothingElse();

  }

  @Test
  public void extinctNames() throws Exception {
    assertName("†Titanoptera", "Titanoptera")
        .monomial("Titanoptera")
        .nothingElse();

    assertName("† Tuarangiida MacKinnon, 1982", "Tuarangiida")
        .monomial("Tuarangiida")
        .combAuthors("1982", "MacKinnon")
        .nothingElse();
  }

  /**
   * Simply test all names in names.txt and make sure they parse without exception.
   * This test does not verify if the parsed name was correct in all its pieces,
   * so only use this as a quick way to add names to tests.
   *
   * Exceptional cases should better be tested in a test on its own!
   */
  @Test
  public void nameFile() throws Exception {
    for (String name : iterResource("names.txt")) {
      ParsedName n = parser.parse(name, null);
      assertTrue(name, n.getState().isParsed());
    }
  }

  /**
   * Test all names in doubtful.txt and make sure they parse without exception,
   * but have a doubtful flag set.
   * This test does not verify if the parsed name was correct in all its pieces,
   * so only use this as a quick way to add names to tests.
   *
   * Exceptional cases should better be tested in a test on its own!
   */
  @Test
  public void doubtfulFile() throws Exception {
    for (String name : iterResource("doubtful.txt")) {
      ParsedName n = parser.parse(name, null);
      assertTrue(name, n.isDoubtful());
      assertTrue(name, n.getState().isParsed());
      assertTrue(name, n.getType().isParsable());
    }
  }

  /**
   * Test all names in unparsable.txt and makes sure they are not parsable.
   */
  @Test
  public void unparsableFile() throws Exception {
    for (String name : iterResource("unparsable.txt")) {
      try {
        parser.parse(name);
        fail("Expected "+name+" to be unparsable");
      } catch (UnparsableNameException ex) {
        assertEquals(name, ex.getName());
      }
    }
  }

  /**
   * Converts lines of a classpath resource that are not empty or are comments starting with #
   * into a simple string iterable
   */
  private Iterable<String> iterResource(String resource) throws UnsupportedEncodingException {
    LineIterator iter = new LineIterator(resourceReader(resource));
    return Iterables.filter(() -> iter,
        line -> line != null && !line.trim().isEmpty() && !line.startsWith("#")
    );
  }

  /**
   * Expect empty unparsable results for nothing or whitespace
   */
  @Test
  public void empty() throws Exception {
    assertNoName(null);
    assertNoName("");
    assertNoName(" ");
    assertNoName("\t");
    assertNoName("\n");
    assertNoName("\t\n");
    assertNoName("\"");
    assertNoName("'");
  }

  /**
   * Avoid nPEs and other exceptions for very short non names and other extremes found in occurrences.
   */
  @Test
  public void avoidNPE() throws Exception {
    assertNoName("\\");
    assertNoName(".");
    assertNoName("@");
    assertNoName("&nbsp;");
    assertNoName("X");
    assertNoName("a");
    assertNoName("143");
    assertNoName("321-432");
    assertNoName("-,.#");
    assertNoName(" .");
  }

  @Test
  public void informal() throws Exception {
    assertName("Trisulcus aff. nana  Petrushevskaya, 1971", "Trisulcus nana")
        .species("Trisulcus", "nana")
        .combAuthors("1971", "Petrushevskaya")
        .type(INFORMAL)
        .nothingElse();
  }

  @Test
  public void stringIndexOutOfBoundsException() throws Exception {
    parser.parse("Amblyomma americanum (Linnaeus, 1758)", null);
    parser.parse("Salix taiwanalpina var. chingshuishanensis (S.S.Ying) F.Y.Lu, C.H.Ou, Y.C.Chen, Y.S.Chi, K.C.Lu & Y.H.Tseng ", null);
    parser.parse("Salix taiwanalpina var. chingshuishanensis (S.S.Ying) F.Y.Lu, C.H.Ou, Y.C.Chen, Y.S.Chi, K.C.Lu & amp  Y.H.Tseng ", null);
    parser.parse("Salix morrisonicola var. takasagoalpina (Koidz.) F.Y.Lu, C.H.Ou, Y.C.Chen, Y.S.Chi, K.C.Lu & amp; Y.H.Tseng", null);
    parser.parse("Ficus ernanii Carauta, Pederneir., P.P.Souza, A.F.P.Machado, M.D.M.Vianna & amp; Romaniuc", null);
  }

  @Test
  public void taxonomicNotes() throws Exception {
    assertName("Dyadobacter (Chelius & Triplett, 2000) emend. Reddy & Garcia-Pichel, 2005", "Dyadobacter")
        .monomial("Dyadobacter")
        .basAuthors("2000", "Chelius", "Triplett")
        .sensu("emend. Reddy & Garcia-Pichel, 2005")
        .nothingElse();

    assertName("Thalassiosira praeconvexa Burckle emend Gersonde & Schrader, 1984", "Thalassiosira praeconvexa")
        .species("Thalassiosira", "praeconvexa")
        .combAuthors(null, "Burckle")
        .sensu("emend Gersonde & Schrader, 1984")
        .nothingElse();

    assertName("Amphora gracilis f. exilis Gutwinski according to Hollerback & Krasavina, 1971", "Amphora gracilis f. exilis")
        .infraSpecies("Amphora", "gracilis", Rank.FORM, "exilis")
        .combAuthors(null, "Gutwinski")
        .sensu("according to Hollerback & Krasavina, 1971")
        .nothingElse();


    assertSensu("Trifolium repens sensu Baker f.", "sensu Baker f.");
    assertSensu("Achillea millefolium sensu latu", "sensu latu");
    assertSensu("Achillea millefolium s.str.", "s.str.");
    assertSensu("Achillea millefolium sec. Greuter 2009", "sec. Greuter 2009");
    assertSensu("Globularia cordifolia L. excl. var. (emend. Lam.)", "excl. var. emend. Lam.");

    assertName("Handmannia austriaca f. elliptica Handmann fide Hustedt, 1922", "Handmannia austriaca f. elliptica")
        .infraSpecies("Handmannia", "austriaca", Rank.FORM, "elliptica")
        .combAuthors(null, "Handmann")
        .sensu("fide Hustedt, 1922")
        .nothingElse();
  }

  @Test
  public void nonNames() throws Exception {
    assertName("Nitocris (Nitocris) similis Breuning, 1956 (nec Gahan, 1893)", "Nitocris similis")
        .species("Nitocris", "Nitocris", "similis")
        .combAuthors("1956", "Breuning")
        .sensu("nec Gahan, 1893")
        .nothingElse();

    assertName("Bartlingia Brongn. non Rchb. 1824 nec F.Muell. 1882", "Bartlingia")
        .monomial("Bartlingia")
        .combAuthors(null, "Brongn.")
        .sensu("non Rchb. 1824 nec F.Muell. 1882")
        .nothingElse();

    assertName("Lindera Thunb. non Adans. 1763", "Lindera")
        .monomial("Lindera")
        .combAuthors(null, "Thunb.")
        .sensu("non Adans. 1763")
        .nothingElse();
  }

  @Test
  public void misapplied() throws Exception {
    assertName("Ficus exasperata auct. non Vahl", "Ficus exasperata")
        .species("Ficus", "exasperata")
        .sensu("auct. non Vahl")
        .nothingElse();
  }

  private void assertSensu(String raw, String sensu) throws UnparsableNameException {
    assertEquals(sensu, parser.parse(raw, null).getTaxonomicNote());
  }

  @Test
  public void occNameFile() throws Exception {
    Reader reader = resourceReader("occurrence-names.txt");
    LineIterator iter = new LineIterator(reader);

    int parseFails = 0;
    int lineNum = 0;
    long start = System.currentTimeMillis();

    while (iter.hasNext()) {
      lineNum++;
      String name = iter.nextLine();
      ParsedName n;
      try {
        n = parser.parse(name, null);
      } catch (UnparsableNameException e) {
        parseFails++;
        LOG.warn("FAIL\t " + name);
      }
    }
    long end = System.currentTimeMillis();
    LOG.info("\n\nNames tested: " + lineNum);
    LOG.info("Names parse fail: " + parseFails);
    LOG.info("Total time: " + (end - start));
    LOG.info("Average per name: " + (((double) end - start) / lineNum));

    int currFail = 73;
    if ((parseFails) > currFail) {
      fail("We are getting worse, not better. Currently failing: " + (parseFails) + ". Was passing:" + currFail);
    }
  }

  @Test
  public void viralNames() throws Exception {
    assertTrue(isViralName("Cactus virus 2"));
    assertTrue(isViralName("Vibrio phage 149 (type IV)"));
    assertTrue(isViralName("Cactus virus 2"));
    assertTrue(isViralName("Suid herpesvirus 3 Ictv"));
    assertTrue(isViralName("Tomato yellow leaf curl Mali virus Ictv"));
    assertTrue(isViralName("Not Sapovirus MC10"));
    assertTrue(isViralName("Diolcogaster facetosa bracovirus"));
    assertTrue(isViralName("Human papillomavirus"));
    assertTrue(isViralName("Sapovirus Hu/GI/Nsc, 150/PA/Bra/, 1993"));
    assertTrue(isViralName("Aspergillus mycovirus, 1816"));
    assertTrue(isViralName("Hantavirus sdp2 Yxl-, 2008"));
    assertTrue(isViralName("Norovirus Nizhny Novgorod /, 2461 / Rus /, 2007"));
    assertTrue(isViralName("Carrot carlavirus WM-, 2008"));
    assertTrue(isViralName("C2-like viruses"));
    assertTrue(isViralName("C1 bacteriophage"));
    assertTrue(isViralName("C-terminal Gfp fusion vector pUG23"));
    assertTrue(isViralName("C-terminal Gfp fusion vector"));
    assertTrue(isViralName("CMVd3 Flexi Vector pFN24K (HaloTag 7)"));
    assertTrue(isViralName("bacteriophage, 315.6"));
    assertTrue(isViralName("bacteriophages"));
    assertTrue(isViralName("\"T1-like viruses\""));
    // http://dev.gbif.org/issues/browse/PF-2574
    assertTrue(isViralName("Inachis io NPV"));
    assertTrue(isViralName("Hyloicus pinastri NPV"));
    assertTrue(isViralName("Dictyoploca japonica NPV"));
    assertTrue(isViralName("Apocheima pilosaria NPV"));
    assertTrue(isViralName("Lymantria xylina NPV"));
    assertTrue(isViralName("Feltia subterranea GV"));
    assertTrue(isViralName("Dionychopus amasis GV"));

    assertFalse(isViralName("Forcipomyia flavirustica Remm, 1968"));

    assertName("Crassatellites janus Hedley, 1906", "Crassatellites janus")
        .species("Crassatellites", "janus")
        .combAuthors("1906", "Hedley")
        .nothingElse();

    assertName("Ypsolophus satellitella", "Ypsolophus satellitella")
        .species("Ypsolophus", "satellitella")
        .nothingElse();

    assertName("Nephodia satellites", "Nephodia satellites")
        .species("Nephodia", "satellites")
        .nothingElse();

    Reader reader = resourceReader("viruses.txt");
    LineIterator iter = new LineIterator(reader);
    while (iter.hasNext()) {
      String line = iter.nextLine();
      if (line == null || line.startsWith("#") || line.trim().isEmpty()) {
        continue;
      }
      assertTrue(isViralName(line));
    }
  }

  @Test
  public void apostropheEpithets() throws Exception {
    assertName("Junellia o'donelli Moldenke, 1946", "Junellia o'donelli")
        .species("Junellia", "o'donelli")
        .combAuthors("1946", "Moldenke")
        .nothingElse();

    assertName("Trophon d'orbignyi Carcelles, 1946", "Trophon d'orbignyi")
        .species("Trophon", "d'orbignyi")
        .combAuthors("1946", "Carcelles")
        .nothingElse();

    assertName("Arca m'coyi Tenison-Woods, 1878", "Arca m'coyi")
        .species("Arca", "m'coyi")
        .combAuthors("1878", "Tenison-Woods")
        .nothingElse();

    assertName("Nucula m'andrewii Hanley, 1860", "Nucula m'andrewii")
        .species("Nucula", "m'andrewii")
        .combAuthors("1860", "Hanley")
        .nothingElse();

    assertName("Eristalis l'herminierii Macquart", "Eristalis l'herminierii")
        .species("Eristalis", "l'herminierii")
        .combAuthors(null, "Macquart")
        .nothingElse();

    assertName("Odynerus o'neili Cameron", "Odynerus o'neili")
        .species("Odynerus", "o'neili")
        .combAuthors(null, "Cameron")
        .nothingElse();

    assertName("Serjania meridionalis Cambess. var. o'donelli F.A. Barkley", "Serjania meridionalis var. o'donelli")
        .infraSpecies("Serjania", "meridionalis", Rank.VARIETY, "o'donelli")
        .combAuthors(null, "F.A.Barkley")
        .nothingElse();
  }

  /**
   * http://dev.gbif.org/issues/browse/POR-3069
   */
  @Test
  public void nullNameParts() throws Exception {
    assertName("Austrorhynchus pectatus null pectatus", "Austrorhynchus pectatus pectatus")
        .infraSpecies("Austrorhynchus", "pectatus", Rank.INFRASPECIFIC_NAME, "pectatus")
        .doubtful()
        .nothingElse();

    //assertName("Poa pratensis null proles (L.) Rouy, 1913", "Poa pratensis proles")
    //    .infraSpecies("Poa", "pratensis", Rank.PROLES, "proles")
    //    .basAuthors(null, "L.")
    //    .combAuthors("1913", "Rouy")
    //    .nothingElse();

    // should the infrasubspecific epithet kewensis be removed from the parsed name?
    //assertParsedParts("Poa pratensis kewensis proles", NameType.INFORMAL, "Poa", "pratensis", "kewensis", Rank.PROLES, null);
    //assertParsedParts("Poa pratensis kewensis proles (L.) Rouy, 1913", NameType.INFORMAL, "Poa", "pratensis", null, Rank.PROLES, "Rouy", "1913", "L.", null);
  }



  @Test
  @Ignore
  public void rNANames() throws Exception {
    assertName("Calathus (Lindrothius) KURNAKOV 1961", "Calathus (Lindrothius)")
        .infraGeneric("Calathus", Rank.INFRAGENERIC_NAME, "Lindrothius")
        .combAuthors("1961", "Kurnakov")
        .nothingElse();

    assertTrue(isViralName("Ustilaginoidea virens RNA virus"));
    assertTrue(isViralName("Rhizoctonia solani dsRNA virus 2"));

    assertName("Candida albicans RNA_CTR0-3", "Candida albicans RNA_CTR0-3")
        .species("Candida", "albicans")
        .nothingElse();


    //pn = parser.parse("Alpha proteobacterium RNA12", null);
    //assertEquals("Alpha", pn.getGenusOrAbove());
    //assertEquals("proteobacterium", pn.getSpecificEpithet());
    //assertEquals(NameType.INFORMAL, pn.getType());
    //assertNull(pn.getInfraSpecificEpithet());
    //assertNull(pn.getAuthorship());
//
    //pn = parser.parse("Armillaria ostoyae RNA1", null);
    //assertEquals("Armillaria", pn.getGenusOrAbove());
    //assertEquals("ostoyae", pn.getSpecificEpithet());
    //assertEquals(NameType.INFORMAL, pn.getType());
    //assertNull(pn.getInfraSpecificEpithet());
    //assertNull(pn.getAuthorship());
//
    //assertUnparsableType(NameType.DOUBTFUL, "siRNA");
  }

  @Test
  public void indetNames() throws Exception {
    assertName("Polygonum spec.", "Polygonum spec.")
        .species("Polygonum", null)
        .type(NameType.INFORMAL)
        .nothingElse();

    assertName("Polygonum vulgaris ssp.", "Polygonum vulgaris subsp.")
        .infraSpecies("Polygonum", "vulgaris", Rank.SUBSPECIES, null)
        .type(NameType.INFORMAL)
        .nothingElse();

    assertName("Mesocricetus sp.", "Mesocricetus spec.")
        .species("Mesocricetus", null)
        .type(NameType.INFORMAL)
        .nothingElse();

   // dont treat these authorships as forms
    assertName("Dioscoreales Hooker f.", "Dioscoreales")
        .monomial("Dioscoreales", Rank.ORDER)
        .combAuthors(null, "Hooker f.")
        .nothingElse();

    assertName("Melastoma vacillans Blume var.", "Melastoma vacillans var.")
        .infraSpecies("Melastoma", "vacillans", Rank.VARIETY, null)
        .type(NameType.INFORMAL)
        .nothingElse();

    assertName("Lepidoptera Hooker", Rank.SPECIES, "Lepidoptera spec.")
        .species("Lepidoptera", null)
        .type(NameType.INFORMAL)
        .nothingElse();

    assertName("Lepidoptera alba DC.", Rank.SUBSPECIES, "Lepidoptera alba subsp.")
        .infraSpecies("Lepidoptera", "alba", Rank.SUBSPECIES, null)
        .type(NameType.INFORMAL)
        .nothingElse();
  }

  @Test
  @Ignore
  public void rankMismatch() throws Exception {
    //ParsedName pn = parser.parse("Polygonum", Rank.SUBGENUS);
    //assertEquals("Polygonum", pn.getGenusOrAbove());
    //assertNull(pn.getSpecificEpithet());
    //assertEquals(Rank.SUBGENUS, pn.getRank());
    //assertEquals(NameType.SCIENTIFIC, pn.getType());
//
    //pn = parser.parse("Polygonum", Rank.SUBSPECIES);
    //assertEquals("Polygonum", pn.getGenusOrAbove());
    //assertNull(pn.getSpecificEpithet());
    //assertNull(pn.getAuthorship());
    //assertEquals(Rank.SUBSPECIES, pn.getRank());
    //assertEquals(NameType.INFORMAL, pn.getType());
//
    //pn = parser.parse("Polygonum alba", Rank.GENUS);
    //assertEquals("Polygonum", pn.getGenusOrAbove());
    //assertEquals("alba", pn.getSpecificEpithet());
    //assertNull(pn.getAuthorship());
    //assertEquals(Rank.GENUS, pn.getRank());
    //assertEquals(NameType.DOUBTFUL, pn.getType());
//
    //pn = parser.parse("Polygonum", Rank.CULTIVAR);
    //assertEquals("Polygonum", pn.getGenusOrAbove());
    //assertNull(pn.getSpecificEpithet());
    //assertNull(pn.getAuthorship());
    //assertEquals(Rank.CULTIVAR, pn.getRank());
    //assertEquals(NameType.INFORMAL, pn.getType());
  }

  /**
   * https://github.com/gbif/name-parser/issues/5
   */
  @Test
  public void vulpes() throws Exception {
    assertName("Vulpes vulpes sp. silaceus Miller, 1907", "Vulpes vulpes subsp. silaceus")
        .infraSpecies("Vulpes", "vulpes", Rank.SUBSPECIES, "silaceus")
        .combAuthors("1907", "Miller")
        .nothingElse();
  }

  @Test
  public void microbialRanks2() throws Exception {
    assertName("Puccinia graminis f. sp. avenae", "Puccinia graminis f.sp. avenae")
        .infraSpecies("Puccinia", "graminis", Rank.FORMA_SPECIALIS, "avenae")
        .code(NomCode.BACTERIAL)
        .nothingElse();
  }

  @Test
  public void chineseAuthors() throws Exception {
    assertName("Abaxisotima acuminata (Wang & Liu, 1996)", "Abaxisotima acuminata")
        .species("Abaxisotima", "acuminata")
        .basAuthors("1996", "Wang", "Liu")
        .nothingElse();

    assertName("Abaxisotima acuminata (Wang, Yuwen & Xian-wei Liu, 1996)", "Abaxisotima acuminata")
        .species("Abaxisotima", "acuminata")
        .basAuthors("1996", "Wang", "Yuwen", "Xian-wei Liu")
        .nothingElse();

    assertName("Abaxisotima bicolor (Liu, Xian-wei, Z. Zheng & G. Xi, 1991)", "Abaxisotima bicolor")
        .species("Abaxisotima", "bicolor")
        .basAuthors("1991", "Liu", "Xian-wei", "Z.Zheng", "G.Xi")
        .nothingElse();
  }

  /**
   * http://dev.gbif.org/issues/browse/POR-2454
   */
  @Test
  public void fungusNames() throws Exception {
    assertName("Merulius lacrimans (Wulfen : Fr.) Schum.", "Merulius lacrimans")
        .species("Merulius", "lacrimans")
        .combAuthors(null, "Schum.")
        .basAuthors(null, "Wulfen")
        .nothingElse();

    assertName("Merulius lacrimans (Wulfen) Schum. : Fr.", "Merulius lacrimans")
        .species("Merulius", "lacrimans")
        .combAuthors(null, "Schum.")
        .basAuthors(null, "Wulfen")
        .sanctAuthor("Fr.")
        .nothingElse();

    //assertParsedParts("", null, "Merulius", "lacrimans", null, null, "Schum.", null, "Wulfen : Fr.", null);
    //assertParsedParts("Aecidium berberidis Pers. ex J.F. Gmel.", null, "Aecidium", "berberidis", null, null, "Pers. ex J.F. Gmel.", null, null, null);
    //assertParsedParts("Roestelia penicillata (O.F. Müll.) Fr.", null, "Roestelia", "penicillata", null, null, "Fr.", null, "O.F. Müll.", null);
//
    //assertParsedParts("Mycosphaerella eryngii (Fr. Duby) ex Oudem., 1897", null, "Mycosphaerella", "eryngii", null, null, "ex Oudem.", "1897", "Fr. Duby", null);
    //assertParsedParts("Mycosphaerella eryngii (Fr.ex Duby) ex Oudem. 1897", null, "Mycosphaerella", "eryngii", null, null, "ex Oudem.", "1897", "Fr.ex Duby", null);
    //assertParsedParts("Mycosphaerella eryngii (Fr. ex Duby) Johanson ex Oudem. 1897", null, "Mycosphaerella", "eryngii", null, null, "Johanson ex Oudem.", "1897", "Fr. ex Duby", null);
  }



  @Test
  public void imprintYears() throws Exception {
    assertName("Ophidocampa tapacumae Ehrenberg, 1870, 1869", "Ophidocampa tapacumae")
        .species("Ophidocampa", "tapacumae")
        .combAuthors("1870", "Ehrenberg")
        .nothingElse();

    assertName("Brachyspira Hovind-Hougen, Birch-Andersen, Henrik-Nielsen, Orholm, Pedersen, Teglbjaerg & Thaysen, 1983, 1982", "Brachyspira")
        .monomial("Brachyspira")
        .combAuthors("1983", "Hovind-Hougen", "Birch-Andersen", "Henrik-Nielsen", "Orholm", "Pedersen", "Teglbjaerg", "Thaysen")
        .nothingElse();

    assertName("Gyrosigma angulatum var. gamma Griffith & Henfrey, 1860, 1856", "Gyrosigma angulatum var. gamma")
        .infraSpecies("Gyrosigma", "angulatum", Rank.VARIETY, "gamma")
        .combAuthors("1860", "Griffith", "Henfrey")
        .nothingElse();

    assertName("Ctenotus alacer Storr, 1970 [\"1969\"]", "Ctenotus alacer")
        .species("Ctenotus", "alacer")
        .combAuthors("1970", "Storr")
        .nothingElse();

    assertName("Ctenotus alacer Storr, 1970 (imprint 1969)", "Ctenotus alacer")
        .species("Ctenotus", "alacer")
        .combAuthors("1970", "Storr")
        .nothingElse();

    assertName("Ctenotus alacer Storr, 1887 (\"1886-1888\")", "Ctenotus alacer")
        .species("Ctenotus", "alacer")
        .combAuthors("1887", "Storr")
        .nothingElse();
  }

  @Test
  public void manuscriptNames() throws Exception {
    assertName("Lepidoptera sp. JGP0404", "Lepidoptera spec.")
        .species("Lepidoptera", null)
        .type(INFORMAL)
        .remarks("sp.JGP0404")
        .nothingElse();

    assertName("Genoplesium vernalis D.L.Jones ms.", "Genoplesium vernalis")
        .species("Genoplesium", "vernalis")
        .combAuthors(null, "D.L.Jones")
        .type(INFORMAL)
        .nothingElse();

    assertName("Verticordia sp.1", "Verticordia spec.")
        .species("Verticordia", null)
        .type(INFORMAL)
        .remarks("sp.1")
        .nothingElse();

    assertName("Bryozoan indet. 1", "Bryozoan spec.")
        .species("Bryozoan", null)
        .type(INFORMAL)
        .remarks("indet.1")
        .nothingElse();

    assertName("Bryozoan sp. E", "Bryozoan spec.")
        .species("Bryozoan", null)
        .type(INFORMAL)
        .remarks("sp.E")
        .nothingElse();

    assertName("Prostanthera sp. Somersbey (B.J.Conn 4024)", "Prostanthera spec.")
        .species("Prostanthera", null)
        .type(INFORMAL)
        .remarks("sp.Somersbey(B.J.Conn 4024)")
        .nothingElse();
  }

  // **************
  // HELPER METHODS
  // **************

  public boolean isViralName(String name) {
    try {
      parser.parse(name, null);
    } catch (UnparsableNameException e) {
      // swallow
      if (NameType.VIRUS == e.getType()) {
        return true;
      }
    }
    return false;
  }

  private void assertNoName(String name) {
    assertUnparsable(name, NO_NAME);
  }

  private void assertUnparsable(String name, NameType type) {
    assertUnparsableName(name, type, name);
  }

  private void assertUnparsableName(String name, NameType type, String expectedName) {
    try {
      parser.parse(name);
      fail("Expected "+name+" to be unparsable");

    } catch (UnparsableNameException ex) {
      assertEquals(type, ex.getType());
      assertEquals(expectedName, ex.getName());
    }
  }

  static NameAssertion assertName(String rawName, String expectedCanonicalWithoutAuthors) throws UnparsableNameException {
    return assertName(rawName, null, expectedCanonicalWithoutAuthors);
  }

  static NameAssertion assertName(String rawName, Rank rank, String expectedCanonicalWithoutAuthors) throws UnparsableNameException {
    ParsedName n = parser.parse(rawName, rank);
    assertEquals(expectedCanonicalWithoutAuthors, n.canonicalNameWithoutAuthorship());
    return new NameAssertion(n);
  }

  private BufferedReader resourceReader(String resourceFileName) throws UnsupportedEncodingException {
    return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + resourceFileName), "UTF-8"));
  }

}