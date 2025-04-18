package medicalapp;

import org.apache.jena.query.*;

import java.util.Scanner;

public class ConsoleInterface {
    private final OntologyManager ontologyManager;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleInterface(OntologyManager manager) {
        this.ontologyManager = manager;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n1. Показати всіх пацієнтів які задовольняють правило SWRL, мають діагноз «Гіпертрофічна кардіоміопатія»");
            System.out.println("2. Показати всіх пацієнтів");
            System.out.println("3. Видалити пацієнта");
            System.out.println("4. Вийти");
            System.out.print("Оберіть опцію: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1" -> showCardiomyopathyPatients();
                case "2" -> showAllPatients();
                case "3" -> removePatientInteractive();
                case "4" -> running = false;
                default -> System.out.println("Невірна опція.");
            }
        }
    }

    private void showCardiomyopathyPatients() {
        ontologyManager.assignCardiomyopathyDiagnosis();

        String queryStr = """
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    PREFIX owl: <http://www.w3.org/2002/07/owl#>
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX ex: <http://example.org/medical#>
                
                    SELECT ?patient ?firstName ?lastName ?age 
                           (STRAFTER(STR(?doctor), "#") AS ?doctorName)
                           (GROUP_CONCAT(STRAFTER(STR(?symptom), "#"); separator=", ") AS ?symptoms)
                           ?diagnosis
                    WHERE {
                      ?patient rdf:type ex:Patient . 
                      ?patient ex:hasFirstName ?firstName . 
                      ?patient ex:hasLastName ?lastName . 
                      OPTIONAL { ?patient ex:hasAge ?age }
                      OPTIONAL { ?patient ex:assignedDoctor ?doctor }
                      OPTIONAL { ?patient ex:hasSymptom ?symptom }
                      ?patient ex:hasDiagnosis ?diagnosis .
                      FILTER(?diagnosis = ex:HypertrophicCardiomyopathy) 
                    }
                    GROUP BY ?patient ?firstName ?lastName ?age ?doctor ?diagnosis
                    ORDER BY ?patient
                """;

        executeAndPrint(queryStr);
    }

    private void showAllPatients() {
        String queryStr = """
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX ex: <http://example.org/medical#>
                
                SELECT ?patient ?firstName ?lastName ?age 
                       (STRAFTER(STR(?doctor), "#") AS ?doctorName)
                       (GROUP_CONCAT(STRAFTER(STR(?symptom), "#"); separator=", ") AS ?symptoms)
                       (STRAFTER(STR(?diagnosis), "#") AS ?diagnosisName)
                WHERE {
                  ?patient rdf:type ex:Patient .
                  ?patient ex:hasFirstName ?firstName .
                  ?patient ex:hasLastName ?lastName .
                  OPTIONAL { ?patient ex:hasAge ?age }
                  OPTIONAL { ?patient ex:hasSymptom ?symptom }
                  OPTIONAL { ?patient ex:assignedDoctor ?doctor }
                  OPTIONAL { ?patient ex:hasDiagnosis ?diagnosis }
                }
                GROUP BY ?patient ?firstName ?lastName ?age ?doctor ?diagnosis
                ORDER BY ?patient
                """;

        executeAndPrint(queryStr);
    }

    private void executeAndPrint(String queryStr) {
        Query query = QueryFactory.create(queryStr);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, ontologyManager.getInfModel())) {
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.out(System.out, results);
        }
    }

    // Метод для видалення пацієнта через консоль
    private void removePatientInteractive() {
        System.out.print("Введіть ім’я пацієнта: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Введіть прізвище пацієнта: ");
        String lastName = scanner.nextLine().trim();

        if (ontologyManager.removePatient(firstName, lastName)) {
            System.out.println("Пацієнта видалено: " + firstName + "_" + lastName);
        } else {
            System.out.println("Пацієнта не знайдено");
        }
    }

}
