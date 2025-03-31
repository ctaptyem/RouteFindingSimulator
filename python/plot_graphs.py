from matplotlib import pyplot as plt
import pandas as pd
import numpy as np

def plot(df: pd.DataFrame, x_col: str, y_col: str, log_x: bool, log_y: bool, x_name: str = None, y_name: str = None, ylim: float = None):
    # df['commtime_percent'] = df['commtime'] / df['runtime']
    if not x_name: x_name = x_col
    if not y_name: y_name = y_col

    fig, ax = plt.subplots(figsize=(12, 8))
    fig.suptitle(f"{y_name} for varied {x_name}")
    ax.set_xlabel(f"{x_name} {'(log scale)' if log_x else ''}")
    ax.set_ylabel(f"{y_name} {'(log scale)' if log_y else ''}")

    grouped_data = df.groupby(['algorithm', x_col]).agg({y_col: ['mean', 'std']}).reset_index()
    # Flatten the multi-level columns
    grouped_data.columns = ['algorithm', x_col, f'{y_col}_mean', f'{y_col}_std']

    for algo in df['algorithm'].unique():
        algo_data = grouped_data[grouped_data['algorithm'] == algo]
        
        # Plot the mean line
        ax.plot(algo_data[x_col], algo_data[f'{y_col}_mean'], 
                marker='o', linewidth=2, label=algo)
        
        # Plot the error range (shaded area)
        ax.fill_between(algo_data[x_col], 
                    algo_data[f'{y_col}_mean'] - algo_data[f'{y_col}_std'],
                    algo_data[f'{y_col}_mean'] + algo_data[f'{y_col}_std'],
                    alpha=0.1)

    ax.grid(True, linestyle='--', alpha=0.7)
    ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)

    if log_x: ax.set_xscale("log")
    if log_y: ax.set_yscale("log")
    # Keep x-ticks at node count values
    ax.set_xticks(df[x_col].unique())
    ax.set_xticklabels([str(nc) for nc in df[x_col].unique()])
    ax.get_xaxis().set_major_formatter(plt.ScalarFormatter())
    ax.get_xaxis().set_tick_params(which='minor', size=0)
    ax.get_xaxis().set_tick_params(which='minor', width=0)

    if ylim is not None and not log_y:
        ax.set_ylim(ylim)

    plt.tight_layout()
    plt.savefig(f'/home/andrei/Dev/RouteFindingSimulator/plots/{y_col}_vs_{x_col}.png', dpi=300, bbox_inches='tight')


def main():
    # algorithm,pe_grid_size,node_count,edge_percentage,undirected,weight_mean,weight_std,edge_seed,weight_seed,runtime,commtime,commcount,total_read,total_write
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/measurements/new_model_commtime_2.csv')
    df['proc_count'] = np.square(df['pe_grid_size'])+1
    df['commcount'] = df['commcount']/100.0
    df['commtime_percent'] = df['commcount'] / (df['runtime']+df['commcount'])

    plot(df, 'node_count', 'runtime', True, True, "Node Count", "Execution Time (ms)", ylim=0.0)
    plot(df, 'edge_percentage', 'runtime', False, False, "Proportion of Edges", "Execution Time (ms)", ylim=0.0)
    plot(df, 'proc_count', 'runtime', True, False, "Processor Count", "Execution Time (ms)", ylim=0.0)
    plot(df, 'node_count', 'commcount', True, True, "Node Count", "Communication Time (ms)", ylim=0.0)
    plot(df, 'node_count', 'commtime_percent', True, False, "Node Count", "Percent of time spent communicating (%)", ylim=0.0)


if __name__ == "__main__":
    main()