/*
 * Copyright (c) 2021 Sebastian Erives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.github.serivesmejia.eocvsim.pipeline.compiler

import com.github.serivesmejia.eocvsim.util.SysUtil
import java.io.File
import javax.tools.ToolProvider

class PipelineCompiler(private val inputPath: File, private val mode: PipelineCompileMode) {

    private val compiler = ToolProvider.getSystemJavaCompiler()

    fun compile(outputJar: File): PipelineCompileResult {
        val files = when(mode) {
            PipelineCompileMode.SINGLE_FILE -> listOf(inputPath)
            PipelineCompileMode.SINGLE_FOLDER -> SysUtil.filesIn(inputPath, ".jar")
            PipelineCompileMode.CURRENT_AND_INNER_FOLDERS -> SysUtil.filesUnder(inputPath, ".jar")
        }

        return PipelineCompileResult(false, "")
    }

}

data class PipelineCompileResult(val succeed: Boolean, val message: String)

enum class PipelineCompileMode {
    SINGLE_FILE,
    SINGLE_FOLDER,
    CURRENT_AND_INNER_FOLDERS
}