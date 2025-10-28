# Algorithm Performance Comparison

## Summary Statistics

| Algorithm | Heuristic | Avg Runtime (ms) | Avg Edges Relaxed | Avg Nodes Visited | Success Rate |
|-----------|-----------|------------------|-------------------|-------------------|--------------|
| A* | hop | 12.7 | 723.7 | 153.9 | 26.0% |
| A* | zero | 0.4 | 987.3 | 226.5 | 26.0% |
| Dijkstra | none | 0.3 | 987.3 | 226.5 | 26.0% |

## Key Findings

- **Fastest Algorithm**: Dijkstra with none heuristic (0.3 ms avg)
- **Most Efficient**: A* with hop heuristic (723.7 edges relaxed avg)

## Notes

- All algorithms use the same weight calculation formula
- A* with zero heuristic is equivalent to Dijkstra
- Hop heuristic uses BFS-based distance estimation
- Results may vary based on graph structure and query patterns
