package org.smart4j.framework.util;

import org.apache.commons.collections4.EnumerationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public final class ClassUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

    public static ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }

    public static Class<?> loadClass(String className, boolean isInitialized){
        Class<?> cls;
        try {
            cls = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {
            LOGGER.error("load class failure", e);
            throw new RuntimeException(e);
        }

        return cls;
    }

    public static Set<Class<?>> getClassSet(String packageName){
        Set<Class<?>> classSet = new HashSet<>();
        try{
            List<URL> urls = EnumerationUtils.toList(getClassLoader().getResources(packageName.replace(".", "/")));
            urls.stream()
                    .filter(url->url!=null && url.getProtocol().equals("file"))
                    .forEach(url->{
                        String packagePath = url.getPath().replaceAll("%20", " ");
                        addClass(classSet, packagePath, packageName);
                    });

            urls.stream()
                    .filter(url->url!=null && url.getProtocol().equals("jar"))
                    .map(url -> {
                        try {
                            return (JarURLConnection) url.openConnection();
                        } catch (IOException e) {
                            LOGGER.error("get class set failure", e);
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(jarURLConnection->jarURLConnection!=null)
                    .map(jarURLConnection -> {
                        try {
                            return jarURLConnection.getJarFile();
                        } catch (IOException e) {
                            LOGGER.error("get class set failure", e);
                            throw new RuntimeException(e);
                        }
                    })
                    .flatMap(jarFile-> EnumerationUtils.toList(jarFile.entries()).stream())
                    .map(jarEntry->jarEntry.getName())
                    .filter(jarEntryName->jarEntryName.endsWith(".class"))
                    .forEach(jarEntryName->{
                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                        doAddClass(classSet, className);
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return classSet;
    }

    private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {
        File parentDirectory = new File(packagePath);

        Stream.of(parentDirectory.listFiles(file->file.isFile() && file.getName().endsWith(".class")))
                .map(file-> {
                    String fileName = file.getName();
                    String className = fileName.substring(0, fileName.lastIndexOf("."));
                    return StringUtil.isNotEmpty(packageName)?packageName+"."+className:className;
                })
                .forEach(className->doAddClass(classSet, className));

        Stream.of(parentDirectory.listFiles(file->file.isDirectory()))
                .forEach(directory->{
                    String directoryName = directory.getName();

                    addClass(classSet,
                            StringUtil.isNotEmpty(packagePath)?packagePath+"/"+directoryName:directoryName,
                            StringUtil.isNotEmpty(packageName)?packageName+"."+directoryName:directoryName);
                });
    }

    private static void doAddClass(Set<Class<?>> classSet, String className) {
        Class<?> cls = loadClass(className, false);
        classSet.add(cls);
    }
}
