package medicalapp;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Arrays;
import java.util.List;

public class OntologyManager {
    private static final String NS = "http://example.org/medical#";
    private OntModel model;
    private InfModel infModel;

    public void loadOntology(String path) {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(path);
    }

    public void fillInitialPatients() {
        OntClass patientClass = model.getOntClass(NS + "Patient");
        OntClass symptomClass = model.getOntClass(NS + "Symptom");
        OntClass doctorClass = model.getOntClass(NS + "Doctor");
        ObjectProperty hasSymptom = model.getObjectProperty(NS + "hasSymptom");
        ObjectProperty assignedDoctor = model.getObjectProperty(NS + "assignedDoctor");
        DatatypeProperty nameProp = model.getDatatypeProperty(NS + "hasFirstName");
        DatatypeProperty surnameProp = model.getDatatypeProperty(NS + "hasLastName");
        DatatypeProperty ageProp = model.getDatatypeProperty(NS + "hasAge");

        if (patientClass == null || doctorClass == null || hasSymptom == null || assignedDoctor == null) {
            System.err.println("Ontology classes or properties not found. Check the ontology structure.");
            return;
        }

        Individual doctor = model.createIndividual(NS + "Doctor_Shevchenko", doctorClass);
        if (nameProp != null) doctor.addProperty(nameProp, "Olena");
        if (surnameProp != null) doctor.addProperty(surnameProp, "Shevchenko");

        List<String[]> patients = Arrays.asList(
                new String[]{"Anna", "Hnatenko", "60", "Dyspnea,Fainting,Tachycardia"},
                new String[]{"Dmytro", "Melnyk", "47", "Fainting"},
                new String[]{"Iryna", "Koval", "33", "Dyspnea,Tachycardia"},
                new String[]{"Oksana", "Tkachenko", "59", "Fainting,Dyspnea,Tachycardia"},
                new String[]{"Mykola", "Bondarenko", "72", "Fainting,Dyspnea"},
                new String[]{"Svitlana", "Boyko", "38", "Tachycardia"},
                new String[]{"Oleksandr", "Shvets", "66", "Dyspnea"},
                new String[]{"Kateryna", "Savchenko", "45", "Dyspnea,Fainting,Tachycardia"},
                new String[]{"Viktor", "Polishchuk", "57", "Fainting,Tachycardia"},
                new String[]{"Yuriy", "Tymoshenko", "61", "Dyspnea,Fainting,Tachycardia"}
        );

        for (int i = 0; i < patients.size(); i++) {
            String[] p = patients.get(i);
            Individual patient = model.createIndividual(NS + "Patient_" + p[0], patientClass);
            if (nameProp != null) patient.addProperty(nameProp, p[0]);
            if (surnameProp != null) patient.addProperty(surnameProp, p[1]);
            if (ageProp != null) patient.addProperty(ageProp, p[2]);
            patient.addProperty(assignedDoctor, doctor);

            for (String s : p[3].split(",")) {
                Resource symptomRes = model.getResource(NS + s.trim());
                if (symptomRes != null) {
                    patient.addProperty(hasSymptom, symptomRes);
                } else {
                    System.err.println("Symptom not found in ontology: " + s.trim());
                }
            }
        }
    }

    public void assignCardiomyopathyDiagnosis() {
        OntClass patientClass = model.getOntClass(NS + "Patient");
        ObjectProperty hasSymptom = model.getObjectProperty(NS + "hasSymptom");
        ObjectProperty hasDiagnosis = model.getObjectProperty(NS + "hasDiagnosis");

        Resource symptom1 = model.getResource(NS + "Dyspnea");
        Resource symptom2 = model.getResource(NS + "Fainting");
        Resource symptom3 = model.getResource(NS + "Tachycardia");
        Resource diagnosis = model.getResource(NS + "HypertrophicCardiomyopathy");

        if (patientClass == null || hasSymptom == null || hasDiagnosis == null) {
            System.err.println("Ontology classes or properties not found.");
            return;
        }

        for (ExtendedIterator<Individual> it = model.listIndividuals(patientClass); it.hasNext(); ) {
            Individual patient = it.next();

            boolean has1 = patient.hasProperty(hasSymptom, symptom1);
            boolean has2 = patient.hasProperty(hasSymptom, symptom2);
            boolean has3 = patient.hasProperty(hasSymptom, symptom3);

            if (has1 && has2 && has3) {

                // Додаємо новий діагноз
                patient.addProperty(hasDiagnosis, diagnosis);
                System.out.println("Додано діагноз для: " + patient.getURI());
            }
        }
    }

    // Видалення пацієнта
    public boolean removePatient(String patientFirstName, String patientLastName) {
        // Побудова URI — краще включати і прізвище
        String patientURI = NS + "Patient_" + patientFirstName;

        // Знаходимо індивіда пацієнта
        Individual patient = model.getIndividual(patientURI);
        if (patient == null) {
            return false;
        }

        // Видаляємо всі трійки, де пацієнт — суб’єкт
        model.removeAll(patient, null, (RDFNode) null);

        // Видаляємо всі трійки, де пацієнт — об’єкт (на нього хтось посилається)
        model.removeAll(null, null, patient);

        return true;
    }

    public void applyReasoning() {
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        infModel = ModelFactory.createInfModel(reasoner, model);
    }

    public InfModel getInfModel() {
        return infModel;
    }

    public OntModel getBaseModel() {
        return model;
    }
}
