package com.fpf.blucon.metrics

data class CountMetric<T>(
   val label: String,
   val count: Int,
   val value: T? = null,
   val coverage: Float? = null,
)