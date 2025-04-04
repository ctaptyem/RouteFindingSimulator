from pathlib import Path
from matplotlib import pyplot as plt
import pandas as pd
import numpy as np

def plot(output_dir:Path, df: pd.DataFrame, x_col: str, y_col: str, log_x: bool, log_y: bool, x_name: str = None, y_name: str = None, ylim: float = None):
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
    plt.savefig(output_dir / f'{y_col}_vs_{x_col}.png', dpi=300, bbox_inches='tight')


def plot_all(measurement_file_name: str):
    # algorithm,pe_grid_size,node_count,edge_percentage,undirected,weight_mean,weight_std,edge_seed,weight_seed,runtime,commtime,commcount,total_read,total_write
    dfs = []
    for file in Path.iterdir(Path.cwd() / Path("measurements") / measurement_file_name):
        file_df = pd.read_csv(file)
        file_df['edge_update_type'] = file.name[:-4]
        dfs.append(file_df)
    df = pd.concat(dfs)
    df['proc_count'] = np.square(df['pe_grid_size'])+1
    df['comm_estimate_ns'] = (df['commcount']*100 + df['commvolume']*10)/1e9
    df['runtime_ns'] = df['runtime']/1000
    df['commtime_percent'] = (df['comm_estimate_ns']) / (df['runtime_ns']+df['comm_estimate_ns'])

    output_dir = Path.cwd() / Path("plots") / measurement_file_name
    Path.mkdir(output_dir, parents=True, exist_ok=True)


    plot(output_dir, df, 'node_count', 'runtime', True, True, "Node Count", "Execution Time (ms)", ylim=0.0)
    plot(output_dir, df, 'edge_percentage', 'runtime', False, False, "Proportion of Edges", "Execution Time (ms)", ylim=0.0)
    plot(output_dir, df, 'proc_count', 'runtime', True, False, "Processor Count", "Execution Time (ms)", ylim=0.0)
    plot(output_dir, df, 'node_count', 'commcount', True, True, "Node Count", "Communication Time (ms)", ylim=0.0)
    plot(output_dir, df, 'node_count', 'commtime_percent', False, False, "Node Count", "Percent of time spent communicating (%)", ylim=0.0)
    plot(output_dir, df, 'proc_count', 'commtime_percent', False, False, "Processor Count", "Percent of time spent communicating (%)", ylim=0.0)

def explore_dataset():
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/testing/input/OL.cedge', delimiter=' ')
    df.columns = ['edge_id,', 'node_A', 'node_B', 'length']
    plt.hist(df['length'])
    plt.yscale('log')
    plt.savefig('/home/andrei/Dev/RouteFindingSimulator/plots/length_distribution.png')
    print(min(df['length']))
    print(np.mean(df['length']))

if __name__ == "__main__":
    measurement_file_name = "first_dynamic"
    plot_all(measurement_file_name)
    # explore_dataset()