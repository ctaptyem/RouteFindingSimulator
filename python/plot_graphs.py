from pathlib import Path
from matplotlib import pyplot as plt
from matplotlib.axes import Axes
from matplotlib.ticker import ScalarFormatter
import pandas as pd
import numpy as np

def plot_on_axes(ax: Axes, df: pd.DataFrame, x_col: str, y_col: str, title: str, log_x: bool, log_y: bool, x_name: str = None, y_name: str = None, ylim: float = None, style: dict[str,tuple[str, str]] = None):
    if not x_name: x_name = x_col
    if not y_name: y_name = y_col

    # fig, ax = plt.subplots(figsize=(12, 8))
    # ax.set_title(title)
    ax.set_xlabel(f"{x_name} {'(log scale)' if log_x else ''}")
    ax.set_ylabel(f"{y_name} {'(log scale)' if log_y else ''}")

    grouped_data = df.groupby(['algorithm', x_col]).agg({y_col: ['mean', 'std']}).reset_index()
    grouped_data.columns = ['algorithm', x_col, f'{y_col}_mean', f'{y_col}_std']

    # for algo in df['algorithm'].unique():
    #     ax.scatter(df.loc[df['algorithm'] == algo, x_col], df.loc[df['algorithm'] == algo, y_col], label=algo, s=9, color=style[algo][0])

    for algo in df['algorithm'].unique():
        algo_data = grouped_data[grouped_data['algorithm'] == algo]
        
        ax.plot(algo_data[x_col], algo_data[f'{y_col}_mean'], linewidth=2, label=algo, color=style[algo][0], marker=style[algo][1])
                
        ax.fill_between(algo_data[x_col], 
                    algo_data[f'{y_col}_mean'] - 1.645 * algo_data[f'{y_col}_std'],
                    algo_data[f'{y_col}_mean'] + 1.645 * algo_data[f'{y_col}_std'],
                    alpha=0.2, color=style[algo][0])

    ax.grid(True, linestyle='--', alpha=0.7)
    # ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)
    if log_x: ax.set_xscale("log", base=2)
    lb = np.inf
    ub = 0
    if log_y: 
        ax.set_yscale("log", base=2)
        ax.yaxis.set_major_formatter(ScalarFormatter())
        ax.minorticks_off()
        # lb = np.round(0.09*np.min(grouped_data[f'{y_col}_mean']))*10
        # print(np.min(grouped_data[f'{y_col}_mean']))
        # print(lb)
        # ub = np.round(0.11*np.max(grouped_data[f'{y_col}_mean']))*10
        # ax.set_yticks(np.arange(lb, ub, step=30))
    ax.set_xticks(df[x_col].unique())
    ax.set_xticklabels([str(nc) for nc in df[x_col].unique()], rotation=-30)
    ax.get_xaxis().set_major_formatter(plt.ScalarFormatter())
    ax.get_xaxis().set_tick_params(which='minor', size=0)
    ax.get_xaxis().set_tick_params(which='minor', width=0)
    ax.legend();

    if ylim is not None and not log_y:
        ax.set_ylim(ylim)

    # plt.tight_layout()
    # plt.savefig(output_dir / f'{y_col}_vs_{x_col}.png', dpi=300, bbox_inches='tight')


