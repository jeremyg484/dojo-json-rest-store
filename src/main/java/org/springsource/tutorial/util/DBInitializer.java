/*
 *  Copyright (C) 2008-2011 VMWare, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springsource.tutorial.util;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springsource.tutorial.domain.USState;

@Component
public class DBInitializer implements InitializingBean{
    
    @Autowired
    private PlatformTransactionManager tm;
    
    @PersistenceContext
    private EntityManager em;
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Resource jsonResource = new ClassPathResource("usstates.json");
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<USState>> ref = new TypeReference<List<USState>>() {};
        List<USState> states = mapper.readValue(jsonResource.getInputStream(), ref);
        TransactionStatus status = tm.getTransaction(null);
        for (USState state : states) {
            state.persist();
        }
        tm.commit(status);
    }
    
}
