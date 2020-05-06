package com.jadyn.ai;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.google.common.collect.ImmutableSet;
import com.jadyn.ai.trace_plugin.TraceClassVisitor;
import com.jadyn.ai.trace_plugin.TraceConfig;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * JadynAi since 2020/4/23
 */
public class TraceDTransform extends Transform {

    private Project project;
    private boolean isForApplication;
    private TraceDExtension sysTrace;

    public TraceDTransform(Project project, boolean isForApplication
            , TraceDExtension sysTrace) {
        this.project = project;
        this.isForApplication = isForApplication;
        this.sysTrace = sysTrace;
    }

    @Override
    public String getName() {
        return "traceMethodTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        if (isForApplication) {
            return TransformManager.SCOPE_FULL_PROJECT;
        }
        return ImmutableSet.of(QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
//        super.transform(transformInvocation);
        println("start transform 111");
        boolean isIncremental = transformInvocation.isIncremental() && isIncremental();
        TraceDExtension sysTraceConfig = sysTrace;
        TraceConfig traceConfig = new TraceConfig();
        traceConfig.pacFilePath = sysTraceConfig.pacListFile;
        traceConfig.parseConfig();

        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        if (outputProvider != null) {
            outputProvider.deleteAll();
        }

        inputs.forEach(transformInput -> {
            transformInput.getDirectoryInputs().forEach(directoryInput -> {
                println("transform src: " + directoryInput.getFile().getAbsolutePath());
//                directoryInput.getChangedFiles().forEach((file, status) -> println("key: " + file.getAbsolutePath() + " status: " + status));
                try {
                    traceSrcFiles(directoryInput, outputProvider, traceConfig, isIncremental);
                } catch (Exception e) {
                    println("trace src file exception " + e);
                }
            });
            transformInput.getJarInputs().forEach(jarInput -> {
                try {
                    traceJarFiles(jarInput, outputProvider, traceConfig);
                } catch (Exception e) {
                    println("trace jar file exception " + e);
                }
//                if (jarInput.getStatus() != Status.REMOVED) {
//                    println("transform jar: " + jarInput.getFile().getName());
//                }
            });
        });
    }

    private static void traceSrcFiles(DirectoryInput directoryInput, TransformOutputProvider outputProvider,
                               TraceConfig traceConfig, boolean isIncremental) throws Exception {
        if (directoryInput.getFile().isDirectory()) {
            for (File file : FileUtils.getAllFiles(directoryInput.getFile())) {
                String name = file.getName();
                println("file name : " + name);
                if (traceConfig.isNeedTraceClass(name)) {
                    ClassReader classReader = new ClassReader(getBytes(new FileInputStream(file)));
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                    ClassVisitor cv = new TraceClassVisitor(Opcodes.ASM5, classWriter, traceConfig);
                    classReader.accept(cv, EXPAND_FRAMES);
                    byte[] code = classWriter.toByteArray();
                    FileOutputStream fos = new FileOutputStream(file.getParentFile().getAbsolutePath() + File.separator + name);
                    fos.write(code);
                    fos.close();
                }
            }
        }
        File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY);
        org.apache.commons.io.FileUtils.copyDirectory(directoryInput.getFile(), dest);
    }

    private static void traceJarFiles(JarInput jarInput, TransformOutputProvider outputProvider, TraceConfig traceConfig) throws Exception {
        if (jarInput.getFile().getAbsolutePath().endsWith(".jar")) {
            String jarName = jarInput.getName();
            String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4);
            }
            JarFile jarFile = new JarFile(jarInput.getFile());
            Enumeration<JarEntry> enumeration = jarFile.entries();
            File tmpFile = new File(jarInput.getFile().getParent() + File.separator + "classes_temp.jar");
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));

            //循环jar包里的文件
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                if (false && traceConfig.isNeedTraceClass(entryName)) {
                    jarOutputStream.putNextEntry(zipEntry);
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream));
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                    ClassVisitor cv = new TraceClassVisitor(Opcodes.ASM5, classWriter, traceConfig);
                    classReader.accept(cv, EXPAND_FRAMES);
                    byte[] code = classWriter.toByteArray();
                    jarOutputStream.write(code);
                } else {
                    jarOutputStream.putNextEntry(zipEntry);
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                jarOutputStream.closeEntry();
            }

            jarOutputStream.close();
            jarFile.close();

            //处理完输出给下一任务作为输入
            File dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
            org.apache.commons.io.FileUtils.copyFile(tmpFile, dest);

            tmpFile.delete();
        }
    }


    private static void println(String s) {
        String s1 = "TraceD :" + s;
        System.out.println(s1);
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream answer = new ByteArrayOutputStream();
        byte[] byteBuffer = new byte[8192];

        int nbByteRead;
        try {
            while ((nbByteRead = is.read(byteBuffer)) != -1) {
                answer.write(byteBuffer, 0, nbByteRead);
            }
        } finally {
            is.close();
        }
        return answer.toByteArray();
    }
}