def make_figure(output_dir: Path, df: pd.DataFrame, x_col: str, y_col: str, log_x: bool, log_y: bool, x_name: str = None, y_name: str = None, ylim: float = None, fig_dims: tuple[int, int] = (1,1), split_col: str = None, style: dict[str,tuple[str, str]] = {}):
    fig, axes = plt.subplots(figsize=(6, 4), nrows=fig_dims[0], ncols=fig_dims[1])
    if split_col:
        for idx, (name, group) in enumerate(df.groupby(split_col)):
            title = f"{y_name if y_name else y_col} for varied {x_name if x_name else x_col} with {split_col}={name}"
            plot_on_axes(axes[idx//fig_dims[1], idx%fig_dims[1]], group, x_col, y_col, title, log_x, log_y, x_name, y_name, ylim, style)
    else:
        title = f"{y_name if y_name else y_col} for varied {x_name if x_name else x_col}"
        plot_on_axes(axes, df, x_col, y_col, title, log_x, log_y, x_name, y_name, ylim, style)
    # fig.legend()
    plt.tight_layout()
    plt.savefig(output_dir / f'{output_dir.name}_{y_col}_vs_{x_col}.png', dpi=300, bbox_inches='tight')





def plot_all(measurement_file_name: str):
    # algorithm,pe_grid_size,node_count,edge_percentage,undirected,weight_mean,weight_std,edge_seed,weight_seed,runtime,commtime,commcount,total_read,total_write
    dfs = []
    for file in Path.iterdir(Path.cwd() / Path("measurements") / measurement_file_name):
        file_df = pd.read_csv(file)
        file_df['edge_update_type'] = file.name[:-4]
        dfs.append(file_df)
    df = pd.concat(dfs)

    algo_rename = {"DijkstraMCU": "Dijkstra", "FoxOttoMCU": "FoxOtto", "CannonsMCU": "Cannons", "DynamicMCU": "MA"}

    df['algorithm'] = df.apply(lambda row: algo_rename[row['algorithm']], axis=1)

    # df = df[(df['algorithm'] == "Dijkstra")]
    # df = df[df['pe_grid_size'] < 8]
    # df = df[df['pe_grid_size'] == 6]

    # df = df[df['avg_degree'] == 2.5]

    # df = df[df['node_count'] >= 200]
    # df = df[df['node_count'] <= 1200]
    # df = df[df['node_count'] == 1000]

    # df = df[df['edge_update_type'] != "increase_edge"]
    # df = df[df['edge_update_type'] != "remove_edge"]
    # df = df[df['edge_update_type'] != "decrease_edge"]
    # df = df[df['edge_update_type'] != "add_edge"]

    df['proc_count'] = np.square(df['pe_grid_size'])

    df['comm_estimate'] = (df['commcount']*500 + df['commvolume']*10)/1e9
    df.loc[df['pe_grid_size'] == 1, 'comm_estimate'] = 0
    df['runtime'] = df['runtime']/1000
    df['total_time'] = np.round(df['runtime']+df['comm_estimate']+0.01,2)
    df['commtime_percent'] = df['comm_estimate'] / df['total_time']
    baseline_times = df[df['proc_count'] == 1].groupby('algorithm')['total_time'].mean().to_dict() 
    df['speedup'] = df.apply(lambda row: (baseline_times[row['algorithm']]/row['total_time'] if baseline_times else 1), axis=1)
    df['efficiency'] = df['speedup']/df['proc_count']

    output_dir = Path.cwd() / Path("plots") / measurement_file_name
    Path.mkdir(output_dir, parents=True, exist_ok=True)
    
    style = {"Dijkstra": ("#1f77b4","o"), "FoxOtto": ("#ff7f0e", "D"), "Cannons": ("#2ca02c", "s"), "MA": ("#d62728", "^")}

    make_figure(output_dir, df, 'node_count', 'total_time', False, True, "Node Count", "Execution Time (s)", ylim=0.0, style=style)
    # make_figure(output_dir, df, 'node_count', 'runtime', True, True, "Node Count", "Execution Time (s)", algo_color=color)
    make_figure(output_dir, df, 'node_count', 'commtime_percent', False, False, "Node Count", "Proportion of time spent communicating", style=style)
    make_figure(output_dir, df, 'node_count', 'comm_estimate', False, False, "Node Count", "Communication Time (s)", ylim=0.0, style=style)
    make_figure(output_dir, df, 'node_count', 'efficiency', False, False, "Node Count", "Efficiency", style=style)

    make_figure(output_dir, df, 'proc_count', 'total_time', False, True, "Processor Count", "Execution Time (s)", ylim=0.0, style=style)
    make_figure(output_dir, df, 'proc_count', 'comm_estimate', False, False, "Processor Count", "Communication Time (s)", ylim=0.0, style=style)
    make_figure(output_dir, df, 'proc_count', 'commtime_percent', False, False, "Processor Count", "Proportion of time spent communicating", style=style)
    make_figure(output_dir, df, 'proc_count', 'speedup', False, False, "Processor Count", "Speedup", style=style)
    make_figure(output_dir, df, 'proc_count', 'efficiency', False, False, "Processor Count", "Efficiency", style=style)
    # make_figure(output_dir, df, 'is_compressed', 'total_time', False, False, "Compressed", "Execution Time (s)", style=style)


def explore_dataset():
    import networkx as nx
    import json
    # import nx_cugraph as cg
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/input_graphs/cal.cedge', delimiter=' ')
    df.columns = ['edge_id,', 'node_A', 'node_B', 'length']
    G = nx.Graph()
    for idx, row in df.iterrows():
        G.add_edge(row['node_A'], row['node_B'], weight=row['length'])
    print("Built graph")
    G.to_undirected()
    print("Made undirected")
    nx.config.backends.parallel.active = True
    nx.config.backends.parallel.n_jobs = 16
    betweenness = nx.edge_betweenness_centrality(G)
    with open("cal_betweenness.csv", 'w') as outfile:
        outfile.write("A,B,v\n")
        for (a,b),v in betweenness.items():
            outfile.write(f"{int(a)},{int(b)},{v}\n")

def plot_theoretical_performance():
    from sklearn.linear_model import LinearRegression
    dfs = []
    for file in Path.iterdir(Path.cwd() / Path("measurements") / "dynamic_random"):
        file_df = pd.read_csv(file)
        file_df['edge_update_type'] = file.name[:-4] 
        dfs.append(file_df)
    df = pd.concat(dfs)

    algo_rename = {"DijkstraMCU": "Dijkstra", "FoxOttoMCU": "FoxOtto", "CannonsMCU": "Cannon", "DynamicMCU": "MA"}
    df['algorithm'] = df.apply(lambda row: algo_rename[row['algorithm']], axis=1)

    algo = "Dijkstra"
    df = df[(df['algorithm'] == algo)]
    # node_list = [100,200,300,400,500,600,800,1000,1200]
    # df['node_count'] = df['node_count'].apply(lambda v: min(node_list, key=lambda x:abs(x-v)))
    df = df[df['pe_grid_size'] == 6]
    # df = df[df['node_count'] == 1000]

    df['proc_count'] = np.square(df['pe_grid_size'])

    df['comm_estimate'] = (df['commcount']*500 + df['commvolume']*10)/1e9
    df.loc[df['pe_grid_size'] == 1, 'comm_estimate'] = 0
    df['runtime'] = df['runtime']/1000
    df['total_time'] = np.round(df['runtime']+df['comm_estimate']+0.01,2)
    df['commtime_percent'] = df['comm_estimate'] / df['total_time']
    baseline_times = df[df['proc_count'] == 1].groupby('algorithm')['total_time'].mean().to_dict() 
    df['speedup'] = df.apply(lambda row: (baseline_times[row['algorithm']]/row['total_time'] if baseline_times else 1), axis=1)
    df['efficiency'] = df['speedup']/df['proc_count']

    time_reg = LinearRegression(fit_intercept=False)
    comm_reg = LinearRegression(fit_intercept=False)

    df['time_complexity'] = df.apply(lambda row: ((row['node_count'] ** 3) * np.log2(row['node_count']))/16, axis=1)
    df['message_complexity'] = df.apply(lambda row: row['node_count']+16 if row['algorithm'] == "Dijkstra" else (16 * np.log2(row['node_count'])), axis=1)
    df['communication_complexity'] = df.apply(lambda row: (row['node_count'] ** 2) * 16 if row['algorithm'] == "Dijkstra" else ((row['node_count'] ** 2) * np.log2(row['node_count'])), axis=1)
    time_reg.fit(np.asarray(df[['time_complexity']]).reshape(-1, 1), np.asarray(df['runtime']).reshape(-1, 1))
    comm_reg.fit(np.asarray(df[['message_complexity', 'communication_complexity']]), np.asarray(df['comm_estimate']).reshape(-1, 1))
    df['corrected_theoretical_time'] = df.apply(lambda row: time_reg.predict([[row['time_complexity']],]) + comm_reg.predict([[row['message_complexity'], row['communication_complexity']]]), axis=1)


    fig, ax = plt.subplots(figsize=(5, 4))
    style = {"Dijkstra": ("#1f77b4","o"), "FoxOtto": ("#ff7f0e", "D"), "Cannon": ("#2ca02c", "s"), "MA": ("#d62728", "^")}


    grouped_data = df.groupby(['node_count']).agg({'total_time': ['mean', 'std'], 'corrected_theoretical_time': ['mean', 'std']}).reset_index()
    grouped_data.columns = ['node_count', f'total_time_mean', f'total_time_std', f'corrected_theoretical_time_mean', f'corrected_theoretical_time_std']

    ax.plot(grouped_data['node_count'], grouped_data[f'total_time_mean'], linewidth=2, label=f'Simulated {algo}', color=style[algo][0], marker=style[algo][1])
    ax.plot(grouped_data['node_count'], grouped_data[f'corrected_theoretical_time_mean'], linewidth=2, label=f'Theoretical {algo}', color=style[algo][0], linestyle="--")
                
    ax.fill_between(grouped_data['node_count'], 
                grouped_data['total_time_mean'] - 1.645 * grouped_data['total_time_std'],
                grouped_data['total_time_mean'] + 1.645 * grouped_data['total_time_std'],
                alpha=0.2, color=style[algo][0])


    ax.grid(True, linestyle='--', alpha=0.7)
    # ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)
    # ax.set_yscale("log", base=2)
    # ax.yaxis.set_major_formatter(ScalarFormatter())
    # ax.minorticks_off()
    # ax.set_xticks(df["node_count"].unique())
    # ax.set_xticklabels([str(nc) for nc in df["node_count"].unique()], rotation=-30)
    ax.get_xaxis().set_major_formatter(plt.ScalarFormatter())
    ax.get_xaxis().set_tick_params(which='minor', size=0)
    ax.get_xaxis().set_tick_params(which='minor', width=0)
    ax.set_title(f'Theoretical and measured performance of {algo}')
    ax.set_ylabel("Execution Time (s)")
    ax.set_xlabel("Node Count")
    ax.legend();

    fig.savefig("theoretical_performance.png")

if __name__ == "__main__":
    measurement_file_name = "cal_diff_PE_3"
    plot_all(measurement_file_name)
    # explore_dataset()
    # plot_theoretical_performance()