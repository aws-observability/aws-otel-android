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
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewUtilsTest {
    @Test
    fun `single view returns count 1 and depth 0`() {
        val view = mockk<View>()
        val (count, depth) = view.getComplexity()

        assertEquals(1, count)
        assertEquals(0, depth)
    }

    @Test
    fun `empty viewgroup returns count 1 and depth 0`() {
        val viewGroup = TestViewGroup()
        val (count, depth) = viewGroup.getComplexity()

        assertEquals(1, count)
        assertEquals(0, depth)
    }

    @Test
    fun `viewgroup with children returns correct count and depth`() {
        val parent = TestViewGroup()
        val child1 = mockk<View>()
        val child2 = mockk<View>()

        parent.testAddView(child1)
        parent.testAddView(child2)

        val (count, depth) = parent.getComplexity()

        assertEquals(3, count) // parent + 2 children
        assertEquals(1, depth)
    }

    @Test
    fun `nested viewgroup hierarchy returns correct count and depth`() {
        val root = TestViewGroup()
        val child = TestViewGroup()
        val grandchild = mockk<View>()

        child.testAddView(grandchild)
        root.testAddView(child)

        val (count, depth) = root.getComplexity()

        assertEquals(3, count) // root + child + grandchild
        assertEquals(2, depth)
    }

    // Test ViewGroup that allows us to add children for testing
    private class TestViewGroup : ViewGroup(null) {
        private val childViews = mutableListOf<View>()

        fun testAddView(child: View) {
            childViews.add(child)
        }

        override fun getChildCount(): Int = childViews.size

        override fun getChildAt(index: Int): View = childViews[index]

        override fun onLayout(
            changed: Boolean,
            l: Int,
            t: Int,
            r: Int,
            b: Int,
        ) {}
    }
}
