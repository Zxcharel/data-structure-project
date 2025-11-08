# Experiment 5: Prefix Autocomplete on Outgoing Routes

**When Tries Beat Arrays: Prefix Autocomplete on Outgoing Routes**

## What was tested
For the top 100 origin nodes (by out-degree), we queried “suggest destinations by 1–3 letter prefixes” (simulating an autocomplete UI). We measured per-query latency to list all destinations whose names start with the prefix.

## What made it interesting
`RoutePartitionedTrieGraph` can traverse a prefix path once and enumerate only matches, while list/array/CSR-based structures must scan all neighbors and perform string checks for each. This creates a workload where the trie’s indexing advantage is highlighted.

## Key learning
For prefix-heavy workloads, the trie achieves much lower per-query latency, especially on high-degree hubs. Traditional structures excel at sequential full scans, but their advantage fades when faced with repeated selective prefix filters.

---

## Methodology
- Build the graph from CSV.
- Select top-K origins by out-degree (default K=100).
- For each origin, derive prefixes from actual destinations (1–3 leading letters), sample up to N per origin (default N=20).
- Measure latency to return matching destinations for each prefix.
- If the graph is `RoutePartitionedTrieGraph`, use `neighborsByPrefix(origin, prefix)`; otherwise, scan `neighbors(origin)` and filter with `startsWith`.

## How to run
1) Build and run the app
```bat
rmdir /s /q out 2>nul
mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -cp out src.Main
```

2) Build the graph from CSV (Menu 1)
- Choose either a baseline implementation (e.g., OffsetArrayGraph) or `RoutePartitionedTrieGraph`.

3) Run the experiment programmatically
Add a small snippet in your driver after building the graph:
```java
// After you have a Graph instance named `graph`
src.experiments.ExperimentRunner runner = new src.experiments.ExperimentRunner(graph);
runner.runPrefixAutocompleteExperiment(100, 20, "out/prefix_exp");
```
This will generate:
- `out/prefix_exp/prefix_autocomplete.csv`
- `out/prefix_exp/prefix_autocomplete_README.md`

Run it twice (once with a baseline graph, once with `RoutePartitionedTrieGraph`) and compare average latency.

## Output schema
`prefix_autocomplete.csv` columns:
- `origin`: origin node
- `prefix`: tested prefix (lowercased)
- `matches`: number of matching destinations
- `runtime_ns`: elapsed time (nanoseconds)

## Expected results
- Trie graph shows significantly lower average microseconds per query on hubs with many outgoing routes.
- Array/CSR/list graphs remain strong for pure neighbor iteration but become slower for repeated prefix filters.

## Notes
- JVM warm-up, CPU scaling, and I/O noise can affect microbenchmarks. Consider a few warm-up runs if needed.
- Degree distribution influences gains; larger hubs show bigger speedups.


