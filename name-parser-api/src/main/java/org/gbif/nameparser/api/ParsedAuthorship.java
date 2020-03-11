package org.gbif.nameparser.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.gbif.nameparser.util.NameFormatter;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public class ParsedAuthorship {

  /**
   * Authorship with years of the name, but excluding any basionym authorship.
   * For binomials the combination authors.
   */
  private Authorship combinationAuthorship = new Authorship();
  
  /**
   * Basionym authorship with years of the name
   */
  private Authorship basionymAuthorship = new Authorship();
  
  /**
   * The sanctioning author for sanctioned fungal names.
   * Fr. or Pers.
   */
  private String sanctioningAuthor;

  /**
   * Taxonomic concept remarks of the name.
   * For example sensu Miller, sec. Pyle 2007, s.l., etc.
   */
  private String taxonomicNote;
  
  /**
   * Nomenclatural status remarks of the name.
   */
  private String nomenclaturalNote;
  
  /**
   * Any additional unparsed string found at the end of the name.
   * Only ever set when state=PARTIAL
   */
  private String unparsed;

  /**
   * Indicates some doubts that this is a name of the given type.
   * Usually indicates the existance of unusual characters not normally found in scientific names.
   */
  private boolean doubtful;
  
  /**
   * Indicates a manuscript name identified by ined. or ms.
   * Can be either of type scientific name or informal
   */
  private boolean manuscript;

  private ParsedName.State state = ParsedName.State.NONE;
  
  private List<String> warnings = Lists.newArrayList();

  /**
   * Copies all values from the given parsed authorship
   */
  public void copy(ParsedAuthorship pa) {
    combinationAuthorship = pa.combinationAuthorship;
    basionymAuthorship = pa.basionymAuthorship;
    sanctioningAuthor = pa.sanctioningAuthor;
    taxonomicNote = pa.taxonomicNote;
    nomenclaturalNote = pa.nomenclaturalNote;
    unparsed = pa.unparsed;
    doubtful = pa.doubtful;
    manuscript = pa.manuscript;
    state = pa.state;
    warnings = pa.warnings;
  }

  public Authorship getCombinationAuthorship() {
    return combinationAuthorship;
  }
  
  public void setCombinationAuthorship(Authorship combinationAuthorship) {
    this.combinationAuthorship = combinationAuthorship;
  }
  
  public Authorship getBasionymAuthorship() {
    return basionymAuthorship;
  }
  
  public void setBasionymAuthorship(Authorship basionymAuthorship) {
    this.basionymAuthorship = basionymAuthorship;
  }
  
  public String getSanctioningAuthor() {
    return sanctioningAuthor;
  }
  
  public void setSanctioningAuthor(String sanctioningAuthor) {
    this.sanctioningAuthor = sanctioningAuthor;
  }

  public String getNomenclaturalNote() {
    return nomenclaturalNote;
  }
  
  public void setNomenclaturalNote(String nomenclaturalNote) {
    this.nomenclaturalNote = nomenclaturalNote;
  }
  
  public void addNomenclaturalNote(String note) {
    if (!StringUtils.isBlank(note)) {
      this.nomenclaturalNote = nomenclaturalNote == null ? note.trim() : nomenclaturalNote + " " + note.trim();
    }
  }
  
  public String getTaxonomicNote() {
    return taxonomicNote;
  }
  
  public void setTaxonomicNote(String taxonomicNote) {
    this.taxonomicNote = taxonomicNote;
  }
  
  public boolean isManuscript() {
    return manuscript;
  }
  
  public void setManuscript(boolean manuscript) {
    this.manuscript = manuscript;
  }
  
  public String getUnparsed() {
    return unparsed;
  }
  
  public void setUnparsed(String unparsed) {
    this.unparsed = unparsed;
  }
  
  public void addUnparsed(String unparsed) {
    if (!StringUtils.isBlank(unparsed)) {
      this.unparsed = this.unparsed == null ? unparsed : this.unparsed + unparsed;
    }
  }

  public ParsedName.State getState() {
    return state;
  }
  
  public void setState(ParsedName.State state) {
    this.state = state;
  }

  public boolean isDoubtful() {
    return doubtful;
  }
  
  public void setDoubtful(boolean doubtful) {
    this.doubtful = doubtful;
  }
  
  public List<String> getWarnings() {
    return warnings;
  }
  
  public void addWarning(String... warnings) {
    for (String warn : warnings) {
      this.warnings.add(warn);
    }
  }

  /**
   * @return true if any kind of authorship exists
   */
  public boolean hasAuthorship() {
    return combinationAuthorship.exists() || basionymAuthorship.exists();
  }

  /**
   * @See NameFormatter.authorshipComplete()
   */
  public String authorshipComplete() {
    return NameFormatter.authorshipComplete(this);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ParsedAuthorship that = (ParsedAuthorship) o;
    return doubtful == that.doubtful &&
        state == that.state &&
        Objects.equals(combinationAuthorship, that.combinationAuthorship) &&
        Objects.equals(basionymAuthorship, that.basionymAuthorship) &&
        Objects.equals(sanctioningAuthor, that.sanctioningAuthor) &&
        Objects.equals(taxonomicNote, that.taxonomicNote) &&
        Objects.equals(nomenclaturalNote, that.nomenclaturalNote) &&
        Objects.equals(unparsed, that.unparsed) &&
        manuscript == that.manuscript &&
        Objects.equals(warnings, that.warnings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(combinationAuthorship, basionymAuthorship, sanctioningAuthor, taxonomicNote, nomenclaturalNote, unparsed,
        doubtful, manuscript, state, warnings);
  }
  
  @Override
  public String toString() {
    return authorshipComplete();
  }
}
