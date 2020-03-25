package com.jadyn.ai

import com.android.build.gradle.AppExtension
import com.jadyn.ai.extension.TraceDExtension
import com.jadyn.ai.transform.TraceDTransform
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class TraceDPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("systrace", TraceDExtension)
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException('application plugin required')
        }
        // project配置成功均会调用的函数
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new TraceDTransform(project))
    }
}