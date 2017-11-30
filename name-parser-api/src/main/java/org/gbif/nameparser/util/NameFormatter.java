package org.gbif.nameparser.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.gbif.nameparser.api.*;

import java.util.List;
import java.util.function.Predicate;

/**
 *
 */
public class NameFormatter {
  public static final char HYBRID_MARKER = '×';
  private static final Joiner AUTHORSHIP_JOINER = Joiner.on(", ").skipNulls();

  /**
   * A full scientific name with authorship from the individual properties in its canonical form.
   * Autonyms are rendered without authorship and subspecies are using the subsp rank marker
   * unless a name is assigned to the zoological code.
   */
  public static String canonical(ParsedName n) {
    // TODO: show authorship for zoological autonyms?
    // TODO: how can we best remove subsp from zoological names?
    // https://github.com/gbif/portal-feedback/issues/640
    return buildName(n, true, true, true, true, false, true, false, true, false, false, false, true, true);
  }

  /**
   * A full scientific name just as canonicalName, but without any authorship.
   */
  public static String canonicalWithoutAuthorship(ParsedName n) {
    return buildName(n, true, true, false, true, false, true, false, true, false, false, false, true, true);
  }

  /**
   * A minimal canonical name with nothing else but the 3 main name parts (genus, species, infraspecific).
   * No rank or hybrid markers and no authorship, cultivar or strain information is rendered.
   * Infrageneric names are represented without a leading genus.
   * Unicode characters will be replaced by their matching ASCII characters.
   * <p/>
   * For example:
   * Abies alba
   * Abies alba alpina
   * Bracteata
   */
  public static String canonicalMinimal(ParsedName n) {
    return buildName(n,false, false, false, false, false, true, true, false, false, false, false, false, false);
  }

  /**
   * Assembles a full name with all details including non code compliant, informal remarks.
   */
  public static String canonicalComplete(ParsedName n) {
    return buildName(n,true, true, true, true, true, true, false, true, true, true, true, true, true);
  }

  /**
   * The full concatenated authorship for parsed names including the sanctioning author.
   */
  public static String authorshipComplete(ParsedName n) {
    StringBuilder sb = new StringBuilder();
    appendAuthorship(n, sb);
    return sb.length() == 0 ? null : sb.toString();
  }

  /**
   * Renders the authors of an authorship including ex authors, optionally with the year included.
   */
  public static String authorString(Authorship authors, boolean inclYear) {
    StringBuilder sb = new StringBuilder();
    appendAuthorship(sb, authors, inclYear);
    return sb.length() == 0 ? null : sb.toString();
  }

