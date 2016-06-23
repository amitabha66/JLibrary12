package amgen.ri.json;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import amgen.ri.util.ExtBase64;
import amgen.ri.util.ExtClass;
import amgen.ri.util.ExtFile;
import amgen.ri.xml.ExtXMLElement;
import java.util.Collection;
import org.apache.commons.collections.IteratorUtils;

/**
 * Set of utilities operating on/creating JSON objects
 */
public class ExtJSON {
  /**
   * Create a List if JSONObjects from the JSONArray. If an member of the
   * JSONArray is not a JSONObject, it is assigned as null.
   *
   * @param arr Document
   * @return JSONArray
   */
  public static List<JSONObject> toJSONObjectList(JSONArray arr) {
    List<JSONObject> list = new ArrayList<JSONObject>();
    if (arr != null) {
      for (int i = 0; i < arr.length(); i++) {
        list.add(arr.optJSONObject(i));
      }
    }
    return list;
  }

  /**
   * Create a JSONArray of JSONObjects containing the attributes as key/value
   * pairs of the elements returned by the record XPath
   *
   * @param doc Document
   * @param recordXPath String
   * @return JSONArray
   */
  public static JSONArray toJSONArray(Document doc, String recordXPath) {
    return toJSONArray(doc.getRootElement(), recordXPath);
  }

  /**
   * Create a JSONArray of JSONObjects containing the attributes as key/value
   * pairs of the elements returned by the record XPath
   *
   * @param element Document
   * @param recordXPath String
   * @return JSONArray
   */
  public static JSONArray toJSONArray(Element element, String recordXPath) {
    JSONArray jsonArr = new JSONArray();
    List<Element> elements = ExtXMLElement.getXPathElements(element, recordXPath);
    for (Element el : elements) {
      List<Attribute> attrs = el.getAttributes();
      if (attrs.size() > 0) {
        JSONObject jsonObj = new JSONObject();
        jsonArr.put(jsonObj);
        for (Attribute attr : attrs) {
          try {
            jsonObj.put(attr.getName(), attr.getValue());
          } catch (JSONException ex) {
          }
        }
      }
    }
    return jsonArr;
  }

  /**
   * Create a JSONObject or JSONArray from the String if possible. null if the
   * String does not represent a JSON Object or JSON Array. Also checks if the
   * String is BASE64 encoded
   *
   * @param json Document
   * @return Object- either JSONObject JSONArray
   */
  public static Object toJSON(String json) {
    try {
      return new JSONObject(json);
    } catch (Exception e) {
    }
    try {
      return new JSONArray(json);
    } catch (Exception e) {
    }

    //Check if the json String is Base64 Encoded
    try {
      return new JSONObject(new String(ExtBase64.decode(json)));
    } catch (Exception e) {
    }
    try {
      return new JSONArray(new String(ExtBase64.decode(json)));
    } catch (Exception e) {
    }
    return null;
  }

  /**
   * Returns whether the String is a JSONObject or JSONArray. Also checks if the
   * String is BASE64 encoded
   *
   * @param json Document
   * @return Object- either JSONObject JSONArray
   */
  public static boolean isJSON(String json) {
    return (toJSON(json) != null);
  }

  /**
   * Base64 encodes a JSONObject returning the Base64 String
   *
   * @param json Document
   * @return String- Base64 String
   */
  public static String encodeJSON(JSONObject json) {
    return ExtBase64.encodeToString(json.toString().getBytes(), false);
  }

  /**
   * Base64 encodes a JSONArray returning the Base64 String
   *
   * @param json Document
   * @return String- Base64 String
   */
  public static String encodeJSON(JSONArray json) {
    return ExtBase64.encodeToString(json.toString().getBytes(), false);
  }

  /**
   * Adds an attribute to the JSONObject if the value is not null
   *
   * @param obj JSONObject
   * @param name String
   * @param value String
   */
  public static void addIfNotNull(JSONObject obj, String name, String value) {
    if (value != null) {
      try {
        obj.put(name, value);
      } catch (JSONException ex) {
      }
    }
  }

