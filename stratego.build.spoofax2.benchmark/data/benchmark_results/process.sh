#!/usr/bin/env bash

# This script processes the generated .json data using the jq command and various filters
# It writes the processed .json as an .csv in the data/processed directory

cd "$(dirname "${BASH_SOURCE[0]}")" || exit # ensure we are in the right directory

jq -f 'filters/simplify.jq' "$1" | # removes unnecessary fields and flattens object
jq -f 'filters/benchmark_name.jq' | # renames benchmark names to something more palatable
jq -f 'filters/problem_size.jq' | # splits the problem param into problem and size params
jq -f 'filters/optimisation_level.jq' | # converts optimisation level param to a number
jq -f 'filters/csv.jq' -r > "processed/$1.csv" # converts to csv and writes to file