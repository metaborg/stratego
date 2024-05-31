#!python3

import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib
from matplotlib.ticker import ScalarFormatter
import pandas as pd
import numpy as np
import os
import json
import re
import sys
from pandas.core.common import flatten

def optlevel_to_label(level: str):
    o = int(level)
    if o <= 2:
        return "off"
    elif o == 3:
        return "off+inlining"
    elif o == 4:
        return "on"
    elif o == 5:
        return "on+inlining"


def read_json(path):
    data = list()
    columns = [
        "Benchmark", 
        "Mode", 
        "Threads", 
        "Samples", 
        "Score", 
        "Score Error (99.9%)", 
        "Unit", 
        "Param: optimisationLevel", 
        "Param: problemSize", 
        "Param: sharedConstructors", 
        "Param: switchImplementation"
    ]
    
    with open(path, 'r') as f:
        for row in json.load(f):
            params = row["params"]
            problemSize = params.get("problemSize", "-1")
            for score in row["primaryMetric"]["rawData"][0]:
                x = [
                    row["benchmark"], 
                    row["mode"], 
                    row["threads"], 
                    1, 
                    score, 
                    "", 
                    row["primaryMetric"]["scoreUnit"], 
                    params["optimisationLevel"], 
                    problemSize, 
                    params["sharedConstructors"], 
                    params["switchImplementation"]
                ]
                data.append(x)
    df = pd.DataFrame(data, columns=columns)
    df["Param: optimisationLevel"] = df["Param: optimisationLevel"].astype("int")
    df["Param: problemSize"] = df["Param: problemSize"].astype("int")
    return df

# Add number of unique constructors to problems [J: Doesn't seem to work]
# cons_regex_pat = r"\bconstructors\b\W*(?:\s*(\w*)\s*:.*\s*)*\W*\brules\b"

cons_area_regex = r"^\s*constructors\s*$((?:.*\n)*)^\s*rules\s*$"
cons_area_pat = re.compile(cons_area_regex, re.MULTILINE)

cons_regex = r"\w+\s*:.*\S"
cons_pat = re.compile(cons_regex)

def find_constructors(p):
    cons_areas = cons_area_pat.findall(p)
    cons_groups = list(map(cons_pat.findall, cons_areas))
    conses = set().union(*cons_groups)
    return conses

def count_constructors_in_file(program_name):
    p_program = os.path.join("..", "src", "main", "resources", "stratego2", program_name + ".str2")
    try:
        with open(p_program, 'r') as f_program:
            contents = f_program.read()
            conses = find_constructors(contents)
            return len(conses)
    except:
        return np.nan


