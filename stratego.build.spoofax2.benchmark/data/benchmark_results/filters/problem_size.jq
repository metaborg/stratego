# this filter takes the benchmark name and reduces it to just the function name (instead of the entire package)
(.[].problem |= split("_"; null)) |
(.[] |= (.size = .problem.[1])) |
(.[].problem |= .[0]) |
(.[].size |= (. // "NaN" | tonumber))