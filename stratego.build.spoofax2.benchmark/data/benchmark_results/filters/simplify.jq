# this filter deletes keys that are not important and flattens the results
(.[] |= del(.jmhVersion, .jvm, .jvmArgs, .jdkVersion, .vmName, .vmVersion, .warmupTime, .warmupBatchSize, .measurementTime, .measurementBatchSize, .secondaryMetrics)) |
(.[] |= (.optimisationLevel = .params.optimisationLevel)) |
(.[] |= (.problem = .params.problem)) |
(.[] |= (.score = .primaryMetric.score)) |
(.[] |= (.scoreError = .primaryMetric.scoreError)) |
(.[] |= (.scoreUnit = .primaryMetric.scoreUnit)) |
(.[] |= del(.params, .primaryMetric))