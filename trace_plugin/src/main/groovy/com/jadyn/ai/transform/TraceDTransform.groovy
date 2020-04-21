package com.jadyn.ai.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import com.jadyn.ai.trace_plugin.TraceClassVisitor
import com.jadyn.ai.trace_plugin.TraceConfig
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class TraceDTransform extends Transform {
    Project project
    // 插件运行在application的Module还是在library
    boolean isForApplication

    TraceDTransform(Project project, boolean isForApplication) {
        this.project = project
        this.isForApplication = isForApplication
    }

    @Override
    String getName() {
        return "traceMethodTransform"
    }

    // 输入类型，选择class
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 输入范围，选择全部工程
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        if (isForApplication) {
            return TransformManager.SCOPE_FULL_PROJECT
        }
        return ImmutableSet.of(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println 'start transform 12'
        final boolean isIncremental = transformInvocation.isIncremental() && this.isIncremental()

        def sysTraceConfig = project.systrace
        TraceConfig traceConfig = new TraceConfig()
        traceConfig.pacFilePath = sysTraceConfig.pacListFile
        traceConfig.parseConfig()

        TransformOutputProvider outputProvider = transformInvocation.outputProvider

        transformInvocation.inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                println "transform src ${dirInput.file.absolutePath}"
                dirInput.changedFiles.forEach {
                    println "key: ${it.key} ,value: ${it.value}"
                }
                traceSrcFiles(dirInput, outputProvider, traceConfig, isIncremental)
            }
            input.jarInputs.each { JarInput jarInput ->
                if (sysTraceConfig.isTraceJar && jarInput.getStatus() != Status.REMOVED) {
                    println "transform jar ${jarInput.file.name}"
//                    traceJarFiles(jarInput, outputProvider, traceConfig)
                }
            }
        }
    }

    private static void traceSrcFiles(DirectoryInput directoryInput, TransformOutputProvider outputProvider,
                                      TraceConfig traceConfig, boolean isIncremental) {
        if (directoryInput.file.isDirectory()) {
            directoryInput.file.eachFileRecurse { File file ->
                def name = file.name
                if (traceConfig.isNeedTraceClass(name)) {
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new TraceClassVisitor(Opcodes.ASM5, classWriter, traceConfig)
                    classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + name)
                    fos.write(code)
                }
            }
        }
        def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes,
                Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    private static void traceJarFiles(JarInput jarInput, TransformOutputProvider outputProvider, TraceConfig traceConfig) {
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(",jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()
            File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            if (tmpFile.exists()) {
                tmpFile.delete()
            }

            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                if (traceConfig.isNeedTraceClass(entryName)) {
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(inputStream)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor classVisitor = new TraceClassVisitor(Opcodes.ASM5, classWriter, traceConfig)
                    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()

            def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes,
                    Format.JAR)
            FileUtils.copyFile(tmpFile, dest)

            tmpFile.delete()
        }
    }
}