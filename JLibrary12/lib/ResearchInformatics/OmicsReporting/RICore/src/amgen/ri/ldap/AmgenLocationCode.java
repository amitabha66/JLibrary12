package amgen.ri.ldap;

import java.util.TimeZone;

public enum AmgenLocationCode {

  AUAP, AUBB, AUBR, AUGF, AUME, AUPE, AUSY, ATVI, AUVI,
  BEBR, BRBR, CABN, CAMI, CAON, CNHK, CNSH, CZPR, DEHE, EETA,
  FNLD, FRPA, GEMU, GERB, GRAT, HUBU, INMU, IECO, IEDU, IEEG,
  ITMI, JPSA, JPTO, LVRI, LTVI, MXMC, NEBR, NETB, AUNZ,
  NOOS, PLEP, PLWA, POLI, RUMO, SGSG, SKBR, SKPI, SLLJ,
  SPBA, SESO, SWEK, SWLS, SWLU, AEDU, UKCA, UKUX, PRJU,
  USAM, USBO, USBT, USCH, USDC, USFM, USLO, USMA, USMV, USOH, USON,
  USPA, USPH, USRI, USRW, USSC, USSD, USSF, USTO, USWA, USFW;

  public static String getLocationName(String code) {
    return getLocationName(AmgenLocationCode.valueOf(code));
  }

  public static String getLocationName(AmgenLocationCode code) {
    switch (code) {
      case AUAP:
        return "Amgen Australia Pty Ltd (Adelaide)";
      case AUBB:
        return "Amgen Australia Pty Ltd (Milton/Brisbane)";
      case AUBR:
        return "Amgen Australia Pty Ltd (Brisbane)";
      case AUGF:
        return "Amgen Australia (Griffith)";
      case AUME:
        return "Amgen Australia Pty. Ltd. (Melbourne)";
      case AUPE:
        return "Amgen Australia Pty Ltd (Perth)";
      case AUSY:
        return "Amgen Australia Pty. Ltd. (Sydney)";
      case ATVI:
        return "Amgen CEE Headquarters (Vienna)";
      case AUVI:
        return "Amgen GmbH (Vienna)";
      case BEBR:
        return "Amgen Belgium S.A./N.V. (Brussels)";
      case BRBR:
        return "Amgen Brazil (Brasilia)";
      case CABN:
        return "Amgen British Columbia (Burnaby)";
      case CAMI:
        return "Amgen Canada Inc. (Mississauga)";
      case CAON:
        return "Amgen Canada Inc. (Ottawa)";
      case CNHK:
        return "Amgen Hong Kong";
      case CNSH:
        return "Amgen China (Shanghai)";
      case CZPR:
        return "Amgen s.r.o. (Prague)";
      case DEHE:
        return "Amgen AB (Denmark)";
      case EETA:
        return "Amgen Estonia (Tallinn)";
      case FNLD:
        return "Amgen AB (Finland)";
      case FRPA:
        return "Amgen S.A.S. (Paris)";
      case GEMU:
        return "Amgen GmbH (Munich)";
      case GERB:
        return "Amgen Regensburg, GR Office";
      case GRAT:
        return "Amgen Hellas EPE (Athens)";
      case HUBU:
        return "Amgen kft. (Budapest)";
      case INMU:
        return "Amgen India (Mumbai)";
      case IECO:
        return "Amgen Technology (Ireland) Limited";
      case IEDU:
        return "Amgen IE (Sales Branch Office in Dublin, Ireland))";
      case IEEG:
        return "Amgen Technology (Ireland) Limited - Eastgate Office";
      case ITMI:
        return "Amgen S.p.A. (Milan)";
      case JPSA:
        return "Amgen Kitamoto CMC Labs - Amgen Limited (Saitama, JP)";
      case JPTO:
        return "Amgen K.K. (Tokyo Sapia Tower - Chiyoda-ku, JP)";
      case LVRI:
        return "Amgen Latvia (Riga)";
      case LTVI:
        return "Amgen Lithuania (Vilnius)";
      case MXMC:
        return "Amgen Mexico (Mexico City)";
      case NEBR:
        return "Amgen B.V. (Sales Office in Breda)";
      case NETB:
        return "Amgen Europe B.V. (ELC in Breda)";
      case AUNZ:
        return "Amgen New Zealand Pty Ltd";
      case NOOS:
        return "Amgen AB (Norway)";
      case PLEP:
        return "Amgen @ Emilii Plater 53 in Warsaw";
      case PLWA:
        return "Amgen sp.z.o.o. (Warsaw)";
      case POLI:
        return "Amgen - Biofarmaceutica, Lda (Portugal)";
      case RUMO:
        return "Amgen Russia (Moscow)";
      case SGSG:
        return "Amgen Singapore";
      case SKBR:
        return "Amgen Slovak Republic (Bratislava)";
      case SKPI:
        return "Amgen Slovakia (Piestany)";
      case SLLJ:
        return "Amgen Slovenia";
      case SPBA:
        return "Amgen SA (Barcelona, Spain)";
      case SESO:
        return "Amgen AB (Stockholm, Sweden)";
      case SWEK:
        return "Amgen AB (Sweden- Nordic Office)";
      case SWLS:
        return "Amgen Switzerland AG (Sales Office in Zug)";
      case SWLU:
        return "Amgen (Europe) GmbH (Zug)";
      case AEDU:
        return "AMGEN Middle East & Africa FZ LLC (Dubai, U.A.E.)";
      case UKCA:
        return "Amgen Limited (Cambridge, UK)";
      case UKUX:
        return "Amgen London, UK Office (Uxbridge)";
      case PRJU:
        return "Amgen Manufacturing, Limited (Puerto Rico)";
      case USAM:
        return "Amgen Inc. (Cambridge, MA)";
      case USBO:
        return "Amgen Inc. (Colorado - Boulder & Longmont)";
      case USBT:
        return "Biostatistics Workers";
      case USCH:
        return "Amgen Great Lakes Regional Sales Force (Chicago)";
      case USDC:
        return "Amgen Inc. Global Government Affairs in D.C.";
      case USFM:
        return "Amgen Fremont";
      case USLO:
        return "Amgen Inc. (Distribution Center in Louisville)";
      case USMA:
        return "Amgen Southeast Regional Sales Force (Atlanta)";
      case USMV:
        return "Amgen Inc. (Mountain View)";
      case USOH:
        return "Amgen Partner Site at Jacob's Engineering";
      case USPA:
        return "Amgen Partner Site @ Jacob's Engineering (Pennsylvania)";
      case USPH:
        return "Amgen NE Regional Sales Force (Philadelphia)";
      case USRI:
        return "Amgen Inc. (Rhode Island)";
      case USSC:
        return "Amgen Sacramento Office";
      case USSD:
        return "Amgen San Diego";
      case USSF:
        return "Amgen Inc. (So. San Francisco)";
      case USTO:
        return "Amgen Inc. (Thousand Oaks)";
      case USWA:
        return "Amgen Inc. (Washington - Bothell & Seattle)";
      case USFW:
        return "United States Flexible Workers";
      case USRW:
        return "United States Remote Workers";
      default:
        return "Unknown";
    }
  }

