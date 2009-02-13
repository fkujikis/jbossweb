/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package javax.servlet;

import java.util.Map;

/**
 * @version $Rev: 743426 $ $Date: 2009-02-11 18:52:39 +0100 (Wed, 11 Feb 2009) $
 * @since 3.0
 */
public abstract class ServletRegistration {

    protected String description;
    protected boolean isAsyncSupported;
    protected int loadOnStartup;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        isAsyncSupported = asyncSupported;
    }

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public abstract void addMapping(String... urlPatterns);

    public abstract void setInitParameter(String name, String value);

    public abstract void setInitParameters(Map<String, String> initParameters);
    
}
