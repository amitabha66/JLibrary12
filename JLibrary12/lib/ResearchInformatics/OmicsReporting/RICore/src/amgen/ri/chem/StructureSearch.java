/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.chem;

import amgen.ri.xml.ExtXMLElement;
import java.io.File;
import java.util.*;
import org.jdom.Element;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainerCreator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smsd.algorithm.rgraph.CDKMCS;

/**
 *
 * @author jemcdowe
 */
public class StructureSearch {
  private IQueryAtomContainer query;

  public StructureSearch(ExtMolecule queryMol) throws InvalidSmilesException {
    SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    query = QueryAtomContainerCreator.createBasicQueryContainer(queryMol.getMolecule());
  }

  public boolean isSubstructure(ExtMolecule molecule) throws CDKException {
    return CDKMCS.isSubgraph(molecule.getMolecule(), query, true);
  }

  public Set<ExtMolecule> performSearch(Collection<ExtMolecule> searchSet) throws CDKException {
    Set<ExtMolecule> matchSet = new HashSet<ExtMolecule>();
    for (ExtMolecule molecule : searchSet) {
      if (isSubstructure(molecule)) {
        matchSet.add(molecule);
      }
    }
    return matchSet;
  }

  public static void main(String[] args) throws Exception {
    List<Element> structureEls = ExtXMLElement.getXPathElements(ExtXMLElement.toDocument(new File("/temp/mdm2.xml")), "//Structure");
    List<ExtMolecule> mols = new ArrayList<ExtMolecule>();
    for (Element structEl : structureEls) {
      String id = structEl.getChildText("ID");
      String mol = structEl.getChildText("MOL");
      mols.add(new ExtMolecule(id, mol));
    }
    String smilesQuery = "O[C@@H]1CCC[C@@H]1O";
    //smilesQuery= "OC1CCCC1O";
    //mols.clear();
    //mols.add(new ExtMolecule("1", smilesQuery));
    
    
    Set<ExtMolecule> results = new StructureSearch(new ExtMolecule(new File("/temp/query.mol"))).performSearch(mols);

    for (ExtMolecule result : results) {
      System.out.println(result.getId());
    }

        System.out.println(results.size()+"/"+mols.size());


  }
}
