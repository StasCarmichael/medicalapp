package medicalapp;


/**
 * Головний клас застосунку MedicalApp.
 * Ініціалізує завантаження онтології та консольний інтерфейс.
 */
public class MedicalApp {
    public static void main(String[] args) {
        OntologyManager ontologyManager = new OntologyManager(); // Створення менеджера онтології
        ontologyManager.loadOntology("src/main/resources/medical.owl"); // Завантаження OWL-файлу

        ontologyManager.fillInitialPatients(); // Додавання початкових пацієнтів
        ontologyManager.applyReasoning();      // Застосування логічного виведення (reasoning)

        ConsoleInterface console = new ConsoleInterface(ontologyManager); // Запуск консолі
        console.start();
    }
}

