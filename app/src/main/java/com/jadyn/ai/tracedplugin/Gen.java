package com.jadyn.ai.tracedplugin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @version:
 * @FileDescription:
 * @Author:Jing
 * @Since:2020/3/20
 * @ChangeList:
 */
public class Gen {
    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader(
                "com.jadyn.ai.tracedplugin.MainActivty");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        MyClassVisitor classVisitor = new MyClassVisitor(Opcodes.ASM5, classWriter);
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);
        byte[] data = classWriter.toByteArray();
        File out = new File("MainActivity.class");
        FileOutputStream stream = new FileOutputStream(out);
        stream.write(data);
        stream.close();
        System.out.println("asm success");
    }
}
