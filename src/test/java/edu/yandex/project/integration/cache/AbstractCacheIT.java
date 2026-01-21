package edu.yandex.project.integration.cache;

import edu.yandex.project.config.RedisCacheConfig;
import edu.yandex.project.factory.ItemListPageViewFactory;
import edu.yandex.project.integration.config.ITRedisContainer;
import edu.yandex.project.mapper.ItemViewMapper;
import edu.yandex.project.repository.CartItemRepository;
import edu.yandex.project.repository.CartRepository;
import edu.yandex.project.repository.ItemPageableRepository;
import edu.yandex.project.repository.ItemRepository;
import edu.yandex.project.service.CartService;
import edu.yandex.project.service.ItemService;
import edu.yandex.project.service.impl.CartServiceImpl;
import edu.yandex.project.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ImportTestcontainers(ITRedisContainer.class)
@SpringBootTest(classes = {
        CartServiceImpl.class,
        ItemServiceImpl.class,

        RedisCacheConfig.class,
        CacheAutoConfiguration.class,
        RedisAutoConfiguration.class
})
public abstract class AbstractCacheIT {

    @Autowired
    protected CacheManager cacheManager;

    @MockitoSpyBean
    protected CartService cartService;
    @MockitoSpyBean
    protected ItemService itemService;

    @MockitoBean
    protected CartRepository cartRepository;
    @MockitoBean
    protected CartItemRepository cartItemRepository;
    @MockitoBean
    protected ItemRepository itemRepository;
    @MockitoBean
    protected ItemPageableRepository itemPageableRepository;

    @MockitoBean
    protected ItemViewMapper itemViewMapper;
    @MockitoBean
    protected ItemListPageViewFactory itemListPageViewFactory;

    @BeforeEach
    protected void cleanCache() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                    }
                });
    }

    protected void assertThatCacheIsEmpty(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        assertThat(cache).withFailMessage("Cache with name [" + cacheName + "] does not exist!")
                .isNotNull();
        assertThat(cache.get(SimpleKey.EMPTY)).withFailMessage("Cache with name [" + cacheName + "] is not empty!")
                .isNull();
    }
}