## Plot-specific settings
configs = [
    {
        "problems": {"Benchexpr", "Benchsym", "Benchtree", "Bubblesort", "Factorial", "Fibonacci", "Hanoi", "Mergesort", "Quicksort", "Sieve"},
        "stages": {"run"},
        "settings": {"yscale": "log"}
    },
    {
        # Apply to all problems
        "stages": {"compileStratego", "compileJava", "Java space", "Class space"},
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

def read(f: str, **kwargs):
    if f.endswith(".csv"):
        return pd.read_csv(f, na_filter=False, **kwargs)
    elif f.endswith(".json"):
        return read_json(f, **kwargs)

def load_data(files):
    df_raw = pd.concat(map(read, files), ignore_index=True).rename(
        columns={
            "Param: optimisationLevel": "Pattern Match Compilation",
            "Param: problemSize": "Problem Size",
            "Param: sharedConstructors": "Shared Constructors",
            "Param: switchImplementation":
            "Codegen Implementation"
        }
    )

    df_raw["Pattern Match Compilation"] = np.vectorize(optlevel_to_label)(df_raw["Pattern Match Compilation"])
    df_raw["Pattern Match Compilation"] = df_raw["Pattern Match Compilation"].astype("string")
    df = df_raw[df_raw.Benchmark.str.endswith("jfr") == False].copy()
    df["Stage"] = df["Benchmark"].str.rpartition('.')[2]
    df["Problem"] = df["Benchmark"].str.split('.').map(lambda l: l[-2])
    df["Number of constructors"] = (df["Problem"].str.lower() + df["Problem Size"].astype("str").mask(df["Problem Size"]==-1, "")).map(count_constructors_in_file)
    return df

def allstages_plot(df_scaledproblem):
    return configure_grid(sns.relplot(
        data=df_scaledproblem,
        x="Problem Size",
        y="Score",
        row="Problem",
        row_order=np.sort(np.unique(df_scaledproblem["Problem"].values)),
        col="Stage",
        col_order=[s for s in ["compileStratego", "Java space", "compileJava", "Class space", "run"] if s in df_scaledproblem["Stage"].values],
        style="Codegen Implementation",
        hue="Pattern Match Compilation",
        hue_order=map(optlevel_to_label, ["2", "3", "4"]),
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
    cols = np.sort(np.unique(df_scaledproblem["Problem"].values))
    opt_levels = list(map(optlevel_to_label, ["3", "4"]))

    g = configure_grid(sns.relplot(
        data=df_scaledproblem[
            (df_scaledproblem["Problem Size"] != -1)
                & (df_scaledproblem["Stage"] == "run")
                & (df_scaledproblem["Pattern Match Compilation"].isin(opt_levels))],
        x="Problem Size",
        y="Score",
        col="Problem",
        col_order=cols,
        col_wrap=min(3, len(cols)),
    #     style="Codegen Implementation",
        hue="Pattern Match Compilation",
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
    opt_levels=list(map(optlevel_to_label, ["3", "4"]))

    g = configure_grid(sns.relplot(
        data=df_scaledproblem[(df_scaledproblem["Stage"] == "run") & (df_scaledproblem["Pattern Match Compilation"].isin(opt_levels))],
        x="Problem Size",
        y="Score",
        col="Problem",
        col_order=cols,
        col_wrap=min(3, len(cols)),
        style="Pattern Match Compilation",
        hue="Pattern Match Compilation",
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
def til_plot(df):
    cols = ["Factorial", "Bubblesort"]
    opt_levels=list(map(optlevel_to_label, ["3", "4"]))

    df_til_add = df[df["Benchmark"].str.contains("til") & df["Problem"].str.contains("Add") & (df["Pattern Match Compilation"].isin(opt_levels))].copy()
    df_til_add["Problem Size"] = df_til_add["Problem"].str[3:].astype("int")
    df_til_add["Stage"] = df_til_add["Stage"].map(lambda s: "Interpret" if s == "run" else "Optimise" if s == "runTILCompiler" else s )

    g = configure_grid(sns.relplot(
        data=df_til_add,
        x="Problem Size",
        y="Score",
        col="Stage",
        col_wrap=min(3, len(cols)),
        style="Pattern Match Compilation",
        hue="Pattern Match Compilation",
        hue_order=opt_levels,
        kind="line",
        markers=True,
        facet_kws=dict(
            sharex=False, 
            sharey=False,
        ),
    ))

    sns.move_legend(g, "upper left", frameon=True, bbox_to_anchor=(0.1, 0.9))
    # g.set(yscale="log")
    for _, ax in g.axes_dict.items():
        ax.grid(True, 'minor', 'y')
    g.set_titles(None, None, "{col_name}")
    g.set_axis_labels("Number of variables/additions", "Time (s)")
    # TODO Include strj runtime -> with and without fusion
    return g

def unscaled_plot(df):
    opt_levels=list(map(optlevel_to_label, ["3", "4"]))
    df_unscaledproblem = df[(df["Problem Size"] == -1) & (df["Pattern Match Compilation"].isin(opt_levels))]
    data = df_unscaledproblem.loc[
                df_unscaledproblem["Benchmark"].str.contains("stratego")].copy()

    data2 = pd.DataFrame()

    for problem, df_problem in data.groupby("Problem"):
        for stage, df_p_stage in df_problem.groupby("Stage"):
            median = df_p_stage[df_p_stage["Codegen Implementation"] == ""]["Score"].median()
            data2 = pd.concat([data2, df_p_stage.assign(MedianScore=lambda x: median)], ignore_index=True)

    data2["Score"] = data2["Score"] / data2["MedianScore"]

    g = sns.catplot(
        data = data2,
        x = "Problem",
        y = "Score",
        col = "Stage",
        col_order = [s for s in ["compileStratego", "compileJava"] if s in df_unscaledproblem["Stage"].values],
        hue = "Pattern Match Compilation",
        kind = "box",
        sharey=False,
    )

    g.set_ylabels("Factor vs. no PMC - logarithmic")
    for _, ax in g.axes_dict.items():
        locs = ax.get_xticks()
        labels = ax.get_xticklabels()
        ax.set_xticklabels(labels, rotation=90)
        ax.grid(True, 'minor', 'y')
        ax.set_xticks([s-0.5 for s in locs], minor=True)
        ax.grid(True, 'minor', 'x', alpha=0.5)
        ax.set_yscale('log')
        ax.yaxis.set_major_formatter(ScalarFormatter(1))
        ax.yaxis.set_minor_formatter(ScalarFormatter(1))
    sns.move_legend(g, "upper left", frameon=True, bbox_to_anchor=(0.47, 0.8))
    g.set_titles(None, None, "{col_name}")

    return g

def main(output_dir, files):
    sns.set_theme(font='Linux Biolinum O', font_scale=1.2)
    plt.rc('pdf',fonttype = 42)
    
    df = load_data(files)

    df_scaledproblem = df[
        df["Benchmark"].str.contains("stratego")
            & (df["Problem Size"] != -1)
            & (df["Problem"] != "BenchNullary")
    ]
    
    file_basenames = list(map(lambda x: os.path.basename(x).replace(" ", "_"), files))
    
    allstages_plot(df_scaledproblem).savefig(f"{output_dir}/{'_'.join(file_basenames)}-allstages.png")
    runtime_plot(df_scaledproblem).savefig(f"{output_dir}/{'_'.join(file_basenames)}-runtime.png")
    fact_bub_plot(df_scaledproblem).savefig(f"{output_dir}/{'_'.join(file_basenames)}-fact&bub-run.pdf")
    til_plot(df).savefig(f"{output_dir}/{'_'.join(file_basenames)}-til.pdf")
    unscaled_plot(df).savefig(f"{output_dir}/{'_'.join(file_basenames)}-unscaled.pdf")

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2:])