  /**
   * Creates an XML Documents from a Collection of JSONObjects
   *
   * @param jObjs
   * @param rootElName
   * @param memberElName
   * @return
   */
  public static Document toDocument(Collection<JSONObject> jObjs, String rootElName, String memberElName) {
    Element rootEl = new Element(rootElName);
    for (JSONObject jObj : jObjs) {
      Element memberEl = ExtXMLElement.addElement(rootEl, memberElName);
      addToXML(jObj, memberEl);
    }
    return new Document(rootEl);
  }

  /**
   * Creates an XML Documents from an JSONObject
   *
   * @param jObj
   * @param rootElName
   * @return
   */
  public static Document toDocument(JSONObject jObj, String rootElName) {
    return new Document(addToXML(jObj, new Element(rootElName)));
  }

  /**
   * Adds the keys from the JSONObject to the parent Element object
   *
   * @param jObj
   * @param parentEl
   * @return
   */
  public static Element addToXML(JSONObject jObj, Element parentEl) {
    List<String> keys = IteratorUtils.toList(jObj.keys());
    for (String key : keys) {
      String childElName = key.replaceAll("\\W", "").replaceFirst("^[0-9]+", "");
      Object val = jObj.opt(key);
      addToXML(val, parentEl, childElName);
    }
    return parentEl;
  }

  /**
   * Adds the val object a JSONObject to the parent Element object using the
   * childElName
   * as the new Element name
   *
   * @param val
   * @param parentEl
   * @param childElName
   */
  private static void addToXML(Object val, Element parentEl, String childElName) {
    if (val == null) {
      ExtXMLElement.addElement(parentEl, childElName);
    } else if (val instanceof JSONObject) {
      Element child = ExtXMLElement.addElement(parentEl, childElName);
      addToXML((JSONObject) val, child);
    } else if (val instanceof JSONArray) {
      Element child = ExtXMLElement.addElement(parentEl, childElName);
      List members = ((JSONArray) val).asList();
      for (int i = 0; i < members.size(); i++) {
        Object member = members.get(i);
        String child2ElName = childElName;
        addToXML(member, child, child2ElName);
      }
    } else {
      ExtXMLElement.addTextElement(parentEl, childElName, val.toString());
    }
  }

