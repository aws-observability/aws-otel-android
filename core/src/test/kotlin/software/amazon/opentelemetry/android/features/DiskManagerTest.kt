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
package software.amazon.opentelemetry.android.features

import android.content.Context
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

@ExtendWith(MockKExtension::class)
class DiskManagerTest {
    @MockK
    lateinit var context: Context

    private val fileName = "testfile"
    private val diskManager: DiskManager = DiskManager()

    @Test
    fun `test getInstance returns the same instance`() {
        val instance1 = DiskManager.getInstance()
        val instance2 = DiskManager.getInstance()
        Assertions.assertSame(instance1, instance2)
    }

    @Test
    fun `test reading from file when file exists`(
        @TempDir tempDir: Path,
    ) {
        val expectedContent = "test response!"
        val newFilePath = tempDir.resolve(fileName)
        newFilePath.writeText(expectedContent)

        every { context.openFileInput(fileName) } returns FileInputStream(newFilePath.toFile())

        Assertions.assertEquals(expectedContent, diskManager.readFromFileIfExists(context, fileName))
    }

    @Test
    fun `test reading from file when file doesn't exist`(
        @TempDir tempDir: Path,
    ) {
        every { context.openFileInput(fileName) } throws FileNotFoundException()

        Assertions.assertNull(diskManager.readFromFileIfExists(context, fileName))
    }

    @Test
    fun `test writing to file`(
        @TempDir tempDir: Path,
    ) {
        val expectedContent = "test content"
        val writeFileName = "writefile"
        val newFilePath = tempDir.resolve(writeFileName)
        every { context.openFileOutput(writeFileName, Context.MODE_PRIVATE) } returns FileOutputStream(newFilePath.toFile())

        Assertions.assertTrue(diskManager.writeToFile(context, writeFileName, expectedContent.toByteArray()))
        Assertions.assertEquals(expectedContent, newFilePath.readText())
    }

    @Test
    fun `test writing to file when there is an error`(
        @TempDir tempDir: Path,
    ) {
        val exception = Exception()
        mockkStatic(Log::class)
        every { Log.w(any(), exception) } returns 1

        every { context.openFileOutput(any(), Context.MODE_PRIVATE) } throws exception
        Assertions.assertFalse(diskManager.writeToFile(context, "test", "stuff".toByteArray()))
        verify { Log.w(any(), exception) }
    }
}
