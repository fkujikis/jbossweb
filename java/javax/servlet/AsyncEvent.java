/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.servlet;

/**
 * @since 3.0
 * $Id: AsyncEvent.java 731967 2009-01-06 15:15:32Z markt $
 * TODO SERVLET3
 */
public class AsyncEvent {
    private ServletRequest request;
    private ServletResponse response;
    
    AsyncEvent(ServletRequest request, ServletResponse response) {
        this.request = request;
        this.response = response;
    }
    
    public ServletRequest getRequest() {
        return request;
    }
    
    public ServletResponse getResponse() {
        return response;
    }
}