  /**
   * build a name controlling all available flags for name parts to be included in the resulting name.
   *
   * @param hybridMarker    include the hybrid marker with the name if existing
   * @param rankMarker      include the infraspecific or infrageneric rank marker with the name if existing
   * @param authorship      include the names authorship (authorteam and year)
   * @param genusForinfrageneric show the genus for infrageneric names
   * @param infrageneric    include the infrageneric name in brackets for species or infraspecies
   * @param decomposition   decompose unicode ligatures into their corresponding ascii ones, e.g. æ beomes ae
   * @param asciiOnly       transform unicode letters into their corresponding ascii ones, e.g. ø beomes o and ü u
   * @param showIndet       if true include the rank marker for incomplete determinations, for example Puma spec.
   * @param nomNote         include nomenclatural notes
   * @param remarks         include informal remarks
   */
  public static String buildName(ParsedName n,
      boolean hybridMarker,
      boolean rankMarker,
      boolean authorship,
      boolean genusForinfrageneric,
      boolean infrageneric,
      boolean decomposition,
      boolean asciiOnly,
      boolean showIndet,
      boolean nomNote,
      boolean remarks,
      boolean showSensu,
      boolean showCultivar,
      boolean showStrain
  ) {
    StringBuilder sb = new StringBuilder();

    if (n.isCandidatus()) {
      sb.append("\"Candidatus ");
    }

    if (n.getUninomial() != null) {
      // higher rank names being just a uninomial!
      if (hybridMarker && NamePart.GENERIC == n.getNotho()) {
        sb.append(HYBRID_MARKER)
          .append(" ");
      }
      sb.append(n.getUninomial());

    } else {
      // bi- or trinomials or infrageneric names
      if (n.getInfragenericEpithet() != null) {
        if (n.getSpecificEpithet() == null) {
          // the infrageneric is the terminal rank. Always show it and wrap it with its genus if requested
          if (n.getGenus() != null && genusForinfrageneric) {
            appendGenus(sb, n, hybridMarker);
            sb.append(" ");
            // we show zoological infragenerics in brackets,
            // but use rank markers for botanical names (unless its no defined rank)
            if (NomCode.ZOOLOGICAL == n.getCode()) {
              sb.append("(")
                  .append(n.getInfragenericEpithet())
                  .append(")");

            } else {
              if (rankMarker && n.getRank() != null) {
                // If we know the rank we use explicit rank markers
                // this is how botanical infrageneric names are formed, see http://www.iapt-taxon.org/nomen/main.php?page=art21
                appendRankMarker(sb, n.getRank());
              }
              sb.append(n.getInfragenericEpithet());
            }

          } else {
            // just show the infragen
            sb.append(n.getInfragenericEpithet());
          }

        } else if (infrageneric) {
          // additional subgenus shown for binomial. Always shown in brackets
          sb.append(" (")
            .append(n.getInfragenericEpithet())
            .append(")");
        }

      } else if (n.getGenus() != null) {
        appendGenus(sb, n, hybridMarker);
      }

      if (n.getSpecificEpithet() == null) {
        if (showIndet) {
          if (Rank.SPECIES == n.getRank()) {
            // no species epithet given, but rank=species. Indetermined species!
            sb.append(" spec.");
            authorship = false;

          } else if (n.getRank() != null && n.getRank().isInfraspecific()) {
            // no species epithet given, but rank below species. Indetermined!
            sb.append(' ');
            sb.append(n.getRank().getMarker());
            authorship = false;
          }
        }

      } else {
        // species part
        sb.append(' ');
        if (hybridMarker && NamePart.SPECIFIC == n.getNotho()) {
          sb.append(HYBRID_MARKER)
            .append(" ");
        }
        String epi = n.getSpecificEpithet().replaceAll("[ _-]", "-");
        sb.append(epi);

        if (n.getInfraspecificEpithet() == null) {
          // Indetermined infraspecies? Only show indet cultivar marker if no cultivar epithet exists
          if (showIndet && n.getRank() != null && n.getRank().isInfraspecific() && (Rank.CULTIVAR != n.getRank() || n.getCultivarEpithet() == null)) {
            // no infraspecific epitheton given, but rank below species. Indetermined!
            sb.append(' ')
              .append(n.getRank().getMarker());
            authorship = false;
          }

        } else {
          // infraspecific part
          sb.append(' ');
          if (hybridMarker && NamePart.INFRASPECIFIC == n.getNotho()) {
            if (rankMarker && n.getRank() != null && isInfraspecificMarker(n.getRank())) {
              sb.append("notho");
            } else {
              sb.append(HYBRID_MARKER);
              sb.append(" ");
            }
          }
          // hide subsp. from zoological names
          if (rankMarker && (!isZoo(n.getCode()) || Rank.SUBSPECIES != n.getRank())) {
            appendRankMarker(sb, n.getRank(), NameFormatter::isInfraspecificMarker);
          }
          epi = n.getInfraspecificEpithet().replaceAll("[ _-]", "-");
          sb.append(epi);
          // non autonym authorship ?
          if (n.isAutonym()) {
            authorship = false;
          }
        }
      }
    }

    // closing quotes for Candidatus names
    if (n.isCandidatus()) {
      sb.append("\"");
    }

    // uninomial, genus, infragen, species or infraspecies authorship
    if (authorship && n.hasAuthorship()) {
      sb.append(" ");
      appendAuthorship(n, sb);
    }

    // add cultivar name
    if (showStrain && n.getStrain() != null) {
      sb.append(" ");
      sb.append(n.getStrain());
    }

    // add cultivar name
    if (showCultivar && n.getCultivarEpithet() != null) {
      sb.append(" '");
      sb.append(n.getCultivarEpithet());
      sb.append("'");
    }

    // add sensu/sec reference
    if (showSensu && n.getSensu() != null) {
      sb.append(" ");
      sb.append(n.getSensu());
    }

    // add nom status
    if (nomNote && n.getNomenclaturalNotes() != null) {
      sb.append(", ");
      sb.append(n.getNomenclaturalNotes());
    }

    // add remarks
    if (remarks && n.getRemarks() != null) {
      sb.append(" [");
      sb.append(n.getRemarks());
      sb.append("]");
    }

    // final char transformations
    String name = sb.toString().trim();
    if (decomposition) {
      name = UnicodeUtils.decompose(name);
    }
    if (asciiOnly) {
      name = UnicodeUtils.ascii(name);
    }
    return Strings.emptyToNull(name);
  }

