/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amgen.ri.chem;

import amgen.ri.util.ExtFile;
import amgen.ri.util.ExtString;
import java.io.*;
import java.util.Properties;
import javax.vecmath.Vector2d;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.MoleculeSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.io.*;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 *
 * @author jemcdowe
 */
public class SMI2MOLConverter {
  public static String smi2mol(String smi, boolean addH) throws InvalidSmilesException, CDKException, IOException {
    IMolecule molecule;
    SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    molecule = sp.parseSmiles(smi);

    IMoleculeSet molecules = ConnectivityChecker.partitionIntoMolecules(molecule);
    CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
    StructureDiagramGenerator sdg = new StructureDiagramGenerator();
    IMoleculeSet updatedMolecules = new MoleculeSet();
    for (int i = 0; i < molecules.getAtomContainerCount(); i++) {
      IAtomContainer c = AtomContainerManipulator.removeHydrogensPreserveMultiplyBonded(molecules.getAtomContainer(i));
      IMolecule m = (IMolecule) c;
      if (addH) {
        ha.addImplicitHydrogens(m);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(m);
      }
      sdg.setMolecule(m, false);
      sdg.generateCoordinates(new Vector2d(0, 1));
      updatedMolecules.addMolecule(sdg.getMolecule());
    }
    molecules.removeAllAtomContainers();
    molecules = updatedMolecules;


    StringWriter sWriter = new StringWriter();
    SDFWriter sdfWriter = new SDFWriter(sWriter);
    for (int i = 0; i < molecules.getMoleculeCount(); i++) {
      sdfWriter.write(molecules.getMolecule(i));
      //MDLV2000Writer writer = new MDLV2000Writer(sWriter);
      //writer.write(molecules.getMolecule(i));
      //writer.close();
    }
    sdfWriter.close();

    return sWriter.toString();
  }

  public static IMolecule smi2molecule(String smi, boolean addH) throws InvalidSmilesException, CDKException, IOException {
    IMolecule molecule;
    SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    molecule = sp.parseSmiles(smi);

    IMoleculeSet molecules = ConnectivityChecker.partitionIntoMolecules(molecule);
    CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
    StructureDiagramGenerator sdg = new StructureDiagramGenerator();
    IMoleculeSet updatedMolecules = new MoleculeSet();
    for (int i = 0; i < molecules.getAtomContainerCount(); i++) {
      IAtomContainer c = AtomContainerManipulator.removeHydrogensPreserveMultiplyBonded(molecules.getAtomContainer(i));
      IMolecule m = (IMolecule) c;
      if (addH) {
        ha.addImplicitHydrogens(m);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(m);
      }
      sdg.setMolecule(m, false);
      sdg.generateCoordinates(new Vector2d(0, 1));
      updatedMolecules.addMolecule(sdg.getMolecule());
    }
    molecules.removeAllAtomContainers();
    molecules = updatedMolecules;

    for (int i = 0; i < molecules.getMoleculeCount(); i++) {
      return molecules.getMolecule(i);
    }
    return null;
  }

  public static String mol2smi(Reader molReader, boolean addH) throws IOException, CDKException {
    String molFile = ExtString.readToString(molReader);
    IMolecule molecule = null;
    try {
      molecule = (Molecule) new MDLV2000Reader(new StringReader(molFile), IChemObjectReader.Mode.STRICT).read(new Molecule());
    } catch (Throwable e) {
    }
    if (molecule == null) {
      try {
        molecule = (Molecule) new MDLV3000Reader(new StringReader(molFile), IChemObjectReader.Mode.STRICT).read(new Molecule());
      } catch (Throwable e) {
      }
    }
    if (addH) {
		    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
      
      CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
      ha.addImplicitHydrogens(molecule);
      AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);

      StructureDiagramGenerator sdg = new StructureDiagramGenerator();
      sdg.setMolecule(molecule);
      sdg.generateCoordinates();
      molecule= sdg.getMolecule();
    }
    return new SmilesGenerator().createSMILES(molecule, true, new boolean[molecule.getBondCount()]);
  }

  private static String smi2canonical(String smi) throws InvalidSmilesException, CDKException, IOException {
    IMolecule molecule;
    SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    molecule = sp.parseSmiles(smi);

    SmilesGenerator smiGen = new SmilesGenerator();
    return smiGen.createSMILES(molecule);
  }

  public static void main(String[] args) throws Exception {
    String[] s = {
      "O=C1[C@](CC(O)=O)(C)C[C@H](C2=CC=CC(Cl)=C2)[C@@H](C3=CC=C(Cl)C=C3)N1[C@@H](CC)C(OC(C)(C)C)=O",
      "ClC(C=C1)=CC=C1[C@H]([C@@H](C2=CC=CC(Cl)=C2)C[C@]3(C)CC(O)=O)N([C@@H](CC)C(OC(C)(C)C)=O)C3=O"
    };
    for (String smi : s) {
      System.out.println(smi2canonical(smi));
    }

  }
}
