package com.fpf.blucon.bluetooth

import java.util.UUID

fun UUID.toBluetoothSigUuid(): Int = this.toString().substring(4, 8).toInt(16)
