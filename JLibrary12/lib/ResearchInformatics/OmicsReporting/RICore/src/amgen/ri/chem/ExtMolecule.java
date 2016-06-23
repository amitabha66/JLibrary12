/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.chem;

import amgen.ri.util.ExtString;
import com.symyx.draw.Renderer;
import java.io.*;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 *
 * @author jemcdowe
 */
public class ExtMolecule {
  private String id;
  private IMolecule molecule = null;
  private IMolecularFormula formula = null;

  public ExtMolecule(String id, String molecule) {
    this(molecule);
    this.id = id;
  }

  public ExtMolecule(String molecule) {
    if (molecule.split("\\r\\n|\\r|\\n").length > 1) {
      setMolecule(new StringReader(molecule));
    }
    if (this.molecule == null) {
      try {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        this.molecule = sp.parseSmiles(molecule);
        setFormula();
      } catch (Exception ise) {
        ise.printStackTrace();
      }
    }
  }

  public ExtMolecule(String smiles, boolean addH, boolean generateStructure) {
    try {
      SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
      this.molecule = sp.parseSmiles(smiles);
      setFormula();
      if (addH) {
        CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
        ha.addImplicitHydrogens(this.molecule);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(this.molecule);
      }
    } catch (CDKException e) {
      e.printStackTrace();
    }
    if (generateStructure) {
      // get the 2D coords
      try {
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(this.molecule);
        sdg.generateCoordinates();
        this.molecule = sdg.getMolecule();
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }

  public ExtMolecule(InputStream molFileStream) {
    this(new InputStreamReader(molFileStream));
  }

  public ExtMolecule(Reader molFileReader) {
    setMolecule(molFileReader);
  }

  public ExtMolecule(File molFile) throws FileNotFoundException {
    setMolecule(new FileReader(molFile));
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  private void setMolecule(Reader molFileReader) {
    molecule = null;
    String mol = null;
    try {
      mol = ExtString.readToString(molFileReader);
    } catch (IOException ex) {
      Logger.getLogger(ExtMolecule.class.getName()).log(Level.SEVERE, null, ex);
    }
    try {
      molecule = (Molecule) new MDLV2000Reader(new StringReader(mol), Mode.STRICT).read(new Molecule());
    } catch (Throwable e) {
    }
    if (molecule == null) {
      try {
        molecule = (Molecule) new MDLV3000Reader(new StringReader(mol), Mode.STRICT).read(new Molecule());
      } catch (Throwable e) {
      }
    }
    setFormula();
  }

  private void setFormula() {
    if (getMolecule() != null) {
      try {
        SmilesGenerator sg = new SmilesGenerator();
        sg.setUseAromaticityFlag(true);
        String smiles = sg.createSMILES(getMolecule());
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IMolecule smiMolecule = sp.parseSmiles(smiles);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(smiMolecule);
        formula = MolecularFormulaManipulator.getMolecularFormula(smiMolecule);
      } catch (Exception e) {
      }
    }
  }

  /**
   * Convert Molecule to SMILES
   *
   * @param useAromaticityFlag
   * @return
   */
  public String generateSmiles(boolean useAromaticityFlag) {
    if (getMolecule() == null) {
      return null;
    }
    SmilesGenerator sg = new SmilesGenerator();
    sg.setUseAromaticityFlag(useAromaticityFlag);
    return sg.createSMILES(getMolecule());
  }

  public String getV2000MOLFile() {
    StringWriter sWriter = new StringWriter();
    try {
      MDLV2000Writer writer = new MDLV2000Writer(sWriter);
      writer.write(getMolecule());
      writer.close();
    } catch (Exception ex) {
    }
    return sWriter.toString();
  }

  public String getChimeString() {
    Renderer renderer = new Renderer();
    renderer.setMolString(getV2000MOLFile());
    return renderer.getChimeString();
  }

  public String getSMILES() {
    return generateSmiles(true);
  }

  public IMolecularFormula getFormula() {
    return formula;
  }

  public String generateFormula2String() {
    if (getFormula() == null) {
      return null;
    }
    return MolecularFormulaManipulator.getString(getFormula());
  }

  public double getExactMass() {
    if (getFormula() == null) {
      return Double.NaN;
    }
    return MolecularFormulaManipulator.getTotalExactMass(getFormula());
  }

  public double getTotalMassNumber() {
    if (getFormula() == null) {
      return Double.NaN;
    }
    return MolecularFormulaManipulator.getTotalMassNumber(getFormula());
  }

  public double getMajorIsotopeMass() {
    if (getFormula() == null) {
      return Double.NaN;
    }
    return MolecularFormulaManipulator.getMajorIsotopeMass(getFormula());
  }

  public double getNaturalExactMass() {
    if (getFormula() == null) {
      return Double.NaN;
    }
    return MolecularFormulaManipulator.getNaturalExactMass(getFormula());
  }

  public double getTotalNaturalAbundance() {
    if (getFormula() == null) {
      return Double.NaN;
    }
    return MolecularFormulaManipulator.getTotalNaturalAbundance(getFormula());
  }

  public int getAtomCount() {
    if (getFormula() == null) {
      return -1;
    }
    return MolecularFormulaManipulator.getAtomCount(getFormula());
  }

  public int getHeavyAtomCount() {
    if (getFormula() == null) {
      return -1;
    }
    int heavyAtomCount = 0;
    try {
      Pattern elementPattern = Pattern.compile("([A-Z]{1}[a-z]{0,1})([0-9]{0,})\\s{0,}");
      Matcher elementMatcher = elementPattern.matcher(getHillString());
      while (elementMatcher.find()) {
        if (elementMatcher.groupCount() == 2) {
          String element = elementMatcher.group(1);
          int count = (elementMatcher.group(2).length() == 0 ? 1 : Integer.valueOf(elementMatcher.group(2)));
          if (!element.equals("H")) {
            heavyAtomCount += count;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return heavyAtomCount;
  }

  public String getHillString() {
    if (getFormula() == null) {
      return null;
    }
    return MolecularFormulaManipulator.getHillString(getFormula());
  }

  public boolean isAromatic() {
    try {
      if (getMolecule() != null) {
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
        return CDKHueckelAromaticityDetector.detectAromaticity(molecule);
      }
    } catch (CDKException ex) {
    }
    return false;
  }

  public double getTanimoto(ExtMolecule molecule) throws CDKException {
    BitSet fingerprint1 = new Fingerprinter().getFingerprint(getMolecule());
    BitSet fingerprint2 = new Fingerprinter().getFingerprint(molecule.getMolecule());
    return Tanimoto.calculate(fingerprint1, fingerprint2);
  }

  /**
   * @return the molecule
   */
  public IMolecule getMolecule() {
    return molecule;
  }

  /*
   * Static utilities
   */
  /**
   * Convert molFile to SMILES
   *
   * @param mol
   * @return
   * @throws CDKException
   */
  public static String mol2smi(String mol) throws CDKException {
    return new ExtMolecule(mol).generateSmiles(true);
  }

  /**
   * Convert molFile to SMILES
   *
   * @param in
   * @return
   * @throws CDKException
   */
  public static String mol2smi(InputStream in) throws CDKException {
    return new ExtMolecule(in).generateSmiles(true);
  }

  /**
   * Convert molFile to SMILES
   *
   * @param in
   * @return
   * @throws CDKException
   */
  public static String mol2smi(Reader in) throws CDKException {
    return new ExtMolecule(in).generateSmiles(true);
  }

  /**
   * Convert molFile to SMILES
   *
   * @param mol
   * @param useAromaticityFlag
   * @return
   * @throws CDKException
   */
  public static String mol2smi(String mol, boolean useAromaticityFlag) throws CDKException {
    return new ExtMolecule(mol).generateSmiles(useAromaticityFlag);
  }

  /**
   * Convert molFile to SMILES
   *
   * @param in
   * @param useAromaticityFlag
   * @return
   * @throws CDKException
   */
  public static String mol2smi(InputStream in, boolean useAromaticityFlag) throws CDKException {
    return new ExtMolecule(in).generateSmiles(useAromaticityFlag);
  }

  /**
   * Convert molFile to SMILES
   *
   * @param in
   * @param useAromaticityFlag
   * @return
   * @throws CDKException
   */
  public static String mol2smi(Reader in, boolean useAromaticityFlag) throws CDKException {
    return new ExtMolecule(in).generateSmiles(useAromaticityFlag);
  }

}
