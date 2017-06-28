package edu.greenwich.intelligentmovementsensor;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.SymbolDesc;

public class AddNew {

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

    public void addCase(String symbolName) throws Exception {
        loadengine();

        SymbolDesc manufacturerDesc = (SymbolDesc) myConcept.getAllAttributeDescs().get("MovementName");
        manufacturerDesc.addSymbol(symbolName);

        project.save();
    }
}

