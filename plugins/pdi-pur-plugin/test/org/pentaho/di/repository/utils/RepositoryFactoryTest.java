package org.pentaho.di.repository.utils;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 11/5/15.
 */
public class RepositoryFactoryTest {

  @BeforeClass
  public static void setup() throws Exception {
    PentahoSystem.init( new StandaloneApplicationContext( "", "" ) );
    KettleEnvironment.init();
  }

  @Test
  public void testConnectNoSession() throws Exception {
    IRepositoryFactory.CachingRepositoryFactory cachingRepositoryFactory = new IRepositoryFactory
        .CachingRepositoryFactory();

    // Call with no Session, should throw Exception
    try {
      cachingRepositoryFactory.connect( "foo" );
      fail( "Should have thrown exception" );
    } catch ( KettleException expected ) {
    }


  }

  @Test
  public void testCachingFactoryConnect() throws Exception {
    ICacheManager cacheManager = mock( ICacheManager.class );
    PentahoSystem.registerObject( cacheManager );
    IPentahoSession session = new StandaloneSession( "joe" );
    PentahoSessionHolder.setSession( session );

    // Delegate is just a mock. connect will be a cache miss
    IRepositoryFactory mockFactory = mock( IRepositoryFactory.class );
    IRepositoryFactory.CachingRepositoryFactory cachingRepositoryFactory = new IRepositoryFactory
        .CachingRepositoryFactory( mockFactory );


    cachingRepositoryFactory.connect( "foo" );

    verify( mockFactory, times( 1 ) ).connect( "foo" );

    // Test with Cache Hit
    Repository mockRepository = mock( Repository.class );
    when( cacheManager.cacheEnabled( IRepositoryFactory.CachingRepositoryFactory.REGION ) ).thenReturn( true );
    when( cacheManager.getFromRegionCache( IRepositoryFactory.CachingRepositoryFactory.REGION, "joe" ) ).
        thenReturn( mockRepository );

    Repository repo = cachingRepositoryFactory.connect( "foo" );
    assertThat( repo, sameInstance( mockRepository ) );
  }


  @Test
  public void testDefaultFactoryConnect() throws Exception {

    IRepositoryFactory.DefaultRepositoryFactory repositoryFactory = new IRepositoryFactory
        .DefaultRepositoryFactory();
    repositoryFactory.setRepositoryId( "KettleFileRepository" );

    IPentahoSession session = new StandaloneSession( "joe" );
    PentahoSessionHolder.setSession( session );

    repositoryFactory.connect( "foo" );

  }

  @Test
  public void testFactoryRegisteredWithPentahoSystem() throws Exception {

    IRepositoryFactory defaultRepositoryFactory = IRepositoryFactory.DEFAULT;

    IRepositoryFactory repositoryFactoryFromPentahoSystem = PentahoSystem.get( IRepositoryFactory.class );
    assertThat( repositoryFactoryFromPentahoSystem, sameInstance( defaultRepositoryFactory ) );

  }
}