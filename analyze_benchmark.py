#!/usr/bin/env python3
"""
Comprehensive analysis of Dijkstra benchmark results
Analyzes benchmark.csv and generates statistical summaries and visualizations
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
import warnings
warnings.filterwarnings('ignore')

# Set style
try:
    plt.style.use('seaborn-v0_8-darkgrid')
except:
    plt.style.use('seaborn-darkgrid')
sns.set_palette("husl")

# File paths
CSV_PATH = Path("out/experiments/experiment1_pathfinding_benchmark/benchmark.csv")
OUTPUT_DIR = Path("out/experiments/experiment1_pathfinding_benchmark/analysis")

def load_and_clean_data(csv_path):
    """Load CSV and clean data"""
    print(f"Loading data from {csv_path}...")
    df = pd.read_csv(csv_path)
    
    print(f"Original shape: {df.shape}")
    
    # Clean data
    # Remove negative or zero runtimes (likely measurement errors)
    initial_count = len(df)
    df = df[df['runtime_ms'] > 0]
    removed = initial_count - len(df)
    if removed > 0:
        print(f"Removed {removed} rows with zero/negative runtime")
    
    # Convert boolean
    df['path_found'] = df['path_found'].astype(bool)
    
    # Ensure numeric columns are numeric
    numeric_cols = ['runtime_ms', 'nodes_visited', 'edges_relaxed', 'path_length', 
                   'total_weight', 'memory_used_bytes']
    for col in numeric_cols:
        df[col] = pd.to_numeric(df[col], errors='coerce')
    
    # Remove any rows with NaN in critical columns
    df = df.dropna(subset=['runtime_ms', 'graph_type'])
    
    print(f"Cleaned shape: {df.shape}")
    print(f"Graph types: {df['graph_type'].nunique()}")
    print(f"Total queries: {df['query_id'].nunique()}")
    
    return df

def compute_summary_stats(df):
    """Compute comprehensive statistics per graph type"""
    print("\nComputing summary statistics...")
    
    stats = []
    
    for graph_type in df['graph_type'].unique():
        graph_data = df[df['graph_type'] == graph_type]
        
        stat = {
            'graph_type': graph_type,
            'num_queries': len(graph_data),
            'avg_runtime_ms': graph_data['runtime_ms'].mean(),
            'median_runtime_ms': graph_data['runtime_ms'].median(),
            'std_runtime_ms': graph_data['runtime_ms'].std(),
            'min_runtime_ms': graph_data['runtime_ms'].min(),
            'max_runtime_ms': graph_data['runtime_ms'].max(),
            'p95_runtime_ms': graph_data['runtime_ms'].quantile(0.95),
            'p99_runtime_ms': graph_data['runtime_ms'].quantile(0.99),
            'coefficient_of_variation': (graph_data['runtime_ms'].std() / graph_data['runtime_ms'].mean() * 100) if graph_data['runtime_ms'].mean() > 0 else 0,
            'avg_nodes_visited': graph_data['nodes_visited'].mean(),
            'avg_edges_relaxed': graph_data['edges_relaxed'].mean(),
            'avg_path_length': graph_data['path_length'].mean(),
            'success_rate': graph_data['path_found'].mean() * 100,
            'avg_memory_MB': graph_data['memory_used_bytes'].mean() / (1024 * 1024),
            'avg_runtime_per_MB': (graph_data['runtime_ms'].mean() / (graph_data['memory_used_bytes'].mean() / (1024 * 1024))) if graph_data['memory_used_bytes'].mean() > 0 else 0
        }
        
        stats.append(stat)
    
    stats_df = pd.DataFrame(stats)
    stats_df = stats_df.sort_values('avg_runtime_ms')
    
    return stats_df

def detect_anomalies(df):
    """Detect anomalies in the data"""
    print("\nDetecting anomalies...")
    
    anomalies = []
    
    # Zero runtimes
    zero_runtime = len(df[df['runtime_ms'] == 0])
    if zero_runtime > 0:
        anomalies.append(f"Found {zero_runtime} queries with zero runtime")
    
    # Extremely high runtimes (outliers)
    for graph_type in df['graph_type'].unique():
        graph_data = df[df['graph_type'] == graph_type]
        mean = graph_data['runtime_ms'].mean()
        std = graph_data['runtime_ms'].std()
        threshold = mean + 3 * std
        outliers = len(graph_data[graph_data['runtime_ms'] > threshold])
        if outliers > 0:
            anomalies.append(f"{graph_type}: {outliers} outliers (>3σ)")
    
    # Failed paths
    failed = len(df[df['path_found'] == False])
    if failed > 0:
        anomalies.append(f"Found {failed} queries where path was not found")
    
    # High variance
    stats = compute_summary_stats(df)
    high_variance = stats[stats['coefficient_of_variation'] > 30]
    if len(high_variance) > 0:
        anomalies.append(f"High variance (CoV > 30%): {', '.join(high_variance['graph_type'].tolist())}")
    
    return anomalies

def correlation_analysis(df):
    """Calculate correlations"""
    print("\nComputing correlations...")
    
    correlations = {
        'nodes_visited_vs_runtime': df['nodes_visited'].corr(df['runtime_ms']),
        'edges_relaxed_vs_runtime': df['edges_relaxed'].corr(df['runtime_ms']),
        'memory_vs_runtime': df['memory_used_bytes'].corr(df['runtime_ms']),
        'path_length_vs_runtime': df['path_length'].corr(df['runtime_ms'])
    }
    
    return correlations

def create_visualizations(df, stats_df, output_dir):
    """Create all visualizations"""
    print("\nCreating visualizations...")
    
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # 1. Average Runtime Bar Chart
    plt.figure(figsize=(14, 8))
    stats_sorted = stats_df.sort_values('avg_runtime_ms')
    plt.barh(stats_sorted['graph_type'], stats_sorted['avg_runtime_ms'])
    plt.xlabel('Average Runtime (ms)', fontsize=12)
    plt.ylabel('Graph Type', fontsize=12)
    plt.title('Average Runtime by Graph Type (Fastest to Slowest)', fontsize=14, fontweight='bold')
    plt.tight_layout()
    plt.savefig(output_dir / 'avg_runtime_bar.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    # 2. Runtime Boxplot
    plt.figure(figsize=(14, 8))
    # Sort by median runtime for better visualization
    order = stats_df.sort_values('median_runtime_ms')['graph_type'].tolist()
    df_sorted = df.copy()
    df_sorted['graph_type'] = pd.Categorical(df_sorted['graph_type'], categories=order, ordered=True)
    df_sorted = df_sorted.sort_values('graph_type')
    
    sns.boxplot(data=df_sorted, x='graph_type', y='runtime_ms')
    plt.xlabel('Graph Type', fontsize=12)
    plt.ylabel('Runtime (ms)', fontsize=12)
    plt.title('Runtime Distribution by Graph Type', fontsize=14, fontweight='bold')
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    plt.savefig(output_dir / 'runtime_boxplot.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    # 3. Memory vs Runtime Scatter (improved - log scale, grouped)
    plt.figure(figsize=(14, 8))
    # Use average memory per graph type to reduce clutter
    for _, row in stats_df.iterrows():
        graph_type = row['graph_type']
        graph_data = df[df['graph_type'] == graph_type]
        avg_memory = row['avg_memory_MB']
        avg_runtime = row['avg_runtime_ms']
        plt.scatter(avg_memory, avg_runtime, s=200, alpha=0.7, label=graph_type)
        plt.annotate(graph_type, (avg_memory, avg_runtime), fontsize=8, 
                    ha='center', va='bottom')
    plt.xlabel('Average Memory Used (MB)', fontsize=12)
    plt.ylabel('Average Runtime (ms)', fontsize=12)
    plt.title('Memory vs Runtime Trade-off (Average per Graph Type)', fontsize=14, fontweight='bold')
    plt.xscale('log')  # Log scale due to large range (0.08 to 19.92 MB)
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(output_dir / 'memory_vs_runtime.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    # 4. Edges Relaxed vs Runtime Scatter (more correlated than nodes_visited)
    plt.figure(figsize=(12, 8))
    for graph_type in stats_df['graph_type'].head(8):  # Top 8 for clarity
        graph_data = df[df['graph_type'] == graph_type]
        plt.scatter(graph_data['edges_relaxed'], graph_data['runtime_ms'], 
                   label=graph_type, alpha=0.6, s=50)
    plt.xlabel('Edges Relaxed', fontsize=12)
    plt.ylabel('Runtime (ms)', fontsize=12)
    plt.title('Edges Relaxed vs Runtime (Top 8 Graph Types, r=0.80)', fontsize=14, fontweight='bold')
    plt.legend(fontsize=9)
    plt.tight_layout()
    plt.savefig(output_dir / 'edges_vs_runtime.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    # 5. Coefficient of Variation (Stability)
    plt.figure(figsize=(14, 8))
    stats_sorted = stats_df.sort_values('coefficient_of_variation')
    colors = ['green' if x < 30 else 'orange' if x < 50 else 'red' for x in stats_sorted['coefficient_of_variation']]
    plt.barh(stats_sorted['graph_type'], stats_sorted['coefficient_of_variation'], color=colors)
    plt.xlabel('Coefficient of Variation (%)', fontsize=12)
    plt.ylabel('Graph Type', fontsize=12)
    plt.title('Runtime Stability (Lower = More Consistent)', fontsize=14, fontweight='bold')
    plt.axvline(x=30, color='orange', linestyle='--', label='High Variance Threshold')
    plt.legend()
    plt.tight_layout()
    plt.savefig(output_dir / 'coefficient_of_variation.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    # 6. Success Rate - SKIP: All graphs have same success rate (same queries)
    # Removed - not useful since all graphs run same queries and get same results
    
    # 7. Runtime Efficiency (Runtime per MB) - improved with log scale
    plt.figure(figsize=(14, 8))
    stats_sorted = stats_df.sort_values('avg_runtime_per_MB')
    plt.barh(stats_sorted['graph_type'], stats_sorted['avg_runtime_per_MB'])
    plt.xlabel('Average Runtime per MB (ms/MB)', fontsize=12)
    plt.ylabel('Graph Type', fontsize=12)
    plt.title('Memory Efficiency (Lower = Better, Log Scale)', fontsize=14, fontweight='bold')
    plt.xscale('log')  # Log scale due to wide range
    plt.tight_layout()
    plt.savefig(output_dir / 'memory_efficiency.png', dpi=300, bbox_inches='tight')
    plt.close()
    
    print(f"Visualizations saved to {output_dir}")

def generate_report(df, stats_df, anomalies, correlations, output_dir):
    """Generate markdown report"""
    print("\nGenerating report...")
    
    report = []
    report.append("# Benchmark Analysis Report\n")
    report.append("## Executive Summary\n\n")
    
    # Top findings
    fastest = stats_df.iloc[0]
    slowest = stats_df.iloc[-1]
    most_stable = stats_df.loc[stats_df['coefficient_of_variation'].idxmin()]
    most_efficient = stats_df.loc[stats_df['avg_runtime_per_MB'].idxmin()]
    
    report.append(f"**Fastest Graph**: {fastest['graph_type']} ({fastest['avg_runtime_ms']:.4f} ms avg)\n")
    report.append(f"**Slowest Graph**: {slowest['graph_type']} ({slowest['avg_runtime_ms']:.4f} ms avg)\n")
    report.append(f"**Most Stable**: {most_stable['graph_type']} (CoV: {most_stable['coefficient_of_variation']:.2f}%)\n")
    report.append(f"**Most Memory Efficient**: {most_efficient['graph_type']} ({most_efficient['avg_runtime_per_MB']:.4f} ms/MB)\n\n")
    
    # Performance Rankings
    report.append("## Performance Rankings\n\n")
    report.append("### Top 5 Fastest Graphs\n")
    report.append("| Rank | Graph Type | Avg Runtime (ms) | Std Dev (ms) | CoV (%) |\n")
    report.append("|------|------------|-------------------|--------------|----------|\n")
    for i, row in stats_df.head(5).iterrows():
        report.append(f"| {i+1} | {row['graph_type']} | {row['avg_runtime_ms']:.4f} | {row['std_runtime_ms']:.4f} | {row['coefficient_of_variation']:.2f} |\n")
    
    report.append("\n### Bottom 5 Slowest Graphs\n")
    report.append("| Rank | Graph Type | Avg Runtime (ms) | Std Dev (ms) | CoV (%) |\n")
    report.append("|------|------------|-------------------|--------------|----------|\n")
    for i, (idx, row) in enumerate(stats_df.tail(5).iterrows(), start=len(stats_df)-4):
        report.append(f"| {i} | {row['graph_type']} | {row['avg_runtime_ms']:.4f} | {row['std_runtime_ms']:.4f} | {row['coefficient_of_variation']:.2f} |\n")
    
    # Correlations
    report.append("\n## Correlation Analysis\n\n")
    report.append("| Metric Pair | Correlation Coefficient |\n")
    report.append("|-------------|------------------------|\n")
    for name, corr in correlations.items():
        report.append(f"| {name.replace('_', ' ').title()} | {corr:.4f} |\n")
    
    # Anomalies
    if anomalies:
        report.append("\n## Anomalies Detected\n\n")
        for anomaly in anomalies:
            report.append(f"- {anomaly}\n")
    
    # Detailed Statistics Table
    report.append("\n## Complete Statistics Table\n\n")
    # Convert DataFrame to markdown manually
    report.append("| " + " | ".join(stats_df.columns) + " |\n")
    report.append("|" + "|".join(["---" for _ in stats_df.columns]) + "|\n")
    for _, row in stats_df.iterrows():
        report.append("| " + " | ".join([str(val) for val in row.values]) + " |\n")
    
    # Hypothesis Validation
    report.append("\n## Hypothesis Validation\n\n")
    
    # Check if CSR/OffsetArray faster than AdjacencyList
    baseline = stats_df[stats_df['graph_type'] == 'AdjacencyListGraph']
    csr = stats_df[stats_df['graph_type'] == 'CSRGraph']
    offset = stats_df[stats_df['graph_type'] == 'OffsetArrayGraph']
    
    if len(baseline) > 0 and len(csr) > 0:
        baseline_runtime = baseline.iloc[0]['avg_runtime_ms']
        csr_runtime = csr.iloc[0]['avg_runtime_ms']
        speedup = baseline_runtime / csr_runtime
        report.append(f"**CSR vs AdjacencyList**: CSRGraph is {speedup:.2f}x {'faster' if speedup > 1 else 'slower'}\n")
    
    if len(baseline) > 0 and len(offset) > 0:
        offset_runtime = offset.iloc[0]['avg_runtime_ms']
        speedup = baseline_runtime / offset_runtime
        report.append(f"**OffsetArray vs AdjacencyList**: OffsetArrayGraph is {speedup:.2f}x {'faster' if speedup > 1 else 'slower'}\n")
    
    # Check if Matrix is slowest
    matrix = stats_df[stats_df['graph_type'] == 'MatrixGraph']
    if len(matrix) > 0:
        matrix_rank = stats_df.index[stats_df['graph_type'] == 'MatrixGraph'].tolist()[0] + 1
        total = len(stats_df)
        report.append(f"**MatrixGraph Performance**: Ranked {matrix_rank}/{total} (1 = fastest)\n")
    
    # Write report
    report_path = output_dir / 'analysis_report.md'
    with open(report_path, 'w') as f:
        f.write(''.join(report))
    
    print(f"Report saved to {report_path}")

def main():
    """Main analysis function"""
    print("=" * 60)
    print("Dijkstra Benchmark Analysis")
    print("=" * 60)
    
    # Load data
    df = load_and_clean_data(CSV_PATH)
    
    # Compute statistics
    stats_df = compute_summary_stats(df)
    
    # Save summary CSV
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    stats_df.to_csv(OUTPUT_DIR / 'summary_by_graph.csv', index=False)
    print(f"\nSummary statistics saved to {OUTPUT_DIR / 'summary_by_graph.csv'}")
    
    # Detect anomalies
    anomalies = detect_anomalies(df)
    
    # Correlation analysis
    correlations = correlation_analysis(df)
    
    # Create visualizations
    create_visualizations(df, stats_df, OUTPUT_DIR)
    
    # Generate report
    generate_report(df, stats_df, anomalies, correlations, OUTPUT_DIR)
    
    # Print summary to console
    print("\n" + "=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print("\nTop 5 Fastest Graphs:")
    for i, (idx, row) in enumerate(stats_df.head(5).iterrows(), 1):
        print(f"  {i}. {row['graph_type']}: {row['avg_runtime_ms']:.4f} ms (CoV: {row['coefficient_of_variation']:.2f}%)")
    
    print("\nCorrelations:")
    for name, corr in correlations.items():
        print(f"  {name}: {corr:.4f}")
    
    if anomalies:
        print("\nAnomalies:")
        for anomaly in anomalies:
            print(f"  - {anomaly}")
    
    print(f"\nAnalysis complete! Results saved to {OUTPUT_DIR}")

if __name__ == "__main__":
    main()

