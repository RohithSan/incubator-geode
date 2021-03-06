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
package org.apache.geode.cache;

import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolFactory;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.distributed.DistributedSystem;
import org.apache.geode.internal.cache.PoolFactoryImpl;
import org.apache.geode.test.junit.categories.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Properties;

import static org.apache.geode.distributed.ConfigurationProperties.LOCATORS;
import static org.apache.geode.distributed.ConfigurationProperties.MCAST_PORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests PoolManager
 * @since GemFire 5.7
 */
@Category(IntegrationTest.class)
public class PoolManagerJUnitTest {
  
  private DistributedSystem ds;
  
  @Before
  public void setUp() {
    Properties props = new Properties();
    props.setProperty(MCAST_PORT, "0");
    props.setProperty(LOCATORS, "");
    ds = DistributedSystem.connect(props);
    assertEquals(0, PoolManager.getAll().size());
  }
  
  @After
  public void tearDown() {
    PoolManager.close();
    ds.disconnect();
  }
  
  @Test
  public void testCreateFactory() {
    assertNotNull(PoolManager.createFactory());
    assertEquals(0, PoolManager.getAll().size());
  }

  @Test
  public void testGetMap() {
    assertEquals(0, PoolManager.getAll().size());
    {
      PoolFactory cpf = PoolManager.createFactory();
      ((PoolFactoryImpl)cpf).setStartDisabled(true);
      cpf.addLocator("localhost", 12345).create("mypool");
    }
    assertEquals(1, PoolManager.getAll().size());
    {
      PoolFactory cpf = PoolManager.createFactory();
      ((PoolFactoryImpl)cpf).setStartDisabled(true);
      cpf.addLocator("localhost", 12345).create("mypool2");
    }
    assertEquals(2, PoolManager.getAll().size());
    assertNotNull(PoolManager.getAll().get("mypool"));
    assertNotNull(PoolManager.getAll().get("mypool2"));
    assertEquals("mypool", (PoolManager.getAll().get("mypool")).getName());
    assertEquals("mypool2", (PoolManager.getAll().get("mypool2")).getName());
  }

  @Test
  public void testFind() {
    {
      PoolFactory cpf = PoolManager.createFactory();
      ((PoolFactoryImpl)cpf).setStartDisabled(true);
      cpf.addLocator("localhost", 12345).create("mypool");
    }
    assertNotNull(PoolManager.find("mypool"));
    assertEquals("mypool", (PoolManager.find("mypool")).getName());
    assertEquals(null, PoolManager.find("bogus"));
  }

  @Test
  public void testRegionFind() {
    PoolFactory cpf = PoolManager.createFactory();
    ((PoolFactoryImpl)cpf).setStartDisabled(true);
    Pool pool = cpf.addLocator("localhost", 12345).create("mypool");
    Cache cache = CacheFactory.create(ds);
    AttributesFactory fact = new AttributesFactory();
    fact.setPoolName(pool.getName());
    Region region = cache.createRegion("myRegion", fact.create());
    assertEquals(pool, PoolManager.find(region));
  }

  @Test
  public void testClose() {
    PoolManager.close();
    assertEquals(0, PoolManager.getAll().size());
    {
      PoolFactory cpf = PoolManager.createFactory();
      ((PoolFactoryImpl)cpf).setStartDisabled(true);
      cpf.addLocator("localhost", 12345).create("mypool");
    }
    assertEquals(1, PoolManager.getAll().size());
    PoolManager.close();
    assertEquals(0, PoolManager.getAll().size());
    {
      PoolFactory cpf = PoolManager.createFactory();
      ((PoolFactoryImpl)cpf).setStartDisabled(true);
      cpf.addLocator("localhost", 12345).create("mypool");
    }
    assertEquals(1, PoolManager.getAll().size());
    PoolManager.find("mypool").destroy();
    assertEquals(null, PoolManager.find("mypool"));
    assertEquals(0, PoolManager.getAll().size());
    PoolManager.close();
    assertEquals(0, PoolManager.getAll().size());
  }
}
