package com.jadyn.ai

import com.android.build.gradle.AppExtension
import com.jadyn.ai.extension.TraceDExtension
import com.jadyn.ai.transform.TraceDTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class TraceDPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("systrace", TraceDExtension)
        // project配置成功均会调用的函数
        def extension = project.extensions.getByType(AppExtension)
        def isForApplication = true
        if (extension == null) {
            println 'plugin not run in the application'
            extension = project.getExtensions().findByType(LibraryExtension.class)
            isForApplication = false
        }
        extension.registerTransform(new TraceDTransform(project))
    }
}