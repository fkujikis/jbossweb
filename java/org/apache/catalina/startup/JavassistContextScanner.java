/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.startup;

import java.io.File;
import java.util.jar.JarEntry;

import org.apache.catalina.Context;

public class JavassistContextScanner
    extends BaseContextScanner {

    /**
     * Scan class for interesting annotations.
     */
    public Class<?> scanClass(Context context, String className, File file, JarEntry entry) {
        // FIXME: Javassist impl
        return null;
    }
    
    
}