  private static boolean isZoo(NomCode code) {
    return code != null && code == NomCode.ZOOLOGICAL;
  }

  private static boolean isInfragenericMarker(Rank r) {
    return r != null && r.isInfrageneric() && !r.isUncomparable();
  }

  private static boolean isInfraspecificMarker(Rank r) {
    return r.isInfraspecific() && !r.isUncomparable();
  }

  private static void appendRankMarker(StringBuilder sb, Rank rank) {
    appendRankMarker(sb, rank, null);
  }

  private static void appendRankMarker(StringBuilder sb, Rank rank, Predicate<Rank> ifRank) {
    if (rank != null
        && rank.getMarker() != null
        && (ifRank == null || ifRank.test(rank))
      ) {
      sb.append(rank.getMarker());
      sb.append(' ');
    }
  }

  private static void appendGenus(StringBuilder sb, ParsedName n, boolean hybridMarker) {
    if (hybridMarker && NamePart.GENERIC == n.getNotho()) {
      sb.append(HYBRID_MARKER)
          .append(" ");
    }
    sb.append(n.getGenus());
  }

  private static String joinAuthors(List<String> authors, boolean useEtAl) {
    if (useEtAl && authors.size() > 2) {
      return AUTHORSHIP_JOINER.join(authors.subList(0, 1)) + " et al.";

    } else if (authors.size() > 1) {
      return AUTHORSHIP_JOINER.join(authors.subList(0, authors.size()-1)) + " & " + authors.get(authors.size() - 1);

    } else {
      return AUTHORSHIP_JOINER.join(authors);
    }
  }

  /**
   * Renders the authorship with ex authors and year
   * @param sb StringBuilder to append to
   */
  public static void appendAuthorship(StringBuilder sb, Authorship auth, boolean includeYear) {
    if (auth.exists()) {
      boolean authorsAppended = false;
      if (!auth.getExAuthors().isEmpty()) {
        sb.append(joinAuthors(auth.getExAuthors(), false));
        sb.append(" ex ");
        authorsAppended = true;
      }
      if (!auth.getAuthors().isEmpty()) {
        sb.append(joinAuthors(auth.getAuthors(), false));
        authorsAppended = true;
      }
      if (auth.getYear() != null && includeYear) {
        if (authorsAppended) {
          sb.append(", ");
        }
        sb.append(auth.getYear());
      }
    }
  }

  private static void appendAuthorship(ParsedName n, StringBuilder sb) {
    if (n.getBasionymAuthorship().exists()) {
      sb.append("(");
      appendAuthorship(sb, n.getBasionymAuthorship(), true);
      sb.append(") ");
    }
    if (n.getAuthorship().exists()) {
      appendAuthorship(sb, n.getAuthorship(), true);
      // Render sanctioning author via colon:
      // http://www.iapt-taxon.org/nomen/main.php?page=r50E
      //TODO: remove rendering of sanctioning author according to Paul Kirk!
      if (n.getSanctioningAuthor() != null) {
        sb.append(" : ");
        sb.append(n.getSanctioningAuthor());
      }
    }
  }

}
