#!python3

import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib
from matplotlib.ticker import ScalarFormatter
import pandas as pd
import numpy as np
import os
from pandas.core.common import flatten

def optlevel_to_label(level: int):
    o = int(level)
    if o <= 2:
        return "off"
    elif o == 3:
        return "optimised"

## Plot-specific settings
configs = [
    {
        "problems": {"Benchexpr", "Benchsym", "Benchtree", "Bubblesort", "Factorial", "Fibonacci", "Hanoi", "Mergesort",
                     "Quicksort", "Sieve"},
        "stages": {"execute"},
        "settings": {"yscale": "log"}
    },
    {
        # Apply to all problems
        "stages": {"compile", "execute"},
        "settings": {"ylim": (0, None)}
    },
    {
        "stages": {"Java space", "Class space"},
        "settings": {"ylabel": "Size (bytes)"}
    }
]

def configure_grid(g: sns.FacetGrid, problem=None):
    ## Global settings
    g.set_ylabels("Time (s)")
    g.set_xlabels("Input size")

    for ax in flatten(g.axes):
        ax.get_xaxis().set_major_locator(matplotlib.ticker.MaxNLocator(integer=True))

    # Specific settings
    try:
        for (row_val, col_val), ax in g.axes_dict.items():
            for config in configs:
                if "problems" not in config or row_val in config["problems"]:
                    if "stages" not in config or col_val in config["stages"]:
                        ax.set(**config["settings"])
    except (ValueError):
        for col_val, ax in g.axes_dict.items():
            for config in configs:
                if "problems" not in config or problem in config["problems"]:
                    if "stages" not in config or col_val in config["stages"]:
                        ax.set(**config["settings"])
    finally:
        return g

def load_data(file):
    df_raw = pd.read_csv(file)
    df_raw["optimisationLevel"] = np.vectorize(optlevel_to_label)(df_raw["optimisationLevel"])
    df_raw["optimisationLevel"] = df_raw["optimisationLevel"].astype("string")
    return df_raw


def allstages_plot(df_scaledproblem):
    return configure_grid(sns.relplot(
        data=df_scaledproblem,
        x="size",
        y="score",
        row="problem",
        row_order=np.sort(np.unique(df_scaledproblem["problem"].values)),
        col="stage",
        col_order=[s for s in ["compile", "execute"] if
                   s in df_scaledproblem["stage"].values],
        hue="optimisationLevel",
        hue_order=map(optlevel_to_label, ["2", "3"]),
        kind="line",
        #     err_style="band",
        markers=True,
        facet_kws=dict(
            sharex=False,
            sharey=False,
        ),
    ))
    # TODO Include strj runtime -> with and without fusion

# Runtimes for DFA switch backends (per problem)
def runtime_plot(df_scaledproblem):
    cols = np.sort(np.unique(df_scaledproblem["problem"].values))
    opt_levels = list(map(optlevel_to_label, ["2", "3"]))

    g = configure_grid(sns.relplot(
        data=df_scaledproblem[
            (df_scaledproblem["stage"] == "execute")
            & (df_scaledproblem["optimisationLevel"].isin(opt_levels))],
        x="size",
        y="score",
        col="problem",
        col_order=cols,
        col_wrap=min(3, len(cols)),
        hue="optimisationLevel",
        hue_order=opt_levels,
        kind="line",
        #     err_style="band",
        markers=True,
        facet_kws=dict(
            sharex=False,
            sharey=False,
        ),
    ))
    # TODO Include strj runtime -> with and without fusion
    g.set(yscale="log")
    return g

# Runtimes for DFA switch backends (per problem)
def fact_bub_plot(df_scaledproblem):
    cols = ["Factorial", "Bubblesort"]
    opt_levels = list(map(optlevel_to_label, ["2", "3"]))

    g = configure_grid(sns.relplot(
        data=df_scaledproblem[
            (df_scaledproblem["stage"] == "execute") & (df_scaledproblem["optimisationLevel"].isin(opt_levels))],
        x="size",
        y="score",
        col="problem",
        col_order=cols,
        col_wrap=min(3, len(cols)),
        style="optimisationLevel",
        hue="optimisationLevel",
        hue_order=opt_levels,
        kind="line",
        markers=True,
        facet_kws=dict(
            sharex=False,
            sharey=False,
        ),
    ))
    # TODO Include strj runtime -> with and without fusion
    sns.move_legend(g, "upper left", frameon=True, bbox_to_anchor=(0.1, 0.9))
    g.set(yscale="log")
    for _, ax in g.axes_dict.items():
        ax.yaxis.set_major_formatter(ScalarFormatter(1))
        ax.grid(True, 'minor', 'y')
    g.set_titles(None, None, "{col_name}")
    g.set_axis_labels("Input size", "Time (s) - logarithmic")
    return g

