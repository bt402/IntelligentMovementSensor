package edu.greenwich.intelligentmovementsensor;

import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.casebase.Instance;

public class AddExisting {

    public CBREngine engine;
    public Project project;
    public DefaultCaseBase cb;
    public Concept myConcept;

    public void loadengine() {
        engine = new CBREngine();
        project = engine.createProjectFromPRJ();
        cb = (DefaultCaseBase) project.getCaseBases().get(engine.getCaseBase());
        myConcept = project.getConceptByID(engine.getConceptName());
    }

    public void addCase(String movementName, float accelerometerPeak, float gravitometerPeak, float gyroPeak) {
        loadengine();

        try {
            int size = myConcept.getAllInstances().size();
            Instance i = myConcept.addInstance("Movement #" + size);
            i.addAttribute("MovementName", movementName);
            i.addAttribute("AccelerometerPeak", accelerometerPeak);
            i.addAttribute("GravitometerPeak", gravitometerPeak);
            i.addAttribute("GyroPeak", gyroPeak);
            cb.addCase(i);
            project.save();
        }
        catch (Exception er){
            System.out.println(er);
        }
    }
}
