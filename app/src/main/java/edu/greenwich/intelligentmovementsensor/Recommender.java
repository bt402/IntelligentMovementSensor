package edu.greenwich.intelligentmovementsensor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.FloatDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.retrieval.Retrieval.RetrievalMethod;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.util.Pair;


/**
 *  @author the cbr team
 */

public class Recommender {

    public CBREngine engine;
    public Project rec;
    public DefaultCaseBase cb;
    public Concept myConcept;



    public void loadengine () {
        engine = new CBREngine();
        rec = engine.createProjectFromPRJ();
        // create case bases and assign the case bases that will be used for submitting a query
        cb = (DefaultCaseBase)rec.getCaseBases().get(CBREngine.getCaseBase());
        // create a concept and get the main concept of the project;
        myConcept = rec.getConceptByID(CBREngine.getConceptName());
    }

    public String solveOuery(String name, Float accelerometerPeak, Float gravitometerPeak, Float gyroPeak, Integer numberofcases, Float timePassed) {

        String answer="";
        // create a new retrieval
        Retrieval ret = new Retrieval(myConcept, cb);
        // specify the retrieval method
        ret.setRetrievalMethod(RetrievalMethod.RETRIEVE_SORTED);
        // create a query instance
        Instance query = ret.getQueryInstance();
        // Insert values into the query: Symbolic Description
        SymbolDesc moveDesc = (SymbolDesc) myConcept.getAllAttributeDescs().get("MovementName");
        query.addAttribute(moveDesc,moveDesc.getAttribute(name));

        // Insert values into the query: Float Description
        FloatDesc accPeakDesc = (FloatDesc) myConcept.getAllAttributeDescs().get("AccelerometerPeak");
        FloatDesc gravPeakDesc = (FloatDesc) myConcept.getAllAttributeDescs().get("GravitometerPeak");
        FloatDesc gyroPeakDesc = (FloatDesc) myConcept.getAllAttributeDescs().get("GyroPeak");
        FloatDesc timePassedDesc = (FloatDesc) myConcept.getAllAttributeDescs().get("Time");

        try {
            query.addAttribute(accPeakDesc,accPeakDesc.getAttribute(accelerometerPeak));
            query.addAttribute(gravPeakDesc, gravPeakDesc.getAttribute(gravitometerPeak));
            query.addAttribute(gyroPeakDesc, gyroPeakDesc.getAttribute(gyroPeak));
            query.addAttribute(timePassedDesc, timePassedDesc.getAttribute(timePassed));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // perform retrieval
        ret.start();
        // get the retrieval result
        List <Pair<Instance, Similarity>> result = ret.getResult();
        // get the case name
        if(result.size()>0){

            // get the best case's name
            String casename = result.get(0).getFirst().getName();
            // get the similarity value
            Double sim = result.get(0).getSecond().getValue();
            //answer = "I found "+casename+" with a similarity of "+sim+" as the best match.";
            //answer = answer+"The "+numberofcases+" best cases shown in a table: <br /> <br /> <table border=\"1\">";
            ArrayList<Hashtable<String, String>> liste = new ArrayList<Hashtable<String, String>>();
            // if more case results are requested than we have in our case base at all:
            if(numberofcases>=cb.getCases().size()){numberofcases = cb.getCases().size();}

            for(int i = 0; i<numberofcases; i++){

                liste.add(getAttributes(result.get(i), rec.getConceptByID(CBREngine.getConceptName())));
                System.out.println("liste "+liste.get(i).toString());
                //answer=answer+"<tr><td>"+result.get(i).getFirst().getName()+"</td><td>"+liste.get(i).toString()+"</td></tr>";
            }

            //answer= answer+"</table>";
            answer = liste.get(0).toString();
        }
        else{System.out.println("Retrieval Result is empty");}

        return answer;
    }

    public int getNoOfMovements(){
        SymbolDesc moveDesc = (SymbolDesc) myConcept.getAllAttributeDescs().get("MovementName");
        return  moveDesc.getNodes().size();
    }

    /**
     * The method returns the similarity table. The similairty is between the Symbol Attributes
     * @author brett,terry
     * @param attrName = name of the symbol attribute
     * @param symbFct = similarity function name for the symbol attribute
     * @return double[][] = returns the table in form of double array
     */
    public double[][] similarityTable(String attrName, String symbFct){
        SymbolDesc moveDesc = (SymbolDesc) myConcept.getAllAttributeDescs().get(attrName);
        SymbolFct function = (SymbolFct) moveDesc.getFct(symbFct);
        List<SymbolAttribute> symAttrs = new ArrayList<>(moveDesc.getSymbolAttributes());

        int size = symAttrs.size();

        double[][] simTable = new double[size][size];

        for(SymbolAttribute symAttr : symAttrs) {
            int indexOuter = symAttrs.indexOf(symAttr); // outer loop
            int indexInner = 0;
            for (final SymbolAttribute otherAttr : moveDesc.getSymbolAttributes()) {
                double similarity = 0d;
                try {
                    similarity = function.calculateSimilarity(symAttr, otherAttr).getValue();
                    simTable[indexOuter][indexInner] = similarity;
                    indexInner++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return simTable;
    }

    public void saveTable(String attrName, String symbFct, double[][] simTable, String[] nameTable){
        SymbolDesc moveDesc = (SymbolDesc) myConcept.getAllAttributeDescs().get(attrName);
        SymbolFct function = (SymbolFct) moveDesc.getFct(symbFct);

        for (int i = 0; i < simTable.length; i++){
            for (int j = 0; j < simTable[i].length; j++){
                // string, string, double
                function.setSimilarity(nameTable[i], nameTable[j], simTable[i][j]);
            }
        }
        rec.save();
    }

    public ArrayList<String> getListOfNames(String attrName){
        SymbolDesc moveDesc = (SymbolDesc) myConcept.getAllAttributeDescs().get(attrName);
        List<SymbolAttribute> symAttrs = new ArrayList<>(moveDesc.getSymbolAttributes());
        ArrayList<String> nameList = new ArrayList<>();

        for (int i = 0; i < symAttrs.size(); i++){
            nameList.add(symAttrs.get(i).toString());
        }

        return nameList;
    }

    public boolean changeNames(String oldName, String newName){
        SymbolDesc moveDesc = (SymbolDesc) myConcept.getAllAttributeDescs().get("MovementName");
        boolean saved = moveDesc.renameValue(oldName, newName);
        rec.save();
        return saved;
    }

    /**
     * This method delivers a Hashtable which contains the Attributs names (Attributes of the case) combined with their respective values.
     * @author weber,koehler,namuth
     * @param r = An Instance.
     * @param concept = A Concept
     * @return List = List containing the Attributes of a case with their values.
     */
    public static Hashtable<String, String> getAttributes(Pair<Instance, Similarity> r, Concept concept) {

        Hashtable<String, String> table = new Hashtable<String, String>();
        ArrayList<String> cats = getCategories(r);
        // Add the similarity of the case
        table.put("Sim", String.valueOf(r.getSecond().getValue()));
        for (String cat : cats) {
            // Add the Attribute name and its value into the Hashtable
            table.put(cat, r.getFirst().getAttForDesc(concept.getAllAttributeDescs().get(cat)).getValueAsString());
        }
        return table;
    }
    /**
     * This Method generates an ArrayList, which contains all Categories of aa Concept.
     * @author weber,koehler,namuth
     * @param r  =  An Instance.
     * @return List = List containing the Attributes names.
     */
    public static ArrayList<String> getCategories(Pair<Instance, Similarity> r) {

        ArrayList<String> cats = new ArrayList<String>();

        // Read all Attributes of a Concept
        Set<AttributeDesc> catlist = r.getFirst().getAttributes().keySet();

        for (AttributeDesc cat : catlist) {
            if (cat != null) {
                // Add the String literals for each Attribute into the ArrayList
                cats.add(cat.getName());
            }
        }
        return cats;
    }

    public String displayAmalgamationFunctions() {

        ArrayList <String> amalgam = new ArrayList<String>();
        String listoffunctions="Currently available Amalgamationfunctions: <br /> <br />";
        AmalgamationFct current = myConcept.getActiveAmalgamFct();
        System.out.println("Amalgamation Function is used = "+current.getName());
        List<AmalgamationFct> liste = myConcept.getAvailableAmalgamFcts();

        for (int i = 0; i < liste.size(); i++){
            System.out.println(liste.get(i).getName());
            listoffunctions=listoffunctions+liste.get(i).getName()+"<br />";
        }

        listoffunctions=listoffunctions+(" <br /> <br /> Currently selected Amalgamationfunction: "+current.getName()+"\n");
        listoffunctions=listoffunctions+(" <br /> <br /> Please type the name of the Amalgamationfunction to use in the " +
                " Field \"Amalgamationfunction\" it will be automatically used during the next retrieval");
        System.out.println(listoffunctions);
        return listoffunctions;
    }
}