# Runtimes for DFA switch backends (per problem)
# def til_plot(df):
#     cols = ["Factorial", "Bubblesort"]
#     opt_levels = list(map(optlevel_to_label, ["2", "3"]))
#
#     df_til_add = df[(df["problem"] == "add") & (df["optimisationLevel"].isin(opt_levels))].copy()
#     df_til_add["stage"] = df_til_add["stage"].map(
#         lambda s: "Interpret" if s == "execute" else "Optimise" if s == "compile" else s)
#
#     g = configure_grid(sns.relplot(
#         data=df_til_add,
#         x="size",
#         y="score",
#         col="stage",
#         col_wrap=min(3, len(cols)),
#         style="optimisationLevel",
#         hue="optimisationLevel",
#         hue_order=opt_levels,
#         kind="line",
#         markers=True,
#         facet_kws=dict(
#             sharex=False,
#             sharey=False,
#         ),
#     ))
#
#     sns.move_legend(g, "upper left", frameon=True, bbox_to_anchor=(0.1, 0.9))
#     # g.set(yscale="log")
#     for _, ax in g.axes_dict.items():
#         ax.grid(True, 'minor', 'y')
#     g.set_titles(None, None, "{col_name}")
#     g.set_axis_labels("Number of variables/additions", "Time (s)")
#     # TODO Include strj runtime -> with and without fusion
#     return g

# def unscaled_plot(df):
#     opt_levels = list(map(optlevel_to_label, ["3", "4"]))
#     df_unscaledproblem = df[(df["Problem Size"] == -1) & (df["Pattern Match Compilation"].isin(opt_levels))]
#     data = df_unscaledproblem.loc[
#         df_unscaledproblem["Benchmark"].str.contains("stratego")].copy()
#
#     data2 = pd.DataFrame()
#
#     for problem, df_problem in data.groupby("Problem"):
#         for stage, df_p_stage in df_problem.groupby("Stage"):
#             median = df_p_stage[df_p_stage["Codegen Implementation"] == ""]["Score"].median()
#             data2 = pd.concat([data2, df_p_stage.assign(MedianScore=lambda x: median)], ignore_index=True)
#
#     data2["Score"] = data2["Score"] / data2["MedianScore"]
#
#     g = sns.catplot(
#         data=data2,
#         x="Problem",
#         y="Score",
#         col="Stage",
#         col_order=[s for s in ["compileStratego", "compileJava"] if s in df_unscaledproblem["Stage"].values],
#         hue="Pattern Match Compilation",
#         kind="box",
#         sharey=False,
#     )
#
#     g.set_ylabels("Factor vs. no PMC - logarithmic")
#     for _, ax in g.axes_dict.items():
#         locs = ax.get_xticks()
#         labels = ax.get_xticklabels()
#         ax.set_xticklabels(labels, rotation=90)
#         ax.grid(True, 'minor', 'y')
#         ax.set_xticks([s - 0.5 for s in locs], minor=True)
#         ax.grid(True, 'minor', 'x', alpha=0.5)
#         ax.set_yscale('log')
#         ax.yaxis.set_major_formatter(ScalarFormatter(1))
#         ax.yaxis.set_minor_formatter(ScalarFormatter(1))
#     sns.move_legend(g, "upper left", frameon=True, bbox_to_anchor=(0.47, 0.8))
#     g.set_titles(None, None, "{col_name}")
#
#     return g

def main(output_dir, file):
    # sns.set_theme(font='Linux Biolinum O', font_scale=1.2)
    plt.rc('pdf', fonttype=42)

    df = load_data(file)

    df_stratego = df[df["language"].str.lower() == "stratego"]
    df_stratego_scaled = df_stratego[df["size"].notna()]

    # df_til = df[df["language"].str.lower() == "til"]

    basename = file.split("/")[-1].split('.')[0]
    dir = f"{output_dir}/{basename}"
    os.makedirs(dir, exist_ok=True)

    allstages_plot(df_stratego_scaled).savefig(f"{dir}/allstages.png")
    runtime_plot(df_stratego_scaled).savefig(f"{dir}/runtime.png")
    fact_bub_plot(df_stratego_scaled).savefig(f"{dir}/fact&bub-run.pdf")
    # til_plot(df).savefig(f"{dir}/til.pdf")
    # unscaled_plot(df).savefig(f"{dir}/unscaled.pdf")


if __name__ == '__main__':
    # main("plots", "../data/benchmark_results/processed/20240107-195853_.json.csv")
    main("plots", "../data/benchmark_results/processed/20240108-110702_.json.csv")
    # main(sys.argv[1], sys.argv[2:])
