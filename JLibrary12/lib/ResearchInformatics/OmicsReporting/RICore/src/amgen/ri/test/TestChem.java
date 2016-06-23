/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.test;

import amgen.ri.chem.SMI2MOLConverter;
import amgen.ri.util.ExtString;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.BitSet;
import javax.vecmath.Vector2d;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.fingerprint.HybridizationFingerprinter;
import org.openscience.cdk.geometry.BondTools;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.formats.MDLV3000Format;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 *
 * @author jemcdowe
 */
public class TestChem {
  public static void main(String[] s) throws Exception {
    File molQuery = new File("C:/temp/RIP-488/Tests/query.mol");
    File molUpdated = new File("C:/temp/RIP-488/Tests/updated.mol");


    IMolecule moleculeQuery = null;
    try {
      moleculeQuery = (Molecule) new MDLV2000Reader(new FileReader(molQuery), IChemObjectReader.Mode.STRICT).read(new Molecule());
    } catch (Throwable e) {
    }
    if (moleculeQuery == null) {
      try {
        moleculeQuery = (Molecule) new MDLV3000Reader(new FileReader(molQuery), IChemObjectReader.Mode.STRICT).read(new Molecule());
      } catch (Throwable e) {
      }
    }

    IMolecule moleculeUpdated = null;
    try {
      moleculeUpdated = (Molecule) new MDLV2000Reader(new FileReader(molUpdated), IChemObjectReader.Mode.STRICT).read(new Molecule());
    } catch (Throwable e) {
    }
    if (moleculeUpdated == null) {
      try {
        moleculeUpdated = (Molecule) new MDLV3000Reader(new FileReader(molUpdated), IChemObjectReader.Mode.STRICT).read(new Molecule());
      } catch (Throwable e) {
      }
    }

    String smi1 = SMI2MOLConverter.mol2smi(new FileReader(molQuery), true);
    String smi2 = SMI2MOLConverter.mol2smi(new FileReader(molUpdated), true);
    System.out.println(smi1);
    System.out.println(smi2);
    System.out.println("Identical:= " + smi1.equals(smi2));

// Generate factory - throws CDKException if native code does not load
    InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
// Get InChIGenerator
    InChIGenerator gen = factory.getInChIGenerator(moleculeQuery);
    String inChi1 = gen.getInchi();
    InChIGenerator gen2 = factory.getInChIGenerator(moleculeUpdated);
    String inChi2 = gen2.getInchi();

    System.out.println(inChi1);
    System.out.println(inChi2);
    System.out.println("Identical:= " + inChi1.equals(inChi2));


    System.exit(0);
    String molFile = ExtString.readToString(new File("C:/temp/RIP-488/RIP-488_orig_struct.mol"));
    IMolecule origMolecule = null;
    try {
      origMolecule = (Molecule) new MDLV2000Reader(new StringReader(molFile), IChemObjectReader.Mode.STRICT).read(new Molecule());
    } catch (Throwable e) {
    }
    if (origMolecule == null) {
      try {
        origMolecule = (Molecule) new MDLV3000Reader(new StringReader(molFile), IChemObjectReader.Mode.STRICT).read(new Molecule());
      } catch (Throwable e) {
      }
    }
    for (IAtom atom : origMolecule.atoms()) {
      System.out.println(atom.getAtomTypeName());
    }


    SmilesGenerator smiGen = new SmilesGenerator();
    System.out.println(smiGen.createSMILES(origMolecule, true, new boolean[origMolecule.getBondCount()]));

    for (IAtom atom : origMolecule.atoms()) {
      System.out.println(atom.getAtomTypeName());
    }

    /*
     *
     * molFile = ExtString.readToString(new
     * File("C:/temp/RIP-488/RIP-488_mod_struct.mol"));
     * IMolecule updatedMolecule = null;
     * try {
     * updatedMolecule = (Molecule) new MDLV2000Reader(new
     * StringReader(molFile), IChemObjectReader.Mode.STRICT).read(new
     * Molecule());
     * } catch (Throwable e) {
     * }
     * if (updatedMolecule == null) {
     * try {
     * updatedMolecule = (Molecule) new MDLV3000Reader(new
     * StringReader(molFile), IChemObjectReader.Mode.STRICT).read(new
     * Molecule());
     * } catch (Throwable e) {
     * }
     * }
     * SmilesGenerator smiGen2 = new SmilesGenerator();
     * System.out.println(smiGen2.createSMILES(updatedMolecule, true, new boolean[updatedMolecule.getBondCount()]));
     */

    CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
    ha.addImplicitHydrogens(origMolecule);
    AtomContainerManipulator.convertImplicitToExplicitHydrogens(origMolecule);

    StructureDiagramGenerator sdg = new StructureDiagramGenerator();
    sdg.setMolecule(origMolecule);
    sdg.generateCoordinates();
    IMolecule layedOutOrigMolecule = sdg.getMolecule();


    SmilesGenerator smiGen3 = new SmilesGenerator();
    System.out.println(smiGen3.createSMILES(layedOutOrigMolecule, true, new boolean[layedOutOrigMolecule.getBondCount()]));

  }
}
