from matplotlib import pyplot as plt
import pandas as pd

def plot_node_count_v_time():
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/measurements/test_commtime2.csv')

    fig, ax = plt.subplots(figsize=(12, 8))
    fig.suptitle("Performance of algorithms for varied node count")
    ax.set_xlabel("# of nodes in graph (log-scale)")
    ax.set_ylabel("Execution time (ms) (log-scale)")

    grouped_data = df.groupby(['algorithm', 'node_count']).agg({'runtime': ['mean', 'std']}).reset_index()
    # Flatten the multi-level columns
    grouped_data.columns = ['algorithm', 'node_count', 'runtime_mean', 'runtime_std']

    for algo in df['algorithm'].unique():
        algo_data = grouped_data[grouped_data['algorithm'] == algo]
        
        # Plot the mean line
        ax.plot(algo_data['node_count'], algo_data['runtime_mean'], 
                marker='o', linewidth=2, label=algo)
        
        # Plot the error range (shaded area)
        ax.fill_between(algo_data['node_count'], 
                    algo_data['runtime_mean'] - algo_data['runtime_std'],
                    algo_data['runtime_mean'] + algo_data['runtime_std'],
                    alpha=0.1)

    ax.grid(True, linestyle='--', alpha=0.7)
    ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)

    ax.set_yscale("log")
    ax.set_xscale("log")
    # Keep x-ticks at node count values
    ax.set_xticks(df['node_count'].unique())
    ax.set_xticklabels([str(nc) for nc in df['node_count'].unique()])
    ax.get_xaxis().set_major_formatter(plt.ScalarFormatter())
    ax.get_xaxis().set_tick_params(which='minor', size=0)
    ax.get_xaxis().set_tick_params(which='minor', width=0)

    plt.tight_layout()
    plt.savefig('/home/andrei/Dev/RouteFindingSimulator/plots/node_count_v_time2.png', dpi=300, bbox_inches='tight')


def plot_edge_prop_v_time():
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/measurements/test_commtime.csv')

    fig, ax = plt.subplots(figsize=(12, 8))
    fig.suptitle("Performance of algorithms for varied node count")
    ax.set_xlabel("Percent of possible edges in graph")
    ax.set_ylabel("Execution time (ms) (log-scale)")

    grouped_data = df.groupby(['algorithm', 'edge_percentage']).agg({'runtime': ['mean', 'std']}).reset_index()
    # Flatten the multi-level columns
    grouped_data.columns = ['algorithm', 'edge_percentage', 'runtime_mean', 'runtime_std']

    for algo in df['algorithm'].unique():
        algo_data = grouped_data[grouped_data['algorithm'] == algo]
        
        # Plot the mean line
        ax.plot(algo_data['edge_percentage'], algo_data['runtime_mean'], 
                marker='o', linewidth=2, label=algo)
        
        # Plot the error range (shaded area)
        ax.fill_between(algo_data['edge_percentage'], 
                    algo_data['runtime_mean'] - algo_data['runtime_std'],
                    algo_data['runtime_mean'] + algo_data['runtime_std'],
                    alpha=0.1)

    ax.grid(True, linestyle='--', alpha=0.7)
    ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)

    # ax.set_yscale("log")
    # ax.set_xscale("log")
    # Keep x-ticks at node count values
    ax.set_xticks(df['edge_percentage'].unique())
    ax.set_xticklabels([str(nc) for nc in df['edge_percentage'].unique()])
    ax.get_xaxis().set_major_formatter(plt.ScalarFormatter())
    ax.get_xaxis().set_tick_params(which='minor', size=0)
    ax.get_xaxis().set_tick_params(which='minor', width=0)

    plt.tight_layout()
    plt.savefig('/home/andrei/Dev/RouteFindingSimulator/plots/edge_prop_v_time.png', dpi=300, bbox_inches='tight')


