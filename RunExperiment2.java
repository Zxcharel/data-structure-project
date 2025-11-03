import src.data.CsvReader;
import src.graph.AdjacencyListGraph;
import src.experiments.ExperimentRunner;

public class RunExperiment2 {
    public static void main(String[] args) throws Exception {
        System.out.println("Running CSR vs Adjacency Lists Experiment...\n");
        
        // Create a dummy graph for the ExperimentRunner
        AdjacencyListGraph dummyGraph = new AdjacencyListGraph();
        ExperimentRunner runner = new ExperimentRunner(dummyGraph);
        
        // Run the experiment
        runner.experimentCSRvsAdjacency(
            "data/cleaned_flights.csv",  // CSV path
            50,                           // Number of queries
            "out/experiments/csr_vs_adjacency" // Output directory
        );
        
        System.out.println("\n✅ Experiment completed successfully!");
        System.out.println("Check out/experiments/csr_vs_adjacency/ for results");
    }
}

