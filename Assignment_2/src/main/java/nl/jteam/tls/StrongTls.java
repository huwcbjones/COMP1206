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

/**
 * Modified by Huw Jones to conform with "more modern" SSL/TLS security practices.
 * Main source for best practice advice: https://www.ssllabs.com/downloads/SSL_TLS_Deployment_Best_Practices.pdf
 */
package nl.jteam.tls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants to use strong protocols and cipher suites with the TLS transport layer.
 * <p>
 * See http://java.sun.com/javase/6/docs/technotes/guides/security/StandardNames.html
 * for the standard names.
 *
 * Updated using http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#ciphersuites
 *
 * @author Erik van Oosten
 */
public class StrongTls {

    /**
     * The protocols that are enabled.
     */
    public static final String[] ENABLED_PROTOCOLS = new String[] {

            // Strong protocols
            "TLSv1",
            "TLSv1.1",
            "TLSv1.2"

            // Weak protocols
//            "SSLv3"
//            "SSLv2"
//            "SSLv2Hello"

    };

    /**
     * The SSL cipher suites that are enabled.
     */
    public static final String[] ENABLED_CIPHER_SUITES = new String[] {

            // Strong cipher suites that are listed at
            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#ciphersuites

            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_256_CBC_SHA",
            "TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA",
            "TLS_RSA_WITH_CAMELLIA_256_CBC_SHA",
            "TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA",
            "TLS_RSA_WITH_CAMELLIA_128_CBC_SHA",
            "TLS_RSA_WITH_3DES_EDE_CBC_SHA"


    };

    /**
     * Gives the intersection of 2 string arrays.
     *
     * @param stringSetA a set of strings (not null)
     * @param stringSetB another set of strings (not null)
     * @return the intersection of strings in stringSetA and stringSetB
     */
    public static String[] intersection(String[] stringSetA, String[] stringSetB) {
        Set<String> intersection = new HashSet<>(Arrays.asList(stringSetA));
        intersection.retainAll(Arrays.asList(stringSetB));
        return intersection.toArray(new String[intersection.size()]);
    }

}
