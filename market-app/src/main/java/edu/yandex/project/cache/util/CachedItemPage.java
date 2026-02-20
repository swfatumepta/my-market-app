package edu.yandex.project.cache.util;

import java.util.List;

public record CachedItemPage(List<Long> itemIds, Long total) {
}
