/*******************************************************************************
 * * Copyright 2011 Impetus Infotech.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 ******************************************************************************/
package com.impetus.kundera.metadata.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.metamodel.Metamodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.impetus.kundera.persistence.EntityManagerFactoryImpl;

/**
 * The Class ApplicationMetadata.
 * 
 * @author amresh.singh
 */
public class ApplicationMetadata
{
    /** Map of Entity Metadata. */
    private Map<String, Metamodel> metamodelMap = new ConcurrentHashMap<String, Metamodel>();

    /** Map of Persistence Unit Metadata. */
    private Map<String, PersistenceUnitMetadata> persistenceUnitMetadataMap = new ConcurrentHashMap<String, PersistenceUnitMetadata>();

    /** The Constant log. */
    private static Log logger = LogFactory.getLog(EntityManagerFactoryImpl.class);

    /**
     *  Collection instance to hold clazz's full name to persistence unit mapping.
     *  Valid Assumption: 1 class can belong to 1 pu only. Reason is @table needs to give pu name!
     */
    private Map<String, List<String>> clazzToPuMap;

    /**
     * Adds the entity metadata.
     * 
     * @param persistenceUnit
     *            the persistence unit
     * @param clazz
     *            the clazz
     * @param entityMetadata
     *            the entity metadata
     */
    public void addEntityMetadata(String persistenceUnit, Class<?> clazz, EntityMetadata entityMetadata)
    {
        Metamodel metamodel = getMetamodelMap().get(persistenceUnit);
        Map<Class<?>, EntityMetadata> entityClassToMetadataMap = ((MetamodelImpl) metamodel).getEntityMetadataMap();
        if (entityClassToMetadataMap == null || entityClassToMetadataMap.isEmpty())
        {
            entityClassToMetadataMap.put(clazz, entityMetadata);
        }
        else
        {
            logger.debug("Entity meta model already exists for persistence unit " + persistenceUnit + " and class "
                    + clazz + ". Noting needs to be done");
        }
    }

    /**
     * Adds the persistence unit metadata.
     * 
     * @param persistenceUnit
     *            the persistence unit
     * @param persistenceUnitMetadata
     *            the persistence unit metadata
     */
    public void addPersistenceUnitMetadata(String persistenceUnit, PersistenceUnitMetadata persistenceUnitMetadata)
    {
        getPersistenceUnitMetadataMap().put(persistenceUnit, persistenceUnitMetadata);
    }

    /**
     * Gets the metamodel map.
     * 
     * @return the entityMetadataMap
     */
    public Map<String, Metamodel> getMetamodelMap()
    {
        if (metamodelMap == null)
        {
            metamodelMap = new HashMap<String, Metamodel>();
        }
        return metamodelMap;
    }

    /**
     * Gets the persistence unit metadata.
     * 
     * @param persistenceUnit
     *            the persistence unit
     * @return the persistence unit metadata
     */
    public PersistenceUnitMetadata getPersistenceUnitMetadata(String persistenceUnit)
    {
        return getPersistenceUnitMetadataMap().get(persistenceUnit);
    }

    /**
     * Gets the metamodel.
     * 
     * @param persistenceUnit
     *            the persistence unit
     * @return the metamodel
     */
    public Metamodel getMetamodel(String persistenceUnit)
    {
        Map<String, Metamodel> model = getMetamodelMap();
        return persistenceUnit != null && model.containsKey(persistenceUnit) ? model.get(persistenceUnit) : null;
    }

    /**
     * Gets the persistence unit metadata map.
     * 
     * @return the persistenceUnitMetadataMap
     */
    public Map<String, PersistenceUnitMetadata> getPersistenceUnitMetadataMap()
    {
        return persistenceUnitMetadataMap;
    }

    /**
     * Sets the clazz to pu map.
     *
     * @param map the map
     */
    public void setClazzToPuMap(Map<String, List<String>> map)
    {
        if(clazzToPuMap == null)
        {
            this.clazzToPuMap = map;
        } else
        {
            clazzToPuMap.putAll(map);
        }
    }
    
    /**
     * Gets the mapped persistence unit.
     *
     * @param clazz the clazz
     * 
     * @return the mapped persistence unit
     */
    public List<String> getMappedPersistenceUnit(Class<?> clazz)
    {
        return this.clazzToPuMap.get(clazz.getName());
    }

    /**
     * returns mapped persistence unit.
     * 
     * @param clazzName  clazz name.
     * 
     * @return mapped persistence unit.
     */
    public String getMappedPersistenceUnit(String clazzName)
    {
        
        List<String> pus = clazzToPuMap.get(clazzName);
        
        final int _first=0;
        String pu=null;
        
        if(pus != null && !pus.isEmpty())
        {
            if(pus.size() == 2)
            {
                onError(clazzName);
            }
           return pus.get(_first); 
        } else
        {
            Set<String> mappedClasses = this.clazzToPuMap.keySet();
            boolean found=false;
            for(String clazz : mappedClasses)
            {
                if(found && clazz.endsWith("."+clazzName))
                {
                    onError(clazzName);
                } else if(clazz.endsWith("." + clazzName))
                {
                    pu = clazzToPuMap.get(clazz).get(_first);
                    found=true;
                }
            }
        }
        
        return pu;
    }

    /**
     * Handler error and log statements.
     * 
     * @param clazzName  class name.
     */
    private void onError(String clazzName)
    {
        logger.error("Duplicate name:" +  clazzName + "Please provide entity with complete package name.");
        throw new ApplicationLoaderException("Duplicate name:" +  clazzName + "Please provide entity with complete package name");
    }
}
