import src.data.CsvReader;
import src.graph.AdjacencyListGraph;
import src.experiments.ExperimentRunner;

public class RunExperiment {
    public static void main(String[] args) throws Exception {
        System.out.println("Running Sorted vs Unsorted Edges Experiment...\n");
        
        // Create a dummy graph for the ExperimentRunner
        AdjacencyListGraph dummyGraph = new AdjacencyListGraph();
        ExperimentRunner runner = new ExperimentRunner(dummyGraph);
        
        // Run the experiment
        runner.experimentSortedVsUnsorted(
            "data/cleaned_flights.csv",  // CSV path
            50,                           // Number of queries
            "out/experiments/sorted_vs_unsorted" // Output directory
        );
        
        System.out.println("\n✅ Experiment completed successfully!");
        System.out.println("Check out/experiments/sorted_vs_unsorted/ for results");
    }
}


