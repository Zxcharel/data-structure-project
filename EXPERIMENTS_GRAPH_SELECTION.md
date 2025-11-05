# Experiment Graph Selection - 7 Core Structures

## Overview
Both Experiment 1 (Pathfinding Benchmark) and Experiment 3 (Neighbor Iteration) now test the same **7 core graph structures** for consistent, focused comparison.

---

## Selected Graphs (7 Total)

### **1. AdjacencyListGraph** 
- **Role**: Baseline / Standard implementation
- **Why**: Industry-standard approach, good all-around performance
- **Complexity**: O(V + E) space, O(d) neighbor iteration

### **2. CSRGraph**
- **Role**: Optimized / Industry standard
- **Why**: Cache-friendly, sequential memory access, used in HPC
- **Complexity**: O(V + E) space, O(d) neighbor iteration
- **Expected**: 20-40% faster than baseline

### **3. SortedAdjacencyListGraph**
- **Role**: Fastest (for certain algorithms)
- **Why**: Pre-sorted edges may help greedy algorithms
- **Complexity**: O(E log E) build, O(d) neighbor iteration
- **Expected**: 10-20% faster than baseline for some workloads

### **4. OffsetArrayGraph**
- **Role**: CSR variant / Different memory layout
- **Why**: Single contiguous array, excellent cache locality
- **Complexity**: O(V + E) space, O(d) neighbor iteration
- **Expected**: Similar to CSRGraph, potentially faster

### **5. MatrixGraph**
- **Role**: Comparison / Worst case for sparse graphs
- **Why**: Demonstrates why sparse structures matter
- **Complexity**: O(V²) space, O(V) neighbor iteration ⚠️
- **Expected**: 50-100% slower for sparse graphs

### **6. RoutePartitionedTrieGraph**
- **Role**: Specialized / For prefix queries (Experiment 5)
- **Why**: Enables efficient prefix-based autocomplete
- **Complexity**: O(V + E × k) space, O(k + m) prefix search
- **Expected**: Slower for full iteration, faster for prefix queries

### **7. HalfEdgeGraph**
- **Role**: Specialized structure
- **Why**: Alternative representation for mesh/edge-heavy workloads
- **Complexity**: O(E × 2) space, O(d) neighbor iteration
- **Expected**: Moderate performance, specialized use case

---

## Removed Graphs (6 Total)

### Why These Were Excluded:

#### **DoublyLinkedListGraph** ❌
- Similar to AdjacencyList but with pointer overhead
- Redundant: doesn't add unique insights
- Poor cache locality

#### **CircularLinkedListGraph** ❌
- Similar to DoublyLinkedList
- Redundant: no significant differentiation
- Poor cache locality

#### **LinearArrayGraph** ❌
- Too similar to AdjacencyListGraph
- Redundant: overlaps with baseline
- Doesn't represent distinct design approach

#### **DynamicArrayGraph** ❌
- Too similar to AdjacencyListGraph
- Redundant: minor variation on standard approach
- Dynamic resizing adds complexity without unique benefits

#### **LinkCutTreeGraph** ❌
- Adapter pattern adds too much overhead
- Not practical for pathfinding workloads
- Specialized for dynamic connectivity (not relevant here)

#### **EulerTourTreeGraph** ❌
- Adapter pattern adds too much overhead
- Not practical for pathfinding workloads
- Specialized for tree-based operations (not relevant here)

---

## Benefits of 7-Graph Selection

### ✅ **Focused Comparison**
- Each graph represents a distinct design philosophy
- No redundant structures cluttering results
- Clear differentiation between approaches

### ✅ **Practical Relevance**
- All 7 have real-world use cases
- Covers spectrum from standard to specialized
- Industry-standard structures included

### ✅ **Efficiency**
- Faster experiment runtime
- Easier to analyze and present
- Clearer conclusions

### ✅ **Story Clarity**
```
Baseline → AdjacencyListGraph
Optimized → CSRGraph, OffsetArrayGraph
Sorted → SortedAdjacencyListGraph
Dense → MatrixGraph (anti-pattern for sparse)
Specialized → RoutePartitionedTrieGraph, HalfEdgeGraph
```

---

## Design Categories Represented

| Category | Graphs | Count |
|----------|--------|-------|
| **Array-Based (Cache-Friendly)** | CSRGraph, OffsetArrayGraph | 2 |
| **List-Based (Standard)** | AdjacencyListGraph, SortedAdjacencyListGraph | 2 |
| **Dense (Comparison)** | MatrixGraph | 1 |
| **Specialized** | RoutePartitionedTrieGraph, HalfEdgeGraph | 2 |

---

## Expected Performance Rankings

### **Neighbor Iteration (Experiment 3)**
1. 🥇 **CSRGraph / OffsetArrayGraph** - Fastest (cache-optimized)
2. 🥈 **SortedAdjacencyListGraph** - Fast (pre-sorted)
3. 🥉 **AdjacencyListGraph** - Baseline
4. 📊 **HalfEdgeGraph** - Moderate
5. 🔍 **RoutePartitionedTrieGraph** - Slower (trie overhead)
6. ❌ **MatrixGraph** - Slowest (O(V) iteration)

### **Full Pathfinding (Experiment 1)**
1. 🥇 **CSRGraph** - Overall winner
2. 🥈 **OffsetArrayGraph** - Close second
3. 🥉 **SortedAdjacencyListGraph** - Strong performer
4. 📊 **AdjacencyListGraph** - Baseline
5. 🔍 **HalfEdgeGraph** - Moderate
6. 🔧 **RoutePartitionedTrieGraph** - Specialized
7. ❌ **MatrixGraph** - Poor for sparse graphs

---

## Consistency Across Experiments

### **Same 7 Graphs Tested In:**
- ✅ Experiment 1: Pathfinding Benchmark
- ✅ Experiment 3: Neighbor Iteration Performance
- 🔄 Experiment 4: Scalability (if applicable)

### **Benefits:**
- Direct comparison across experiments
- Consistent methodology
- Easier to draw conclusions
- Clearer story for presentation

---

## Runtime Estimates

### **Experiment 1 (Pathfinding)**
```
7 graphs × 200 queries × 50 runs = 70,000 measurements
Estimated time: ~10-15 minutes
```

### **Experiment 3 (Neighbor Iteration)**
```
7 graphs × 300 nodes × 50 iterations = 105,000 measurements
Estimated time: ~2-3 minutes
```

---

## Implementation Details

### **Both Experiments Use:**
```java
// 7 graphs in consistent order
1. AdjacencyListGraph
2. CSRGraph
3. SortedAdjacencyListGraph
4. OffsetArrayGraph
5. MatrixGraph
6. RoutePartitionedTrieGraph
7. HalfEdgeGraph
```

### **Clear Documentation:**
- Each graph has descriptive comment
- Role and purpose explained
- Excluded graphs documented
- Rationale provided

---

## Summary

**Selected 7 core graphs** that represent distinct design approaches:
- ✅ Baseline (AdjacencyList)
- ✅ Optimized (CSR, OffsetArray)
- ✅ Sorted (SortedAdjacencyList)
- ✅ Comparison (Matrix)
- ✅ Specialized (RoutePartitionedTrie, HalfEdge)

**Removed 6 redundant graphs** that didn't add unique value:
- ❌ DoublyLinkedList, CircularLinkedList (similar to baseline)
- ❌ LinearArray, DynamicArray (too similar to baseline)
- ❌ LinkCutTree, EulerTourTree (impractical adapters)

**Result**: Focused, efficient experiments with clear, actionable insights! 🎯

