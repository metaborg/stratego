# this filter splits the benchmark function name into the stage and language e.g. compile+stratego, execute+TIL
(.[].benchmark |= split("\\."; null).[-1]) |
(.[].benchmark |= capture("(?<stage>[a-z]+)(?<lang>[A-Z][a-z]*)")) |
(.[] |= (.stage = .benchmark.stage)) |
(.[] |= (.language = (.benchmark.lang|ascii_downcase))) |
del(.[].benchmark)
