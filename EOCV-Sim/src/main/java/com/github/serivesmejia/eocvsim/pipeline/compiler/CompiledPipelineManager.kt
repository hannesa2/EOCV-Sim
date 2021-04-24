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

import com.github.serivesmejia.eocvsim.pipeline.PipelineManager
import com.github.serivesmejia.eocvsim.pipeline.PipelineSource
import com.github.serivesmejia.eocvsim.util.Log
import com.github.serivesmejia.eocvsim.util.SysUtil
import com.qualcomm.robotcore.util.ElapsedTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class CompiledPipelineManager(private val pipelineManager: PipelineManager) {

    companion object {
        val DEF_WORKING_DIR_FOLDER = File(SysUtil.getEOCVSimFolder(), File.separator + "default_workingdir").mkdirLazy()

        val COMPILER_FOLDER       = File(SysUtil.getEOCVSimFolder(), File.separator + "compiler").mkdirLazy()

        val SOURCES_OUTPUT_FOLDER = File(COMPILER_FOLDER, File.separator + "gen_src").mkdirLazy()
        val CLASSES_OUTPUT_FOLDER = File(COMPILER_FOLDER, File.separator + "out_classes").mkdirLazy()
        val JARS_OUTPUT_FOLDER    = File(COMPILER_FOLDER, File.separator + "out_jars").mkdirLazy()

        val PIPELINES_OUTPUT_JAR  = File(JARS_OUTPUT_FOLDER, File.separator + "pipelines.jar")

        val IS_USABLE by lazy {
            try {
                // create stub compiler to see if we
                // have the JavacTool (only available on JDKs)
                PipelineCompiler(File(""))
                true // yes! we can compile stuff on runtime!
            } catch(e: ClassNotFoundException) {
                false // uh oh, the class wasn't found anywhere in the current JVM...
            }
        }
    }

    var workingDirectory = DEF_WORKING_DIR_FOLDER

    var currentPipelineClassLoader: PipelineClassLoader? = null
        private set

    val TAG = "CompiledPipelineManager"

    fun init() {
        Log.info(TAG, "Initializing...")
        asyncCompile()
    }

    fun compile(mainThreadCallsWrapBlock: (() -> Unit) -> Unit = { it() } ) {
        if(!IS_USABLE) {
            Log.warn(TAG, "Unable to compile Java source code in this JVM (the class JavacTool wasn't found)")
            Log.warn(TAG, "For the user, this probably means that the sim is running in a JRE which doesn't include the javac compiler executable")
            Log.warn(TAG, "To be able to compile pipelines on runtime, make sure the sim is running on a JDK that includes the javac executable (any JDK probably does)")
            return
        }

        Log.info(TAG, "Compiling java files of working directory at ${workingDirectory.absolutePath}")

        val runtime = ElapsedTime()

        val compiler = PipelineCompiler(workingDirectory)
        val result = compiler.compile(PIPELINES_OUTPUT_JAR)

        val timeElapsed = runtime.seconds()

        mainThreadCallsWrapBlock {
            pipelineManager.removeAllPipelinesFrom(PipelineSource.COMPILED_ON_RUNTIME, false)
        }

        currentPipelineClassLoader = null

        val messageEnd = "(took $timeElapsed seconds)\n${result.message}".trim()

        if(result.status == PipelineCompileStatus.SUCCESS) {
            Log.info(TAG, "Build successful $messageEnd")

            currentPipelineClassLoader = PipelineClassLoader(PIPELINES_OUTPUT_JAR)

            for(pipelineClass in currentPipelineClassLoader!!.pipelineClasses) {
                pipelineManager.addPipelineClass(pipelineClass, PipelineSource.COMPILED_ON_RUNTIME)
                Log.info(TAG, "Added ${pipelineClass.simpleName} from built sources ($PIPELINES_OUTPUT_JAR)")
            }
        } else if(result.status == PipelineCompileStatus.NO_SOURCE) {
                Log.warn(TAG, "Build cancelled, no source files to compile $messageEnd")
        } else {
            Log.warn(TAG, "Build failed $messageEnd")
        }

        pipelineManager.refreshGuiPipelineList()
    }

    fun asyncCompile() = GlobalScope.launch(Dispatchers.IO) {
        compile { pipelineManager.eocvSim.onMainUpdate.doOnce(it) }
    }

}

private fun File.mkdirLazy() = apply { mkdir() }