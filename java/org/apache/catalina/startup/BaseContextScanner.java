/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */


package org.apache.catalina.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.catalina.Context;
import org.apache.catalina.ContextScanner;
import org.apache.catalina.Globals;
import org.apache.catalina.JarRepository;
import org.apache.tomcat.WarComponents.JarServletContainerInitializerService;

public abstract class BaseContextScanner
    implements ContextScanner {

    
    /**
     * Names of JARs that are known not to contain any descriptors or annotations.
     */
    protected static HashSet<String> skipJars;

    /**
     * Initializes the set of JARs that are known not to contain any descriptors.
     */
    static {
        skipJars = new HashSet<String>();
        // Bootstrap JARs
        skipJars.add("bootstrap.jar");
        skipJars.add("commons-daemon.jar");
        skipJars.add("tomcat-juli.jar");
        // Main JARs
        skipJars.add("annotations-api.jar");
        skipJars.add("catalina.jar");
        skipJars.add("catalina-ant.jar");
        skipJars.add("el-api.jar");
        skipJars.add("jasper.jar");
        skipJars.add("jasper-el.jar");
        skipJars.add("jasper-jdt.jar");
        skipJars.add("jsp-api.jar");
        skipJars.add("servlet-api.jar");
        skipJars.add("tomcat-coyote.jar");
        skipJars.add("tomcat-dbcp.jar");
        // i18n JARs
        skipJars.add("tomcat-i18n-en.jar");
        skipJars.add("tomcat-i18n-es.jar");
        skipJars.add("tomcat-i18n-fr.jar");
        skipJars.add("tomcat-i18n-ja.jar");
        // Misc JARs not included with Tomcat
        skipJars.add("ant.jar");
        skipJars.add("commons-dbcp.jar");
        skipJars.add("commons-beanutils.jar");
        skipJars.add("commons-fileupload-1.0.jar");
        skipJars.add("commons-pool.jar");
        skipJars.add("commons-digester.jar");
        skipJars.add("commons-logging.jar");
        skipJars.add("commons-collections.jar");
        skipJars.add("jmx.jar");
        skipJars.add("jmx-tools.jar");
        skipJars.add("xercesImpl.jar");
        skipJars.add("xmlParserAPIs.jar");
        skipJars.add("xml-apis.jar");
        // JARs from J2SE runtime
        skipJars.add("sunjce_provider.jar");
        skipJars.add("ldapsec.jar");
        skipJars.add("localedata.jar");
        skipJars.add("dnsns.jar");
        skipJars.add("tools.jar");
        skipJars.add("sunpkcs11.jar");
    }


    protected ArrayList<Class<?>> annotatedClasses = new ArrayList<Class<?>>();
    protected ArrayList<String> overlays = new ArrayList<String>();
    protected ArrayList<String> webFragments = new ArrayList<String>();
    protected Map<String, Set<String>> TLDs = new HashMap<String, Set<String>>();
    protected Map<String, JarServletContainerInitializerService> jarServletContainerInitializerService = 
        new HashMap<String, JarServletContainerInitializerService>();

    /**
     * Used to speed up scanning for the services interest classes.
     */
    protected Class<?>[] handlesTypesArray = null;
    protected Map<Class<?>, JarServletContainerInitializerService> handlesTypes = 
        new HashMap<Class<?>, JarServletContainerInitializerService>();


    public Iterator<Class<?>> getAnnotatedClasses() {
        return annotatedClasses.iterator();
    }


    public Iterator<String> getOverlays() {
        return overlays.iterator();
    }


    public Iterator<String> getWebFragments() {
        return webFragments.iterator();
    }


    public Map<String, Set<String>> getTLDs() {
        return TLDs;
    }
    
    
    public Map<String, JarServletContainerInitializerService> getJarServletContainerInitializerServices() {
        return jarServletContainerInitializerService;
    }
    
    
    /**
     * Scan the given context's default locations for annotations.
     * 
     * @param context
     */
    public void scan(Context context) {
        JarRepository jarRepository = context.getJarRepository();
        if (jarRepository != null) {
            JarFile[] jars = jarRepository.findJars();
            for (int i = 0; i < jars.length; i++) {
                scanJar(context, jars[i]);
            }
            File[] explodedJars = jarRepository.findExplodedJars();
            for (int i = 0; i < explodedJars.length; i++) {
                scanClasses(context, explodedJars[i], "");
            }
        }
        // Do the same for the context parent
        jarRepository = context.getParent().getJarRepository();
        if (jarRepository != null) {
            JarFile[] jars = jarRepository.findJars();
            for (int i = 0; i < jars.length; i++) {
                scanJar(context, jars[i]);
            }
            File[] explodedJars = jarRepository.findExplodedJars();
            for (int i = 0; i < explodedJars.length; i++) {
                scanClasses(context, explodedJars[i], "");
            }
        }
        /*
        ClassLoader loader = context.getLoader().getClassLoader().getParent();
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                for (int i=0; i<urls.length; i++) {
                    // Expect file URLs, these are %xx encoded or not depending on
                    // the class loader
                    // This is definitely not as clean as using JAR URLs either
                    // over file or the custom jndi handler, but a lot less
                    // buggy overall
                    File file = null;
                    try {
                        file = new File(urls[i].toURI());
                    } catch (Exception e) {
                        // Ignore, probably an unencoded char or non file URL,
                        // attempt direct access
                        file = new File(urls[i].getFile());
                    }
                    try {
                        file = file.getCanonicalFile();
                    } catch (IOException e) {
                        // Ignore
                    }
                    if (!file.exists()) {
                        continue;
                    }
                    String path = file.getAbsolutePath();
                    if (!path.endsWith(".jar")) {
                        continue;
                    }
                    // Scan all JARs from WEB-INF/lib, plus any shared JARs
                    // that are not known not to contain any TLDs
                    if (skipJars == null
                        || !skipJars.contains(file.getName())) {
                        try {
                            scanJar(context, new JarFile(path));
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            }
            loader = loader.getParent();
        }
        */

        HashSet<String> warTLDs = new HashSet<String>();

        // Find any TLD file in /WEB-INF
        DirContext resources = context.getResources();
        if (resources != null) {
            tldScanResourcePathsWebInf(resources, "/WEB-INF", warTLDs);
        }
        TLDs.put("", warTLDs);

        /*
        DirContext resources = context.getResources();
        DirContext webInfClasses = null;
        DirContext webInfLib = null;

        try {
            webInfClasses = (DirContext) resources.lookup("/WEB-INF/classes");
        } catch (Exception e) {
            // Ignore, /WEB-INF/classes not found, or not a folder
        }
        if (webInfClasses != null) {
            scanClasses(context, webInfClasses, "");
        }
        
        try {
            webInfLib = (DirContext) resources.lookup("/WEB-INF/lib");
        } catch (Exception e) {
            // Ignore, /WEB-INF/classes not found, or not a folder
        }
        if (webInfLib != null) {
            scanJars(context, webInfLib);
        }*/
        
    }
    
    
    /**
     * Scans the web application's subdirectory identified by rootPath,
     * along with its subdirectories, for TLDs.
     *
     * Initially, rootPath equals /WEB-INF. The /WEB-INF/classes and
     * /WEB-INF/lib subdirectories are excluded from the search, as per the
     * JSP 2.0 spec.
     *
     * @param resources The web application's resources
     * @param rootPath The path whose subdirectories are to be searched for
     * TLDs
     * @param tldPaths The set of TLD resource paths to add to
     */
    protected void tldScanResourcePathsWebInf(DirContext resources,
                                            String rootPath,
                                            HashSet<String> tldPaths) {
        try {
            NamingEnumeration<NameClassPair> items = resources.list(rootPath);
            while (items.hasMoreElements()) {
                NameClassPair item = items.nextElement();
                String resourcePath = rootPath + "/" + item.getName();
                if (!resourcePath.endsWith(".tld")
                        && (resourcePath.startsWith("/WEB-INF/classes")
                            || resourcePath.startsWith("/WEB-INF/lib"))) {
                    continue;
                }
                if (resourcePath.endsWith(".tld")) {
                    tldPaths.add(resourcePath);
                } else {
                    tldScanResourcePathsWebInf(resources, resourcePath,
                                               tldPaths);
                }
            }
        } catch (NamingException e) {
            ; // Silent catch: it's valid that no /WEB-INF directory exists
        }
    }


    /**
     * Scan folder containing class files.
     */
    public void scanClasses(Context context, File folder, String path) {
        String[] files = folder.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(folder, files[i]);
            if (file.isDirectory()) {
                scanClasses(context, file, path + "/" + files[i]);
            } else if (files[i].endsWith(".class")) {
                String className = getClassName(path + "/" + files[i]);
                Class<?> annotated = scanClass(context, className, file, null);
                if (annotated != null) {
                    annotatedClasses.add(annotated);
                }
            }
        }
    }
    
    
    /**
     * Scan folder containing class files.
     */
    /*
    public static void scanClasses(Context context, DirContext folder, String path) {
        try {
            NamingEnumeration<Binding> enumeration = folder.listBindings(path);
            while (enumeration.hasMore()) {
                Binding binding = enumeration.next();
                Object object = binding.getObject();
                
                if (object instanceof Resource) {
                    // This is a class, so we should load it
                    String className = getClassName(path + "/" + binding.getName());
                    scanClass(context, className, (Resource) object, null, null);
                } else if (object instanceof DirContext) {
                    scanClasses(context, folder, path + "/" + binding.getName());
                }
                
            }            
        } catch (NamingException e) {
            // Ignore for now
            e.printStackTrace();
        }
    }*/
    
    
    /**
     * Scan folder containing JAR files.
     */
    //public void scanJars(Context context, DirContext folder) {
        /*if (context.getLoader().findLoaderRepositories() != null) {
            String[] repositories = context.getLoader().findLoaderRepositories();
            for (int i = 0; i < repositories.length; i++) {
                if (repositories[i].endsWith(".jar")) {
                    try {
                        scanJar(context, new JarFile(repositories[i]));
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }*/
        /*else {
            try {
                NamingEnumeration<Binding> enumeration = folder.listBindings("");
                while (enumeration.hasMore()) {
                    Binding binding = enumeration.next();
                    Object object = binding.getObject();

                    if (object instanceof Resource && binding.getName().endsWith(".jar")) {
                        // This is normally a JAR, put it in the work folder
                        File destDir = null;
                        File workDir =
                            (File) context.getServletContext().getAttribute(Globals.WORK_DIR_ATTR);
                        destDir = new File(workDir, "WEB-INF/lib");
                        destDir.mkdirs();
                        File destFile = new File(destDir, binding.getName());
                        
                        scanJar(context, (Resource) object);
                    }

                }            
            } catch (NamingException e) {
                // Ignore for now
                e.printStackTrace();
            }
        }*/
    //}
    
    
    /**
     * Scan all class files in the given JAR.
     */
    public void scanJar(Context context, JarFile file) {
        if (file.getEntry(Globals.WEB_FRAGMENT_PATH) != null) {
            webFragments.add(file.getName());
        }
        if (file.getEntry(Globals.OVERLAY_PATH) != null) {
            overlays.add(file.getName());
        }
        if (file.getEntry(Globals.SERVLET_CONTAINER_INITIALIZER_SERVICE_PATH) != null) {
            // FIXME: Read Servlet container initializer service file
            
            // FIXME: Load Servlet container initializer class and read HandlesTypes annotation
            
            // FIXME: Add in jarService map, and add in the local map used to speed up lookups 
            
        }
        HashSet<String> jarTLDs = new HashSet<String>();
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!context.getIgnoreAnnotations() && name.endsWith(".class")) {
                Class<?> annotated = scanClass(context, getClassName(entry.getName()), null, entry);
                if (annotated != null) {
                    annotatedClasses.add(annotated);
                }
            } else if (name.startsWith("META-INF/") && name.endsWith(".tld")) {
                jarTLDs.add(name);
            }
        }
        if (jarTLDs.size() > 0) {
            TLDs.put(file.getName(), jarTLDs);
        }
    }
    
    
    /**
     * Get class name given a path to a classfile.
     * /my/class/MyClass.class -> my.class.MyClass
     */
    public String getClassName(String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        if (filePath.endsWith(".class")) {
            filePath = filePath.substring(0, filePath.length() - ".class".length());
        }
        return filePath.replace('/', '.');
    }
    
    
    /**
     * Scan class for interesting annotations.
     */
    public abstract Class<?> scanClass(Context context, String className, File file, JarEntry entry);
    
    
}