  public static void main(String[] args) throws Exception {
    String s
            = "{   \"AnalyzedBy\": \".\",   \"Category\": \"\",   \"CostCenter\": \"Genome Analysis Unit\",   \"ExperimentSource\": \"Public\",   \"ExperimentTitle\": \"CCLE Expression, Copy Number, and Mutation Data\",   \"ID\": \"ccle_expression_copy_number_mutation\",   \"Link\": \"Download\",   \"Matched#\": 1530087,   \"Platform\": \"affymetrix.genomewidesnp_6_cnv; affymetrix.hg-u133_plus_2; Human.B37_RefGene\",   \"PlatformType\": \"Genotyping\\\\Genotyping by Array\\\\Comparative Hybridization by Array; Genotyping\\\\Genotyping by High-Throughput Sequencing\\\\Targeted Resequencing; Transcription Profiling\\\\Transcription Profiling by Array\",   \"PrincipalInvestigator\": \"Cosgrove, Elissa\",   \"Project\": \".\",   \"PublishDate\": \"2013-10-10 16:06:20\",   \"PublishedBy\": \"elissac\",   \"Sample#\": 1036,   \"Species\": \"Human\",   \"datasets\": [     {       \"as_created_by\": \"elissac\",       \"as_created_date\": \"10/10/2013 04:06:20 PM\",       \"as_dimension\": \"18901*1036\",       \"as_experiment_description\": \"expression microarray values using custom CDF, SNP-derived copy number values from CCLE website, and mutation calls from the Broad-Novartis CCLE project\",       \"as_experiment_design_date\": \"10/10/2013 03:47:24 PM\",       \"as_experiment_source\": \"Public\",       \"as_experiment_title\": \"CCLE Expression, Copy Number, and Mutation Data\",       \"as_modified_date\": \"10/10/2013 04:06:47 PM\",       \"as_platform\": \"affymetrix.genomewidesnp_6_cnv; affymetrix.hg-u133_plus_2; Human.B37_RefGene\",       \"as_platform_types\": [         \"Comparative Hybridization by Array\",         \"Targeted Resequencing\",         \"Transcription Profiling by Array\"       ],       \"as_principalinvestigator\": \"elissac\",       \"as_project\": \"null\",       \"as_reportable_platforms\": [         \"Affymetrix.HG-U133_Plus_2\",         \"Human.B37_RefGene\"       ],       \"as_species\": \"Human\",       \"as_verify_valid_platform\": false,       \"as_xref_collection_id\": \"ccle_expression_copy_number_mutation\",       \"as_xref_data_class\": \"Data\",       \"as_xref_data_name\": \"ccle_expression\",       \"as_xref_data_type\": \"MicroArray\",       \"as_xref_mode\": \"Data\",       \"id\": \"ccle_expression_copy_number_mutation.ccle_expression.Data\",       \"oh_reportable_by_user\": true,       \"oh_reported_status\": \"UNREPORTED\",       \"oh_reporting_requires_result_type\": true,       \"oh_transfer_status\": \"NOTTRANSFERRED\",       \"platform_types\": [         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134887,           \"platform_type\": \"Comparative Hybridization by Array\",           \"platform_type_term_id\": 7134973,           \"result_types\": [             {               \"result_type\": \"Log2 Ratio\",               \"result_type_term_id\": 7134924             },             {               \"result_type\": \"Segment Length\",               \"result_type_term_id\": 7134920             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134889,           \"platform_type\": \"Targeted Resequencing\",           \"platform_type_term_id\": 7134976,           \"result_types\": [             {               \"result_type\": \"Read Count\",               \"result_type_term_id\": 7134933             },             {               \"result_type\": \"Minor Allele Frequency\",               \"result_type_term_id\": 7134931             },             {               \"result_type\": \"Mutation CDNA\",               \"result_type_term_id\": 7134925             },             {               \"result_type\": \"Mutation AA\",               \"result_type_term_id\": 7134932             },             {               \"result_type\": \"Mutation Description/Variant Classification\",               \"result_type_term_id\": 7134927             },             {               \"result_type\": \"Mutation Status\",               \"result_type_term_id\": 7134935             }           ],           \"technology\": \"Next-Generation Sequencing\",           \"technology_term_id\": 7134880         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134888,           \"platform_type\": \"Transcription Profiling by Array\",           \"platform_type_term_id\": 7134970,           \"result_types\": [             {               \"result_type\": \"Ratio\",               \"result_type_term_id\": 7134929             },             {               \"result_type\": \"Intensity\",               \"result_type_term_id\": 7134923             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         }       ]     },     {       \"as_created_by\": \"elissac\",       \"as_created_date\": \"10/10/2013 04:06:20 PM\",       \"as_dimension\": \"21217*995\",       \"as_experiment_description\": \"expression microarray values using custom CDF, SNP-derived copy number values from CCLE website, and mutation calls from the Broad-Novartis CCLE project\",       \"as_experiment_design_date\": \"10/10/2013 03:47:24 PM\",       \"as_experiment_source\": \"Public\",       \"as_experiment_title\": \"CCLE Expression, Copy Number, and Mutation Data\",       \"as_modified_date\": \"10/10/2013 04:06:47 PM\",       \"as_platform\": \"affymetrix.genomewidesnp_6_cnv; affymetrix.hg-u133_plus_2; Human.B37_RefGene\",       \"as_platform_types\": [         \"Comparative Hybridization by Array\",         \"Targeted Resequencing\",         \"Transcription Profiling by Array\"       ],       \"as_principalinvestigator\": \"elissac\",       \"as_project\": \"null\",       \"as_reportable_platforms\": [         \"Affymetrix.HG-U133_Plus_2\",         \"Human.B37_RefGene\"       ],       \"as_species\": \"Human\",       \"as_verify_valid_platform\": false,       \"as_xref_collection_id\": \"ccle_expression_copy_number_mutation\",       \"as_xref_data_class\": \"Data\",       \"as_xref_data_name\": \"ccle_copy_number\",       \"as_xref_data_type\": \"MicroArray\",       \"as_xref_mode\": \"Data\",       \"id\": \"ccle_expression_copy_number_mutation.ccle_copy_number.Data\",       \"oh_reportable_by_user\": true,       \"oh_reported_status\": \"UNREPORTED\",       \"oh_reporting_requires_result_type\": true,       \"oh_transfer_status\": \"NOTTRANSFERRED\",       \"platform_types\": [         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134887,           \"platform_type\": \"Comparative Hybridization by Array\",           \"platform_type_term_id\": 7134973,           \"result_types\": [             {               \"result_type\": \"Log2 Ratio\",               \"result_type_term_id\": 7134924             },             {               \"result_type\": \"Segment Length\",               \"result_type_term_id\": 7134920             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134889,           \"platform_type\": \"Targeted Resequencing\",           \"platform_type_term_id\": 7134976,           \"result_types\": [             {               \"result_type\": \"Read Count\",               \"result_type_term_id\": 7134933             },             {               \"result_type\": \"Minor Allele Frequency\",               \"result_type_term_id\": 7134931             },             {               \"result_type\": \"Mutation CDNA\",               \"result_type_term_id\": 7134925             },             {               \"result_type\": \"Mutation AA\",               \"result_type_term_id\": 7134932             },             {               \"result_type\": \"Mutation Description/Variant Classification\",               \"result_type_term_id\": 7134927             },             {               \"result_type\": \"Mutation Status\",               \"result_type_term_id\": 7134935             }           ],           \"technology\": \"Next-Generation Sequencing\",           \"technology_term_id\": 7134880         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134888,           \"platform_type\": \"Transcription Profiling by Array\",           \"platform_type_term_id\": 7134970,           \"result_types\": [             {               \"result_type\": \"Ratio\",               \"result_type_term_id\": 7134929             },             {               \"result_type\": \"Intensity\",               \"result_type_term_id\": 7134923             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         }       ]     },     {       \"as_created_by\": \"elissac\",       \"as_created_date\": \"10/10/2013 04:06:20 PM\",       \"as_dimension\": \"1530087*26\",       \"as_experiment_description\": \"expression microarray values using custom CDF, SNP-derived copy number values from CCLE website, and mutation calls from the Broad-Novartis CCLE project\",       \"as_experiment_design_date\": \"10/10/2013 03:47:24 PM\",       \"as_experiment_source\": \"Public\",       \"as_experiment_title\": \"CCLE Expression, Copy Number, and Mutation Data\",       \"as_modified_date\": \"10/10/2013 04:06:47 PM\",       \"as_platform\": \"affymetrix.genomewidesnp_6_cnv; affymetrix.hg-u133_plus_2; Human.B37_RefGene\",       \"as_platform_types\": [         \"Comparative Hybridization by Array\",         \"Targeted Resequencing\",         \"Transcription Profiling by Array\"       ],       \"as_principalinvestigator\": \"elissac\",       \"as_project\": \"null\",       \"as_reportable_platforms\": [         \"Affymetrix.HG-U133_Plus_2\",         \"Human.B37_RefGene\"       ],       \"as_species\": \"Human\",       \"as_verify_valid_platform\": false,       \"as_xref_collection_id\": \"ccle_expression_copy_number_mutation\",       \"as_xref_data_class\": \"Data\",       \"as_xref_data_name\": \"full_variant_table\",       \"as_xref_data_type\": \"Table\",       \"as_xref_mode\": \"Report\",       \"id\": \"ccle_expression_copy_number_mutation.full_variant_table.Data\",       \"oh_reportable_by_user\": true,       \"oh_reported_status\": \"UNREPORTED\",       \"oh_reporting_requires_result_type\": false,       \"oh_transfer_status\": \"NOTTRANSFERRED\",       \"platform_types\": [         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134887,           \"platform_type\": \"Comparative Hybridization by Array\",           \"platform_type_term_id\": 7134973,           \"result_types\": [             {               \"result_type\": \"Log2 Ratio\",               \"result_type_term_id\": 7134924             },             {               \"result_type\": \"Segment Length\",               \"result_type_term_id\": 7134920             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134889,           \"platform_type\": \"Targeted Resequencing\",           \"platform_type_term_id\": 7134976,           \"result_types\": [             {               \"result_type\": \"Read Count\",               \"result_type_term_id\": 7134933             },             {               \"result_type\": \"Minor Allele Frequency\",               \"result_type_term_id\": 7134931             },             {               \"result_type\": \"Mutation CDNA\",               \"result_type_term_id\": 7134925             },             {               \"result_type\": \"Mutation AA\",               \"result_type_term_id\": 7134932             },             {               \"result_type\": \"Mutation Description/Variant Classification\",               \"result_type_term_id\": 7134927             },             {               \"result_type\": \"Mutation Status\",               \"result_type_term_id\": 7134935             }           ],           \"technology\": \"Next-Generation Sequencing\",           \"technology_term_id\": 7134880         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134888,           \"platform_type\": \"Transcription Profiling by Array\",           \"platform_type_term_id\": 7134970,           \"result_types\": [             {               \"result_type\": \"Ratio\",               \"result_type_term_id\": 7134929             },             {               \"result_type\": \"Intensity\",               \"result_type_term_id\": 7134923             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         }       ]     },     {       \"as_created_by\": \"elissac\",       \"as_created_date\": \"10/10/2013 04:06:20 PM\",       \"as_dimension\": \"1672*905\",       \"as_experiment_description\": \"expression microarray values using custom CDF, SNP-derived copy number values from CCLE website, and mutation calls from the Broad-Novartis CCLE project\",       \"as_experiment_design_date\": \"10/10/2013 03:47:24 PM\",       \"as_experiment_source\": \"Public\",       \"as_experiment_title\": \"CCLE Expression, Copy Number, and Mutation Data\",       \"as_modified_date\": \"10/10/2013 04:06:47 PM\",       \"as_platform\": \"affymetrix.genomewidesnp_6_cnv; affymetrix.hg-u133_plus_2; Human.B37_RefGene\",       \"as_platform_types\": [         \"Comparative Hybridization by Array\",         \"Targeted Resequencing\",         \"Transcription Profiling by Array\"       ],       \"as_principalinvestigator\": \"elissac\",       \"as_project\": \"null\",       \"as_reportable_platforms\": [         \"Affymetrix.HG-U133_Plus_2\",         \"Human.B37_RefGene\"       ],       \"as_species\": \"Human\",       \"as_verify_valid_platform\": false,       \"as_xref_collection_id\": \"ccle_expression_copy_number_mutation\",       \"as_xref_data_class\": \"Data\",       \"as_xref_data_name\": \"mutation_status\",       \"as_xref_data_type\": \"MicroArray\",       \"as_xref_mode\": \"Data\",       \"id\": \"ccle_expression_copy_number_mutation.mutation_status.Data\",       \"oh_reportable_by_user\": true,       \"oh_reported_status\": \"UNREPORTED\",       \"oh_reporting_requires_result_type\": true,       \"oh_transfer_status\": \"NOTTRANSFERRED\",       \"platform_types\": [         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134887,           \"platform_type\": \"Comparative Hybridization by Array\",           \"platform_type_term_id\": 7134973,           \"result_types\": [             {               \"result_type\": \"Log2 Ratio\",               \"result_type_term_id\": 7134924             },             {               \"result_type\": \"Segment Length\",               \"result_type_term_id\": 7134920             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134889,           \"platform_type\": \"Targeted Resequencing\",           \"platform_type_term_id\": 7134976,           \"result_types\": [             {               \"result_type\": \"Read Count\",               \"result_type_term_id\": 7134933             },             {               \"result_type\": \"Minor Allele Frequency\",               \"result_type_term_id\": 7134931             },             {               \"result_type\": \"Mutation CDNA\",               \"result_type_term_id\": 7134925             },             {               \"result_type\": \"Mutation AA\",               \"result_type_term_id\": 7134932             },             {               \"result_type\": \"Mutation Description/Variant Classification\",               \"result_type_term_id\": 7134927             },             {               \"result_type\": \"Mutation Status\",               \"result_type_term_id\": 7134935             }           ],           \"technology\": \"Next-Generation Sequencing\",           \"technology_term_id\": 7134880         },         {           \"dataset\": \"Panel\",           \"dataset_term_id\": 7134888,           \"platform_type\": \"Transcription Profiling by Array\",           \"platform_type_term_id\": 7134970,           \"result_types\": [             {               \"result_type\": \"Ratio\",               \"result_type_term_id\": 7134929             },             {               \"result_type\": \"Intensity\",               \"result_type_term_id\": 7134923             }           ],           \"technology\": \"Microarray\",           \"technology_term_id\": 7134882         }       ]     }   ] }";
    JSONObject json = (JSONObject) toJSON(s);
    System.out.println(ExtXMLElement.toPrettyString(toDocument(json, "root")));
  }
}
