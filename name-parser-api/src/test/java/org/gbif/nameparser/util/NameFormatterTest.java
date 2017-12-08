package org.gbif.nameparser.util;

import org.gbif.nameparser.api.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class NameFormatterTest {
  NameFormatter formatter = new NameFormatter();
  ParsedName pn;

  @Before
  public void init() {
    pn = new ParsedName();
  }


  @Test
  public void testFullAuthorship() throws Exception {
    assertNull(pn.authorshipComplete());

    pn.getCombinationAuthorship().getAuthors().add("L.");
    assertEquals("L.", pn.authorshipComplete());

    pn.getBasionymAuthorship().getAuthors().add("Bassier");
    assertEquals("(Bassier) L.", pn.authorshipComplete());
    assertEquals("(Bassier) L.", pn.authorshipComplete());

    pn.getCombinationAuthorship().getAuthors().add("Rohe");
    assertEquals("(Bassier) L. & Rohe", pn.authorshipComplete());
    assertEquals("(Bassier) L. & Rohe", pn.authorshipComplete());

    pn.setSanctioningAuthor("Fr.");
    assertEquals("(Bassier) L. & Rohe : Fr.", pn.authorshipComplete());
  }

  @Test
  public void testFullAuthorshipSanctioning() throws Exception {
    pn.getCombinationAuthorship().getAuthors().add("L.");
    pn.setSanctioningAuthor("Pers.");
    assertEquals("L. : Pers.", pn.authorshipComplete());
  }

  @Test
  public void testCandidatus() throws Exception {
    pn.setUninomial("Endowatersipora");
    pn.setCandidatus(true);
    assertEquals("\"Candidatus Endowatersipora\"", pn.canonicalName());

    pn.setUninomial(null);
    pn.setGenus("Protochlamydia");
    pn.setSpecificEpithet("amoebophila");
    pn.setCandidatus(true);
    pn.getCombinationAuthorship().getAuthors().add("Collingro");
    pn.getCombinationAuthorship().setYear("2005");
    assertEquals("\"Candidatus Protochlamydia amoebophila\" Collingro, 2005", pn.canonicalName());
  }

  @Test
  @Ignore("currently expected to be handled externally")
  public void testCanonicalAscii() throws Exception {
    pn.setGenus("Abies");
    pn.setSpecificEpithet("vülgårîs");
    pn.setInfraspecificEpithet("æbiéñtø");

    assertName("Abies vulgaris aebiento", "Abies vülgårîs aebiéñtø");
  }

  @Test
  public void testUnparsableCanonical() throws Exception {
    pn.setType(NameType.PLACEHOLDER);
    pn.setParsed(false);
    assertNameNull();
  }

  @Test
  public void testUninomials() throws Exception {
    pn.setUninomial("Abies");
    assertEquals("Abies", pn.canonicalName());

    pn.getCombinationAuthorship().setYear("1877");
    assertEquals("Abies 1877", pn.canonicalName());

    pn.getCombinationAuthorship().getAuthors().add("Mill.");
    assertEquals("Abies Mill., 1877", pn.canonicalName());

    pn.setNotho(NamePart.GENERIC);
    assertEquals("× Abies Mill., 1877", pn.canonicalName());
  }

  @Test
  public void testOTU() throws Exception {
    pn.setUninomial("BOLD:AAA0001");
    assertEquals("BOLD:AAA0001", pn.canonicalName());
    assertEquals("BOLD:AAA0001", pn.canonicalNameComplete());

    pn.setType(NameType.OTU);
    pn.setRank(Rank.SPECIES);
    assertEquals("BOLD:AAA0001", pn.canonicalName());
    assertEquals("BOLD:AAA0001", pn.canonicalNameComplete());
  }

  @Test
  public void testCanonicalNames() throws Exception {
    pn.setGenus("Abies");
    assertEquals("Abies", pn.canonicalName());

    pn.setSpecificEpithet("alba");
    assertEquals("Abies alba", pn.canonicalName());

    pn = new ParsedName();
    pn.setGenus("Abies");
    pn.setSpecificEpithet("alba");
    pn.setRank(Rank.VARIETY);
    pn.getCombinationAuthorship().getAuthors().add("Mill.");
    pn.getCombinationAuthorship().setYear("1887");
    pn.getBasionymAuthorship().getAuthors().add("Carl.");
    pn.setNotho(NamePart.GENERIC);
    pn.setInfraspecificEpithet("alpina");
    pn.setSensu("Döring");
    pn.setRemarks("lost");
    pn.setNomenclaturalNotes("nom. illeg.");

    assertEquals("Abies alba alpina", NameFormatter.canonicalMinimal(pn));
    assertEquals("× Abies alba var. alpina", NameFormatter.canonicalWithoutAuthorship(pn));
    assertEquals("× Abies alba var. alpina (Carl.) Mill., 1887", NameFormatter.canonical(pn));
    assertEquals("× Abies alba var. alpina (Carl.) Mill., 1887 Döring, nom. illeg. [lost]", NameFormatter.canonicalComplete(pn));
  }

  @Test
  public void testCanonicalName() throws Exception {
    assertNull(pn.canonicalName());

    pn.setUninomial("Asteraceae");
    pn.setRank(Rank.FAMILY);
    assertEquals("Asteraceae", pn.canonicalName());

    pn.setUninomial("Abies");
    pn.setRank(Rank.GENUS);
    pn.getCombinationAuthorship().getAuthors().add("Mill.");
    assertEquals("Abies Mill.", pn.canonicalName());

    pn.setRank(Rank.UNRANKED);
    assertEquals("Abies Mill.", pn.canonicalName());

    pn.setUninomial(null);
    pn.setInfragenericEpithet("Pinoideae");
    assertEquals("Pinoideae Mill.", pn.canonicalName());

    pn.setGenus("Abies");
    pn.setRank(Rank.INFRAGENERIC_NAME);
    assertEquals("Abies infragen. Pinoideae Mill.", pn.canonicalName());

    pn.setRank(Rank.SUBGENUS);
    assertEquals("Abies subgen. Pinoideae Mill.", pn.canonicalName());

    pn.setCode(NomCode.ZOOLOGICAL);
    assertEquals("Abies (Pinoideae) Mill.", pn.canonicalName());

    pn.setInfragenericEpithet(null);
    pn.setSpecificEpithet("alba");
    assertEquals("Abies alba Mill.", pn.canonicalName());

    pn.setRank(Rank.SPECIES);
    assertEquals("Abies alba Mill.", pn.canonicalName());

    pn.setInfraspecificEpithet("alpina");
    assertEquals("Abies alba alpina Mill.", pn.canonicalName());

    pn.setRank(Rank.SUBSPECIES);
    assertEquals("Abies alba alpina Mill.", pn.canonicalName());

    pn.setRank(Rank.VARIETY);
    assertEquals("Abies alba var. alpina Mill.", pn.canonicalName());

    pn.setCode(NomCode.BOTANICAL);
    pn.setRank(Rank.SUBSPECIES);
    assertEquals("Abies alba subsp. alpina Mill.", pn.canonicalName());

    pn.setRank(Rank.VARIETY);
    assertEquals("Abies alba var. alpina Mill.", pn.canonicalName());

    pn.setRank(Rank.INFRASPECIFIC_NAME);
    assertEquals("Abies alba alpina Mill.", pn.canonicalName());

    pn.setNotho(NamePart.INFRASPECIFIC);
    assertEquals("Abies alba × alpina Mill.", pn.canonicalName());

    pn.setNotho(NamePart.GENERIC);
    assertEquals("× Abies alba alpina Mill.", pn.canonicalName());
  }

  /**
   * http://dev.gbif.org/issues/browse/POR-2624
   */
  @Test
  public void testSubgenus() throws Exception {
    // Brachyhypopomus (Odontohypopomus) Sullivan, Zuanon & Cox Fernandes, 2013
    pn.setGenus("Brachyhypopomus");
    pn.setInfragenericEpithet("Odontohypopomus");
    pn.setCombinationAuthorship(authorship("2013","Sullivan", "Zuanon", "Cox Fernandes"));
    assertName(
        "Odontohypopomus",
        "Brachyhypopomus Odontohypopomus Sullivan, Zuanon & Cox Fernandes, 2013"
    );

    pn.setRank(Rank.INFRAGENERIC_NAME);
    assertName(
        "Odontohypopomus",
        "Brachyhypopomus infragen. Odontohypopomus Sullivan, Zuanon & Cox Fernandes, 2013"
    );

    // with given rank marker it is shown instead of brackets
    pn.setRank(Rank.SUBGENUS);
    assertName(
        "Odontohypopomus",
        "Brachyhypopomus subgen. Odontohypopomus Sullivan, Zuanon & Cox Fernandes, 2013"
    );

    // but not for zoological names
    pn.setCode(NomCode.ZOOLOGICAL);
    assertName(
        "Odontohypopomus",
        "Brachyhypopomus (Odontohypopomus) Sullivan, Zuanon & Cox Fernandes, 2013"
    );

    // Achillea sect. Ptarmica (Mill.) W.D.J.Koch
    pn = new ParsedName();
    pn.setCode(NomCode.BOTANICAL);
    pn.setGenus("Achillea");
    pn.setInfragenericEpithet("Ptarmica");
    pn.getCombinationAuthorship().getAuthors().add("W.D.J.Koch");
    pn.getBasionymAuthorship().getAuthors().add("Mill.");
    assertName(
        "Ptarmica",
        "Achillea Ptarmica (Mill.) W.D.J.Koch"
    );

    pn.setRank(Rank.SECTION);
    assertName(
        "Ptarmica",
        "Achillea sect. Ptarmica (Mill.) W.D.J.Koch"
    );
  }

  @Test
  public void testBuildName() throws Exception {
    pn.setUninomial("Pseudomonas");
    assertName("Pseudomonas");
    
    pn.setUninomial(null);
    pn.setGenus("Pseudomonas");
    pn.setSpecificEpithet("syringae");
    assertName("Pseudomonas syringae");

    pn.getCombinationAuthorship().getAuthors().add("Van Hall");
    assertName("Pseudomonas syringae", "Pseudomonas syringae Van Hall");

    pn.getCombinationAuthorship().setYear("1904");
    assertName("Pseudomonas syringae", "Pseudomonas syringae Van Hall, 1904");

    pn.getBasionymAuthorship().getAuthors().add("Carl.");
    assertName("Pseudomonas syringae", "Pseudomonas syringae (Carl.) Van Hall, 1904");

    pn.setRank(Rank.PATHOVAR);
    pn.setInfraspecificEpithet("aceris");
    pn.getBasionymAuthorship().getAuthors().clear();
    assertName("Pseudomonas syringae aceris", "Pseudomonas syringae pv. aceris Van Hall, 1904");

    pn.setStrain("CFBP 2339");
    assertName("Pseudomonas syringae aceris", "Pseudomonas syringae pv. aceris Van Hall, 1904 CFBP 2339");

    pn.getCombinationAuthorship().setYear(null);
    pn.getCombinationAuthorship().getAuthors().clear();
    assertName("Pseudomonas syringae aceris", "Pseudomonas syringae pv. aceris CFBP 2339");


    pn = new ParsedName();
    pn.setGenus("Abax");
    pn.setSpecificEpithet("carinatus");
    pn.setInfraspecificEpithet("carinatus");
    pn.getBasionymAuthorship().getAuthors().add("Duftschmid");
    pn.getBasionymAuthorship().setYear("1812");
    pn.setRank(Rank.UNRANKED);
    assertName("Abax carinatus carinatus");

    pn.setRank(null);
    assertName("Abax carinatus carinatus");

    pn.setInfraspecificEpithet("urinatus");
    assertName("Abax carinatus urinatus", "Abax carinatus urinatus (Duftschmid, 1812)");

    pn.setRank(null);
    assertName("Abax carinatus urinatus", "Abax carinatus urinatus (Duftschmid, 1812)");

    pn.setRank(Rank.SUBSPECIES);
    assertName("Abax carinatus urinatus", "Abax carinatus subsp. urinatus (Duftschmid, 1812)");

    pn.setCode(NomCode.ZOOLOGICAL);
    assertName("Abax carinatus urinatus", "Abax carinatus urinatus (Duftschmid, 1812)");


    pn = new ParsedName();
    pn.setGenus("Polypodium");
    pn.setSpecificEpithet("vulgare");
    pn.setInfraspecificEpithet("mantoniae");
    pn.getBasionymAuthorship().getAuthors().add("Rothm.");
    pn.getCombinationAuthorship().getAuthors().add("Schidlay");
    pn.setRank(Rank.SUBSPECIES);
    pn.setNotho(NamePart.INFRASPECIFIC);
    assertName(
        "Polypodium vulgare mantoniae",
        "Polypodium vulgare nothosubsp. mantoniae (Rothm.) Schidlay",
        "Polypodium vulgare nothosubsp. mantoniae (Rothm.) Schidlay");
  }



  private Authorship authorship(String year, String ... authors){
    Authorship a = new Authorship();
    a.setYear(year);
    for (String au : authors) {
      a.getAuthors().add(au);
    }
    return a;
  }

  private void assertNameNull() {
    assertName(null, null, null);
  }

  /**
   * assert all build name methods return the same string
   */
  private void assertName(String name) {
    assertName(name, name, name);
  }

  /**
   * assert a minimal trinomen and a canonical & complete name being the same string
   */
  private void assertName(String trinomen, String canonical) {
    assertName(trinomen, canonical, canonical);
  }

  private void assertName(String trinomen, String canonical, String complete) {
    assertEquals("wrong trinomen", trinomen, formatter.canonicalMinimal(pn));
    assertEquals("wrong canonical", canonical, formatter.canonical(pn));
    assertEquals("wrong canonicalComplete", complete, formatter.canonicalComplete(pn));
  }
}