package com.liu.aopdemo


import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project


//gradle插件需要实现Plugin接口，void apply() 会在apply该插件时(apply plugin 'xxx') 调用
class PluginDemo implements Plugin<Project> {
    String TAG = "PluginDemo"

    @Override
    void apply(Project project) {
        println 'start plugin'
        def hasApp = project.plugins.withType(AppPlugin)
    }
}


