package com.jadyn.ai;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * JadynAi since 2020/4/23
 */
public class TraceDPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        TraceDExtension sysTrace = project.getExtensions().create("systrace", TraceDExtension.class);
        BaseExtension extension = null;
        try {
            extension = project.getExtensions().getByType(AppExtension.class);
        } catch (Exception e) {
            System.out.println("get AppExtension is Failed " + e);
        }
        boolean isForApplication = true;
        if (extension == null) {
            System.out.println("plugin not run in the application");
            isForApplication = false;
            extension = project.getExtensions().findByType(LibraryExtension.class);
        }
        if (extension != null) {
            extension.registerTransform(new TraceDTransform(project, isForApplication, sysTrace));
        }
    }
}
