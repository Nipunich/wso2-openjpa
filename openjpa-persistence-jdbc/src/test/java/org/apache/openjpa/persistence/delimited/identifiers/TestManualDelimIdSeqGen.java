/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.delimited.identifiers;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.DerbyDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestManualDelimIdSeqGen extends SQLListenerTestCase {
    OpenJPAEntityManager em;
    JDBCConfiguration conf;
    DBDictionary dict;
    boolean supportsNativeSequence = false;
    
    EntityE entityE;
    
    @Override
    public void setUp() throws Exception {
        // NOTE: This test is only configured to run on DB2 and Derby since 
        // those DBs handle non-default schemas without additional authority or 
        // configuration  
        setSupportedDatabases(DB2Dictionary.class, DerbyDictionary.class);
        if (isTestsDisabled())
            return;

        super.setUp(EntityE.class,DROP_TABLES);
        assertNotNull(emf);
        
        conf = (JDBCConfiguration) emf.getConfiguration();
        dict = conf.getDBDictionaryInstance();
        supportsNativeSequence = dict.nextSequenceQuery != null;
        
        if (supportsNativeSequence) {
            em = emf.createEntityManager();
            assertNotNull(em);
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
            em = null;
        }
        dict = null;
        conf = null;
        super.tearDown();
    }

    public void createEntityE() {
        entityE = new EntityE("e name");
    }
    
    public void testSeqGen() {
        if (!supportsNativeSequence) {
            return;
        }
        createEntityE();
        
        em.getTransaction().begin();
        em.persist(entityE);
        em.getTransaction().commit();
                
        int genId = entityE.getId();
        em.clear();
        em.getTransaction().begin();
        EntityE eA = em.find(EntityE.class, genId);
        assertEquals("e name", eA.getName());
        
        em.getTransaction().commit();
        em.close();
    }
}
