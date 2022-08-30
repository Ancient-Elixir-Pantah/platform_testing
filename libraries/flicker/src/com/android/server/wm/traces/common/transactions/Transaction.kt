/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wm.traces.common.transactions

import kotlin.js.JsName

data class Transaction(
    @JsName("pid")
    val pid: Int,
    @JsName("uid")
    val uid: Int,
    @JsName("requestedVSyncId")
    val requestedVSyncId: Long,
    @JsName("postTime")
    val postTime: Long,
    @JsName("id")
    val id: Long,
) {
    lateinit var appliedInEntry: TransactionsTraceEntry
        internal set

    val appliedVSyncId: Long get() = appliedInEntry.vSyncId

    override fun toString(): String {
        return "Transaction#${hashCode().toString(16)}" +
                "(pid=$pid, uid=$uid, requestedVSyncId=$requestedVSyncId, postTime=$postTime, " +
                "id=$id)"
    }
}
