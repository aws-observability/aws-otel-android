/*
 * Copyright Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.opentelemetry.android.uiload.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

/**
 * Depth-first, iterative approach to recursively count all child views under the current View, and
 * compute the maximum depth.
 *
 * Returns a pair of [count, maxDepth]
 */
fun View.getComplexity(): Pair<Int, Int> {
    val stack = ArrayDeque<Pair<View, Int>>()
    var maxDepth = 0
    var count = 0

    stack.add(this to 0)

    while (stack.isNotEmpty()) {
        val (view, depth) = stack.removeFirst()
        maxDepth = maxOf(depth, maxDepth)
        count++

        if (view is ViewGroup) {
            stack.addAll(view.children.map { it to (1 + depth) })
        }
    }

    return Pair(count, maxDepth)
}
