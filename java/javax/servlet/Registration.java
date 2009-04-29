/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.servlet;

import java.util.Map;
import java.util.Set;

/**
 * Interface through which a {@link Servlet} or {@link Filter} may be
 * further configured.
 *
 * @since Servlet 3.0
 */
public interface Registration {

    /**
     * Sets the initialization parameter with the given name and value
     * on the Servlet or Filter that is represented by this Registration.
     *
     * @param name the initialization parameter name
     * @param value the initialization parameter value
     *
     * @return true if the update was successful, i.e., an initialization
     * parameter with the given name did not already exist for the Servlet
     * or Filter represented by this Registration, and false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * Registration was obtained has already been initialized
     * @throws IllegalArgumentException if the given name or value is
     * <tt>null</tt>
     */ 
    public boolean setInitParameter(String name, String value);

    /**
     * Sets the given initialization parameters on the Servlet or Filter
     * that is represented by this Registration.
     *
     * <p>The given map of initialization parameters is processed
     * <i>by-value</i>, i.e., for each initialization parameter contained
     * in the map, this method calls {@link #setInitParameter(String,String)}.
     * If that method would return false for any of the
     * initialization parameters in the given map, no updates will be
     * performed, and false will be returned. Likewise, if the map contains
     * an initialization parameter with a <tt>null</tt> name or value, no
     * updates will be performed, and an IllegalArgumentException will be
     * thrown.
     *
     * @param initParameters the initialization parameters
     *
     * @return the (possibly empty) Set of initialization parameter names
     * that are in conflict
     *
     * @throws IllegalStateException if the ServletContext from which this
     * Registration was obtained has already been initialized
     * @throws IllegalArgumentException if the given map contains an
     * initialization parameter with a <tt>null</tt> name or value
     */ 
    public Set<String> setInitParameters(Map<String, String> initParameters);

    /**
     * Interface through which a {@link Servlet} or {@link Filter} registered
     * via one of the <tt>addServlet</tt> or <tt>addFilter</tt> methods,
     * respectively, on {@link ServletContext} may be further configured.
     */
    interface Dynamic extends Registration {

        /**
         * Sets the description on the Servlet or Filter represented by
         * this dynamic Registration.
         *
         * <p>A call to this method overrides any previous setting.
         *
         * @param description the description of the servlet
         *
         * @throws IllegalStateException if the ServletContext from which
         * this dynamic Registration was obtained has already been initialized
         */
        public void setDescription(String description);

        /**
         * Configures the Servlet or Filter represented by this dynamic
         * Registration as supporting asynchronous operations or not.
         *
         * <p>By default, servlet and filters do not support asynchronous
         * operations.
         *
         * <p>A call to this method overrides any previous setting.
         *
         * @param isAsyncSupported true if the Servlet or Filter represented
         * by this dynamic Registration supports asynchronous operations,
         * false otherwise
         *
         * @throws IllegalStateException if the ServletContext from which
         * this dynamic Registration was obtained has already been
         * initialized
         */
        public void setAsyncSupported(boolean isAsyncSupported);
    }
}
