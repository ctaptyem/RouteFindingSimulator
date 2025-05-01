from pathlib import Path
from matplotlib import pyplot as plt
from matplotlib.axes import Axes
import pandas as pd
import numpy as np

def plot_on_axes(ax: Axes, df: pd.DataFrame, x_col: str, y_col: str, title: str, log_x: bool, log_y: bool, x_name: str = None, y_name: str = None, ylim: float = None):
    # df['commtime_percent'] = df['commtime'] / df['runtime']
    if not x_name: x_name = x_col
    if not y_name: y_name = y_col

    # fig, ax = plt.subplots(figsize=(12, 8))
    ax.set_title(title)
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
    # ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)

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

    # plt.tight_layout()
    # plt.savefig(output_dir / f'{y_col}_vs_{x_col}.png', dpi=300, bbox_inches='tight')

def make_figure(output_dir: Path, df: pd.DataFrame, x_col: str, y_col: str, log_x: bool, log_y: bool, x_name: str = None, y_name: str = None, ylim: float = None, fig_dims: tuple[int, int] = (1,1), split_col: str = None):
    fig, axes = plt.subplots(figsize=(12, 8), nrows=fig_dims[0], ncols=fig_dims[1])
    if split_col:
        for idx, (name, group) in enumerate(df.groupby(split_col)):
            title = f"{y_name if y_name else y_col} for varied {x_name if x_name else x_col} with {split_col}={name}"
            plot_on_axes(axes[idx//fig_dims[1], idx%fig_dims[1]], group, x_col, y_col, title, log_x, log_y, x_name, y_name, ylim)
    else:
        title = f"{y_name if y_name else y_col} for varied {x_name if x_name else x_col}"
        plot_on_axes(axes, df, x_col, y_col, title, log_x, log_y, x_name, y_name, ylim)
    fig.legend()
    plt.tight_layout()
    plt.savefig(output_dir / f'{y_col}_vs_{x_col}.png', dpi=300, bbox_inches='tight')


def plot_all(measurement_file_name: str):
    # algorithm,pe_grid_size,node_count,edge_percentage,undirected,weight_mean,weight_std,edge_seed,weight_seed,runtime,commtime,commcount,total_read,total_write
    dfs = []
    for file in Path.iterdir(Path.cwd() / Path("measurements") / measurement_file_name):
        file_df = pd.read_csv(file)
        file_df['edge_update_type'] = file.name[:-4] # change this to something else for static?
        dfs.append(file_df)
    df = pd.concat(dfs)
    # df = df[df['pe_grid_size'] == 4]
    df['proc_count'] = np.square(df['pe_grid_size'])+1
    df['comm_estimate'] = (df['commcount']*100 + df['commvolume']*10)/1e9
    df['runtime'] = df['runtime']/1000
    df['total_time'] = df['runtime']+df['comm_estimate']
    df['commtime_percent'] = (df['comm_estimate']) / (df['total_time'])
    baseline_times = df[df['proc_count'] == 2].groupby('algorithm')['total_time'].mean().to_dict()
    df['speedup'] = df.apply(lambda row: (baseline_times[row['algorithm']]/row['total_time']), axis=1)
    df['efficiency'] = df['speedup']/(df['proc_count'] - 1)

    output_dir = Path.cwd() / Path("plots") / measurement_file_name
    Path.mkdir(output_dir, parents=True, exist_ok=True)
    

    # make_figure(output_dir, df, 'node_count', 'runtime', True, True, "Node Count", "Execution Time (ms)", ylim=0.0)
    make_figure(output_dir, df, 'node_count', 'total_time', True, True, "Node Count", "Execution Time (ms)", ylim=0.0)
    # make_figure(output_dir, df, 'edge_percentage', 'runtime', False, False, "Proportion of Edges", "Execution Time (ms)", ylim=0.0)
    make_figure(output_dir, df, 'proc_count', 'total_time', True, False, "Processor Count", "Execution Time (ms)", ylim=0.0)
    make_figure(output_dir, df, 'node_count', 'comm_estimate', True, True, "Node Count", "Communication Time (ms)", ylim=0.0)
    make_figure(output_dir, df, 'node_count', 'commtime_percent', False, False, "Node Count", "Percent of time spent communicating (%)", ylim=0.0)
    make_figure(output_dir, df, 'proc_count', 'commtime_percent', False, False, "Processor Count", "Percent of time spent communicating (%)", ylim=0.0)
    make_figure(output_dir, df, 'node_count', 'commtime_percent', False, False, "Node Count", "Percent of time spent communicating (%)", ylim=0.0)
    make_figure(output_dir, df, 'proc_count', 'speedup', False, False, "Processor Count", "Percent speedup (%)")
    make_figure(output_dir, df, 'proc_count', 'efficiency', False, False, "Processor Count", "Efficiency")

def explore_dataset():
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/input_graphs/OL.cedge', delimiter=' ')
    df.columns = ['edge_id,', 'node_A', 'node_B', 'length']
    plt.hist(df['length'])
    plt.yscale('log')
    plt.savefig('/home/andrei/Dev/RouteFindingSimulator/plots/length_distribution.png')
    print(min(df['length']))
    print(np.mean(df['length']))

if __name__ == "__main__":
    measurement_file_name = "low_density"
    plot_all(measurement_file_name)
    # explore_dataset()