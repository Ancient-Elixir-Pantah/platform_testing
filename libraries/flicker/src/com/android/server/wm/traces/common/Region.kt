/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.server.wm.traces.common

class Region(val rects: Array<Rect>) : Rect(
    rects.map { it.left }.min() ?: 0,
    rects.map { it.top }.min() ?: 0,
    rects.map { it.right }.max() ?: 0,
    rects.map { it.bottom }.max() ?: 0
) {
    constructor(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) : this(Rect(left, top, right, bottom))

    constructor(rect: Rect): this(arrayOf(rect))

    constructor() : this(Rect.EMPTY)

    override fun toString(): String {
        return rects.joinToString(", ")
    }

    companion object {
        val EMPTY = Region()
    }
}