  public static TimeZone getLocationTimeZone(String locationCode) {
    return getLocationTimeZone(AmgenLocationCode.valueOf(locationCode));
  }

  public static TimeZone getLocationTimeZone(AmgenLocationCode code) {
    switch (code) {
      case AUAP:
        return TimeZone.getTimeZone("Australia/Adelaide");
      case AUBB:
        return TimeZone.getTimeZone("Australia/Adelaide");
      case AUBR:
        return TimeZone.getTimeZone("Australia/Adelaide");
      case AUGF:
        return TimeZone.getTimeZone("Australia/Adelaide");
      case AUME:
        return TimeZone.getTimeZone("Australia/Adelaide");
      case AUPE:
        return TimeZone.getTimeZone("Australia/South");
      case AUSY:
        return TimeZone.getTimeZone("Australia/Adelaide");
      case ATVI:
        return TimeZone.getTimeZone("Europe/Vienna");
      case AUVI:
        return TimeZone.getTimeZone("Europe/Vienna");
      case BEBR:
        return TimeZone.getTimeZone("Europe/Brussels");
      case BRBR:
        return TimeZone.getTimeZone("Brazil/East");
      case CABN:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case CAMI:
        return TimeZone.getTimeZone("America/New_York");
      case CAON:
        return TimeZone.getTimeZone("America/New_York");
      case CNHK:
        return TimeZone.getTimeZone("Asia/Hong_Kong");
      case CNSH:
        return TimeZone.getTimeZone("Asia/Shanghai");
      case CZPR:
        return TimeZone.getTimeZone("Europe/Prague");
      case DEHE:
        return TimeZone.getTimeZone("Europe/Copenhagen");
      case EETA:
        return TimeZone.getTimeZone("Europe/Tallinn");
      case FNLD:
        return TimeZone.getTimeZone("Europe/Helsinki");
      case FRPA:
        return TimeZone.getTimeZone("Europe/Paris");
      case GEMU:
        return TimeZone.getTimeZone("Europe/Berlin");
      case GERB:
        return TimeZone.getTimeZone("Europe/Berlin");
      case GRAT:
        return TimeZone.getTimeZone("Europe/Athens");
      case HUBU:
        return TimeZone.getTimeZone("Europe/Budapest");
      case INMU:
        return TimeZone.getTimeZone("Israel");
      case IECO:
        return TimeZone.getTimeZone("Israel");
      case IEDU:
        return TimeZone.getTimeZone("Israel");
      case IEEG:
        return TimeZone.getTimeZone("Israel");
      case ITMI:
        return TimeZone.getTimeZone("Europe/Rome");
      case JPSA:
        return TimeZone.getTimeZone("Japan");
      case JPTO:
        return TimeZone.getTimeZone("Japan");
      case LVRI:
        return TimeZone.getTimeZone("Europe/Riga");
      case LTVI:
        return TimeZone.getTimeZone("Europe/Vilnius");
      case MXMC:
        return TimeZone.getTimeZone("America/Chicago");
      case NEBR:
        return TimeZone.getTimeZone("Europe/Amsterdam");
      case NETB:
        return TimeZone.getTimeZone("Europe/Amsterdam");
      case AUNZ:
        return TimeZone.getTimeZone("Pacific/Auckland");
      case NOOS:
        return TimeZone.getTimeZone("Europe/Oslo");
      case PLEP:
        return TimeZone.getTimeZone("Europe/Warsaw");
      case PLWA:
        return TimeZone.getTimeZone("Europe/Warsaw");
      case POLI:
        return TimeZone.getTimeZone("Portugal");
      case RUMO:
        return TimeZone.getTimeZone("Europe/Moscow");
      case SGSG:
        return TimeZone.getTimeZone("Singapore");
      case SKBR:
        return TimeZone.getTimeZone("Europe/Bratislava");
      case SKPI:
        return TimeZone.getTimeZone("Europe/Bratislava");
      case SLLJ:
        return TimeZone.getTimeZone("Europe/Ljubljana");
      case SPBA:
        return TimeZone.getTimeZone("Europe/Madrid");
      case SESO:
        return TimeZone.getTimeZone("Europe/Stockholm");
      case SWEK:
        return TimeZone.getTimeZone("Europe/Stockholm");
      case SWLS:
        return TimeZone.getTimeZone("Europe/Vienna");
      case SWLU:
        return TimeZone.getTimeZone("Europe/Vienna");
      case AEDU:
        return TimeZone.getTimeZone("Asia/Dubai");
      case UKCA:
        return TimeZone.getTimeZone("Europe/London");
      case UKUX:
        return TimeZone.getTimeZone("Europe/London");
      case PRJU:
        return TimeZone.getTimeZone("America/Anchorage");
      case USAM:
        return TimeZone.getTimeZone("America/New_York");
      case USBO:
        return TimeZone.getTimeZone("America/Denver");
      case USCH:
        return TimeZone.getTimeZone("America/Chicago");
      case USDC:
        return TimeZone.getTimeZone("America/New_York");
      case USFM:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case USLO:
        return TimeZone.getTimeZone("America/New_York");
      case USMA:
        return TimeZone.getTimeZone("America/New_York");
      case USMV:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case USOH:
        return TimeZone.getTimeZone("America/New_York");
      case USPA:
        return TimeZone.getTimeZone("America/New_York");
      case USPH:
        return TimeZone.getTimeZone("America/New_York");
      case USRI:
        return TimeZone.getTimeZone("America/New_York");
      case USSC:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case USSD:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case USSF:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case USTO:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case USWA:
        return TimeZone.getTimeZone("America/Los_Angeles");
      case USFW:
      case USRW:
        return TimeZone.getTimeZone("America/Los_Angeles");
      default:
        return TimeZone.getTimeZone("America/Los_Angeles");
    }
  }

  public static AmgenLocationCode getLocationCode(String code) {
    if (code.equalsIgnoreCase("AMA")) {
      return USAM;
    } else if (code.equalsIgnoreCase("ASF")) {
      return USSF;
    } else if (code.equalsIgnoreCase("ATO")) {
      return USTO;
    } else if (code.equalsIgnoreCase("AWA")) {
      return USWA;
    } else if (code.equalsIgnoreCase("ARG")) {
      return GERB;
    } else {
      try {
        return valueOf(code);
      } catch (Exception e) {
      }
    }
    return null;
  }

}