def plot_pe_count_v_time():
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/measurements/test_commtime.csv')

    fig, ax = plt.subplots(figsize=(12, 8))
    fig.suptitle("Performance of algorithms for varied node count")
    ax.set_xlabel("Processing Element Grid Size")
    ax.set_ylabel("Execution time (ms) (log-scale)")

    grouped_data = df.groupby(['algorithm', 'pe_grid_size']).agg({'runtime': ['mean', 'std']}).reset_index()
    # Flatten the multi-level columns
    grouped_data.columns = ['algorithm', 'pe_grid_size', 'runtime_mean', 'runtime_std']

    for algo in df['algorithm'].unique():
        algo_data = grouped_data[grouped_data['algorithm'] == algo]
        
        # Plot the mean line
        ax.plot(algo_data['pe_grid_size'], algo_data['runtime_mean'], 
                marker='o', linewidth=2, label=algo)
        
        # Plot the error range (shaded area)
        ax.fill_between(algo_data['pe_grid_size'], 
                    algo_data['runtime_mean'] - algo_data['runtime_std'],
                    algo_data['runtime_mean'] + algo_data['runtime_std'],
                    alpha=0.1)

    ax.grid(True, linestyle='--', alpha=0.7)
    ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)

    ax.set_yscale("log")
    ax.set_xscale("log")
    # Keep x-ticks at node count values
    ax.set_xticks(df['pe_grid_size'].unique())
    ax.set_xticklabels([str(nc) for nc in df['pe_grid_size'].unique()])
    ax.get_xaxis().set_major_formatter(plt.ScalarFormatter())
    ax.get_xaxis().set_tick_params(which='minor', size=0)
    ax.get_xaxis().set_tick_params(which='minor', width=0)

    plt.tight_layout()
    plt.savefig('/home/andrei/Dev/RouteFindingSimulator/plots/pe_count_v_time.png', dpi=300, bbox_inches='tight')

def plot_node_count_v_commtime():
    df = pd.read_csv('/home/andrei/Dev/RouteFindingSimulator/measurements/test_commtime2.csv')
    df.loc[df['algorithm'] == 'DijkstraMCU', 'commtime'] = 0
    df['commtime_percent'] = df['commtime'] / df['runtime']

    fig, ax = plt.subplots(figsize=(12, 8))
    fig.suptitle("Performance of algorithms for varied node count")
    ax.set_xlabel("# of nodes in graph (log-scale)")
    ax.set_ylabel("Percentage of time communicating (ms) (log-scale)")

    grouped_data = df.groupby(['algorithm', 'node_count']).agg({'commtime_percent': ['mean', 'std']}).reset_index()
    # Flatten the multi-level columns
    grouped_data.columns = ['algorithm', 'node_count', 'commtime_mean', 'commtime_std']

    for algo in df['algorithm'].unique():
        algo_data = grouped_data[grouped_data['algorithm'] == algo]
        
        # Plot the mean line
        ax.plot(algo_data['node_count'], algo_data['commtime_mean'], 
                marker='o', linewidth=2, label=algo)
        
        # Plot the error range (shaded area)
        ax.fill_between(algo_data['node_count'], 
                    algo_data['commtime_mean'] - algo_data['commtime_std'],
                    algo_data['commtime_mean'] + algo_data['commtime_std'],
                    alpha=0.1)

    ax.grid(True, linestyle='--', alpha=0.7)
    ax.legend(title='Algorithm', fontsize=14, title_fontsize=14)

    # ax.set_yscale("log")
    ax.set_xscale("log")
    # Keep x-ticks at node count values
    ax.set_xticks(df['node_count'].unique())
    ax.set_xticklabels([str(nc) for nc in df['node_count'].unique()])
    ax.get_xaxis().set_major_formatter(plt.ScalarFormatter())
    ax.get_xaxis().set_tick_params(which='minor', size=0)
    ax.get_xaxis().set_tick_params(which='minor', width=0)

    plt.tight_layout()
    plt.savefig('/home/andrei/Dev/RouteFindingSimulator/plots/node_count_v_commtime2.png', dpi=300, bbox_inches='tight')



def main():
    plot_node_count_v_commtime()

if __name__ == "__main__":
    main()